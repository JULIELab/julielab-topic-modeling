package de.julielab.topicmodeling.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.julielab.jcore.reader.xmi.XmiDBReader;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.TokenSequence;
import de.julielab.jcore.reader.db.SubsetReaderConstants;
import de.julielab.jcore.reader.db.TableReaderConstants;
import de.julielab.jcore.types.Annotation;
import de.julielab.jcore.types.Lemma;
import de.julielab.jcore.types.Token;
import de.julielab.jcore.types.pubmed.Header;
import de.julielab.jcore.utility.JCoReTools;
import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.TMSearchResult;
import de.julielab.topicmodeling.businessobjects.Topic;
import de.julielab.xml.JulieXMLConstants;
import de.julielab.xml.JulieXMLTools;

public class MalletTopicModeling implements ITopicModeling {

	private static final Logger LOGGER = LoggerFactory.getLogger(MalletTopicModeling.class);
	
	String forEach = "/PubmedArticleSet/PubmedArticle/MedlineCitation";
	String idField = "PMID";
	String textField = "Article/Abstract/AbstractText";
	String alternativeTextField = "OtherAbstract/AbstractText";
	
	public MalletTopicModeling() {
	}
	
	public XMLConfiguration loadConfig(String configFile) throws ConfigurationException {
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<XMLConfiguration> builder =
		    new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class)
		    .configure(params.xml()
		        .setFileName(configFile));
		XMLConfiguration xmlConfig = builder.getConfiguration();
		return xmlConfig;
	}

	public Model train(List<Document> docs, XMLConfiguration xmlConfig) {
		int numTopics = xmlConfig.getInt("train.parameters.parameter.numTopics"); 
		double alphaSum = xmlConfig.getDouble("train.parameters.parameter.alphaSum"); 
		double beta = xmlConfig.getDouble("train.parameters.parameter.beta");
		int numThreads = xmlConfig.getInt("train.parameters.parameter.numThreads");
		int numIterations = xmlConfig.getInt("train.parameters.parameter.numIterations");
		int optimizationInterval = xmlConfig.getInt("train.parameters.parameter.optimizationInterval");
		String modelId = xmlConfig.getString("model.meta.ID");
		String modelVersion = xmlConfig.getString("model.meta.Version");
		
		LOGGER.info("Chosen number of topics: " + numTopics);
		LOGGER.info("Chosen Dirichlet-alpha: " + alphaSum);
		LOGGER.info("Chosen Dirichlet-beta: " + beta);
		LOGGER.info("Chosen training iterations: " + numIterations);
		LOGGER.info("Chosen optimization interval (if 0, optim. is deactivated): " 
						+ optimizationInterval);
		
		ParallelTopicModel malletParallelModel = new ParallelTopicModel(numTopics, alphaSum, beta);
		Model model = new Model();
		model.modelId = modelId;
		model.modelVersion = modelVersion;
		try {
			LOGGER.info("Start preprocessing");
			InstanceList instances = preprocess(docs);
			malletParallelModel.addInstances(instances);			 
			malletParallelModel.setNumThreads(numThreads);
			malletParallelModel.setNumIterations(numIterations);
			malletParallelModel.setOptimizeInterval(optimizationInterval);
			malletParallelModel.estimate();
			model.malletModel = malletParallelModel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("Model: " + modelId + " Version: " + modelVersion + " is trained");
		mapMalletIdToPubmedId(docs, model);
		LOGGER.info("PubMed citation IDs (PMIDs) are mapped to Mallet document IDs");
		return model;
	}
	
	public Model train(InstanceList instances, XMLConfiguration xmlConfig) {
		String modelId = xmlConfig.getString("model.id"); 
		String modelVersion = xmlConfig.getString("model.version");
		
		int numTopics = xmlConfig.getInt("train.parameters.parameter.numTopics"); 
		double alphaSum = xmlConfig.getDouble("train.parameters.parameter.alphaSum"); 
		double beta = xmlConfig.getDouble("train.parameters.parameter.beta");
		int numThreads = xmlConfig.getInt("train.parameters.parameter.numThreads");
		int numIterations = xmlConfig.getInt("train.parameters.parameter.numIterations");
		int optimizationInterval = xmlConfig.getInt("train.parameters.parameter.optimizationInterval");
		
		LOGGER.info("Chosen number of topics: " + numTopics);
		LOGGER.info("Chosen Dirichlet-alpha: " + alphaSum);
		LOGGER.info("Chosen Dirichlet-beta: " + beta);
		LOGGER.info("Chosen training iterations: " + numIterations);
		LOGGER.info("Chosen optimization interval (if 0, optim. is deactivated): " 
						+ optimizationInterval);
		
		ParallelTopicModel malletParallelModel = new ParallelTopicModel(numTopics, alphaSum, beta);
		Model model = new Model();
		try {
			LOGGER.info("Start preprocessing");
			malletParallelModel.addInstances(instances);
			malletParallelModel.setNumThreads(numThreads);
			malletParallelModel.setNumIterations(numIterations);
			malletParallelModel.setOptimizeInterval(optimizationInterval);
			LOGGER.info("Start training");
			malletParallelModel.estimate();
			model.malletModel = malletParallelModel;
			model.modelId = modelId;
			model.modelVersion = modelVersion;
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("Model is trained");
		return model;
	}
	
	public void saveMalletModel(Model model, File file) {
		try {
		if (model.malletModel != null) {
			ParallelTopicModel newModel = model.malletModel;
			newModel.write(file);
			LOGGER.info("Mallet model is saved in " + file.getName());
		} else {
			LOGGER.info("No Mallet model was found in "
					+ "ModelID: " + model.modelId + ", ModelVersion: " + model.modelVersion);
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveModel(Model model, String filename) {
		try {
			FileOutputStream fileOut = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(model);
			out.close();
			fileOut.close();
			LOGGER.info("Serialized Model is saved in " + filename);
	    } catch (IOException i) {
	       i.printStackTrace();
	    }
	}
	
	// Reader
	public List<Document> readDocuments(File file, XMLConfiguration xmlConfig) {
		if (file.isDirectory()) {
			FilenameFilter xmlFilter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.endsWith(".xml.gz") || name.endsWith(".xml.zip")
							|| name.endsWith(".xml.gzip") || name.endsWith(".xml")) {
						return true;
					} else {
						return false;
					}
				}
			};
			List<Document> docs = new ArrayList<Document>();
			File[] xmlFiles = file.listFiles(xmlFilter);
			int fileCount = xmlConfig.getInteger("evaluate.heldout.files.number", xmlFiles.length);
			for (int i = 0; i < fileCount; i++) {
				LOGGER.info("Attempt to read " + xmlFiles[i].getName() + ", no. " + (i + 1)  
							+ " of total " + fileCount);
				List<Document> docsFileI = readDocuments(xmlFiles[i], xmlConfig);
				for (int j = 0; j < docsFileI.size(); j++) {
					docs.add(docsFileI.get(j));
				}
			}
			return docs;
		} else {
			String fileName = file.getAbsolutePath();
			String[] fieldPaths = new String [3];
			fieldPaths[0] = idField;
			fieldPaths[1] = textField;
			fieldPaths[2] = alternativeTextField;
			List<Map<String, String>> fields = new ArrayList<>();
			for (int i = 0; i < fieldPaths.length; i++) {
				String path = fieldPaths[i];
				Map<String, String> field = new HashMap<String, String>();
				field.put(JulieXMLConstants.NAME, "fieldvalue" + i);
				field.put(JulieXMLConstants.XPATH, path);
				fields.add(field);
			};
			Iterator<Map<String, Object>> rowIterator = JulieXMLTools.constructRowIterator(
				fileName, 1024, forEach, fields, false);
			
			List<Document> docs = new ArrayList<Document>();
			while (rowIterator.hasNext()) {
				Document doc = new Document();
				Map<String, Object> row = rowIterator.next();
				List<String> rowValues = new ArrayList<>();
				String idValue = (String) row.get("fieldvalue" + 0);
				String textValue = (String) row.get("fieldvalue" + 1);
				String alternativeTextValue = (String) row.get("fieldvalue" + 2);
				rowValues.add(idValue);
				rowValues.add(textValue);
				rowValues.add(alternativeTextValue);
				if (idValue != null) {
					doc.id = idValue;
				}
				if (textValue != null) {
					doc.text = textValue;
				}
				if (alternativeTextValue != null) {
					doc.text = alternativeTextValue;
				}
				docs.add(doc);
				}
			LOGGER.info("Total citations found: " + docs.size());
			return docs;
		}
	}		
	
	public List<Document> readXmiDb(MalletTopicModeling tm, HierarchicalConfiguration<ImmutableNode> configuration) {
		String subset = configuration.getString("train.corpus.subset.table");
		boolean resetSubset = configuration.getBoolean("train.corpus.subset.reset", false);
		String costosysConfigFile = configuration.getString("train.corpus.costosys.configurationFile");
		LOGGER.info("Start reading from DB table {} with CoStoSys configuration file {}", subset, costosysConfigFile);
		List<String> annotationsToLoad = new ArrayList<>();
		annotationsToLoad.add("_data_xmi." + Token.class.getCanonicalName());
		List<Document> docs = new ArrayList<>();
		try {
			CollectionReader xmiDbReader = CollectionReaderFactory.createReader(
			        "de.julielab.jcore.reader.xmi.desc.jcore-xmi-db-reader",
					SubsetReaderConstants.PARAM_ADDITIONAL_TABLES,
					annotationsToLoad, TableReaderConstants.PARAM_TABLE, subset,
					TableReaderConstants.PARAM_COSTOSYS_CONFIG_NAME, costosysConfigFile,
					XmiDBReader.PARAM_READS_BASE_DOCUMENT, true,
					SubsetReaderConstants.PARAM_RESET_TABLE, resetSubset);
			JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
			CAS aCAS = jCas.getCas();
			while (xmiDbReader.hasNext()) {
				xmiDbReader.getNext(aCAS);
				Document doc = new Document();
				JCas filledjCas = aCAS.getJCas();
				TokenSequence docLemmata = tm.getLemmata(filledjCas);
				doc.preprocessedData = docLemmata;
				doc.id = tm.getId(filledjCas);
				LOGGER.info("Data for doc " + doc.id + ": " + doc.preprocessedData);
				docs.add(doc);
			}
		} catch (Exception e) {
            throw new RuntimeException(e);
		}		 
		return docs;	
	}
	
	//Searcher
	public TMSearchResult search(Document query, Model model, XMLConfiguration xmlConfig) {
		double probabilityThreshold = xmlConfig.getDouble("search.parameters.parameter"
				+ ".probabilityThreshold"); 
		
		TMSearchResult result = new TMSearchResult();
		result.malletId = new ArrayList<Integer>();
		result.probabilities = new ArrayList<Double>();
		result.pubmedID = new ArrayList<String>();
		
		// preprocess query
		if (query.preprocessedData == null) {
			List<Document> queryInList = new ArrayList<>();
			queryInList.add(query);
			List<TokenSequence> preprocessedQueryInList = jcorePreprocess(queryInList);
			query.preprocessedData = preprocessedQueryInList.get(0);
		}
		Map<String, List<Topic>> queryInstance = inferLabel(query, model, xmlConfig);
		List<Topic> queryTopics = queryInstance.get(query.id);
		
		// reduce query vector to the values bigger than the threshold
		List<Integer> relevantProbabilitiesIndex = new ArrayList<Integer>();
		for (int i = 0; i < queryTopics.size(); i++) {
			if (queryTopics.get(i).probability >= probabilityThreshold) {
				relevantProbabilitiesIndex.add(i);
			}	
		}
		double[] queryProbabilities = new double[relevantProbabilitiesIndex.size()];
		HashMap<Integer, Double> cosineSimilarities = new HashMap<Integer, Double>();
		for (int i = 0; i < relevantProbabilitiesIndex.size(); i++) {
			double queryProbability = queryTopics.get(i).probability;
			queryProbabilities[i] = queryProbability;
		}
		
		// get topics from modeled documents
		ParallelTopicModel malletModel = model.malletModel;
		double[][] documentsTopics = malletModel.getDocumentTopics(false, false);
		for (int i = 0 ; i < relevantProbabilitiesIndex.size(); i++) {
			double[] documentTopics = documentsTopics[relevantProbabilitiesIndex.get(i)];
			double cosineSimilarity = MalletTopicModeling.computeSimilarity(queryProbabilities, documentTopics);
			cosineSimilarities.put(i, cosineSimilarity);
		}
		
		// get topics from indexed documents
		HashMap<String, List<Topic>> index = model.index;
		for (int i = 0; i < index.size(); i++) {
			List<Topic> documentTopics = index.get(index.keySet().toArray()[i]);
			if (documentTopics != null) {
				double[] documentProbabilities = new double[relevantProbabilitiesIndex.size()];
				for (int k = 0 ; k < relevantProbabilitiesIndex.size(); k++) {
					for (int m = 0; m < documentTopics.size(); m++) {
						if (documentTopics.get(m).id == relevantProbabilitiesIndex.get(k) 
								&& relevantProbabilitiesIndex.get(k) != null) {
							documentProbabilities[k] = documentTopics.get(m).probability;
						}
					}
				}
				double cosineSimilarity = MalletTopicModeling.computeSimilarity
						(queryProbabilities, documentProbabilities);
				cosineSimilarities.put(i, cosineSimilarity);
			}
		}
		List<Entry<Integer, Double>> list = new LinkedList<Entry<Integer, Double>>(
				cosineSimilarities.entrySet());
        Collections.sort(list, new Comparator<Entry<Integer, Double>>() {
            public int compare(Entry<Integer, Double> o1,
            	Entry<Integer, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        int displayedHits = xmlConfig.getInt("search.results.displayedHits", list.size());
        // displayHits as cosine threshold
        for(int i = 0; i < displayedHits; i++) {
        	Entry<Integer, Double> entry = list.get(i);
            result.malletId.add(entry.getKey());
            result.probabilities.add(entry.getValue());
            result.pubmedID.add(model.ModelIdpubmedId.get(entry.getKey()));
        }
		return result;
	}
	
	
	public TMSearchResult searchModelOnly(Document query, Model model, XMLConfiguration xmlConfig) {
		double probabilityThreshold = xmlConfig.getDouble("search.parameters.parameter"
				+ ".probabilityThreshold"); 
		
		TMSearchResult result = new TMSearchResult();
		result.malletId = new ArrayList<Integer>();
		result.probabilities = new ArrayList<Double>();
		result.pubmedID = new ArrayList<String>();
		
		// preprocess query
		if (query.preprocessedData == null) {
			List<Document> queryInList = new ArrayList<>();
			queryInList.add(query);
			List<TokenSequence> preprocessedQueryInList = jcorePreprocess(queryInList);
			query.preprocessedData = preprocessedQueryInList.get(0);
		}
		Map<String, List<Topic>> queryInstance = inferLabel(query, model, xmlConfig);
		List<Topic> queryTopics = queryInstance.get(query.id);
		
		// reduce query vector to the values bigger than the threshold
		List<Integer> relevantProbabilitiesIndex = new ArrayList<Integer>();
		for (int i = 0; i < queryTopics.size(); i++) {
			if (queryTopics.get(i).probability >= probabilityThreshold) {
				relevantProbabilitiesIndex.add(i);
			}	
		}
		double[] queryProbabilities = new double[relevantProbabilitiesIndex.size()];
		HashMap<Integer, Double> cosineSimilarities = new HashMap<Integer, Double>();
		for (int i = 0; i < relevantProbabilitiesIndex.size(); i++) {
			double queryProbability = queryTopics.get(i).probability;
			queryProbabilities[i] = queryProbability;
		}

		// get topics from modeled documents 
		ParallelTopicModel malletModel = model.malletModel;
		double[][] documentsTopics = malletModel.getDocumentTopics(false, false);
		for (int i = 0 ; i < relevantProbabilitiesIndex.size(); i++) {
			double[] documentTopics = documentsTopics[relevantProbabilitiesIndex.get(i)];
			double cosineSimilarity = MalletTopicModeling.computeSimilarity(queryProbabilities, documentTopics);
			cosineSimilarities.put(i, cosineSimilarity);
		}
		
		List<Entry<Integer, Double>> list = new LinkedList<Entry<Integer, Double>>(
				cosineSimilarities.entrySet());
        Collections.sort(list, new Comparator<Entry<Integer, Double>>() {
            public int compare(Entry<Integer, Double> o1,
            	Entry<Integer, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        int displayedHits = xmlConfig.getInt("search.results.displayedHits", list.size());
        // displayHits as cosine threshold
        for(int i = 0; i < displayedHits; i++) {
        	Entry<Integer, Double> entry = list.get(i);
            result.malletId.add(entry.getKey());
            result.probabilities.add(entry.getValue());
            result.pubmedID.add(model.ModelIdpubmedId.get(entry.getKey()));
        }
        return result;
	}
	
	public TMSearchResult searchIndexOnly(Document query, Model model, XMLConfiguration xmlConfig) {
		double probabilityThreshold = xmlConfig.getDouble("search.parameters.parameter"
				+ ".probabilityThreshold"); 
		
		TMSearchResult result = new TMSearchResult();
		result.malletId = new ArrayList<Integer>();
		result.probabilities = new ArrayList<Double>();
		result.pubmedID = new ArrayList<String>();
		
		if (query.preprocessedData == null) {
			List<Document> queryInList = new ArrayList<>();
			queryInList.add(query);
			List<TokenSequence> preprocessedQueryInList = jcorePreprocess(queryInList);
			query.preprocessedData = preprocessedQueryInList.get(0);
		}
		Map<String, List<Topic>> queryInstance = inferLabel(query, model, xmlConfig);
		List<Topic> queryTopics = queryInstance.get(query.id);
		
		// reduce query vector to the values bigger than the thresholds
		List<Integer> relevantProbabilitiesIndex = new ArrayList<Integer>();
		for (int i = 0; i < queryTopics.size(); i++) {
			if (queryTopics.get(i).probability >= probabilityThreshold) {
				relevantProbabilitiesIndex.add(i);
			}	
		}
		double[] queryProbabilities = new double[relevantProbabilitiesIndex.size()];
		HashMap<Integer, Double> cosineSimilarities = new HashMap<Integer, Double>();
		for (int i = 0; i < relevantProbabilitiesIndex.size(); i++) {
			double queryProbability = queryTopics.get(i).probability;
			queryProbabilities[i] = queryProbability;
		}
		
		// get topics from indexed documents
		HashMap<String, List<Topic>> index = model.index;
		for (int i = 0; i < index.size(); i++) {
			List<Topic> documentTopics = index.get(index.keySet().toArray()[i]);
			if (documentTopics != null) {
				double[] documentProbabilities = new double[relevantProbabilitiesIndex.size()];
				for (int k = 0 ; k < relevantProbabilitiesIndex.size(); k++) {
					for (int m = 0; m < documentTopics.size(); m++) {
						if (documentTopics.get(m).id == relevantProbabilitiesIndex.get(k) 
								&& relevantProbabilitiesIndex.get(k) != null) {
							documentProbabilities[k] = documentTopics.get(m).probability;
						}
					}
				}
				double cosineSimilarity = MalletTopicModeling.computeSimilarity
						(queryProbabilities, documentProbabilities);
				cosineSimilarities.put(i, cosineSimilarity);
			}
		}
		List<Entry<Integer, Double>> list = new LinkedList<Entry<Integer, Double>>(
				cosineSimilarities.entrySet());
        Collections.sort(list, new Comparator<Entry<Integer, Double>>() {
            public int compare(Entry<Integer, Double> o1,
            	Entry<Integer, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        int displayedHits = xmlConfig.getInt("search.results.displayedHits", list.size());
        // displayHits as cosine threshold
        for(int i = 0; i < displayedHits; i++) {
        	Entry<Integer, Double> entry = list.get(i);
            result.malletId.add(entry.getKey());
            result.probabilities.add(entry.getValue());
            result.pubmedID.add(model.ModelIdpubmedId.get(entry.getKey()));
        }
		return result;
	}
	
	public Map<String, List<Topic>> inferLabel(Document doc, Model model, XMLConfiguration xmlConfig) {
		LOGGER.warn("Argument topicWords is not set in inferLabel! This may cause low performance "
				+ "during infering labels!");
		int numWords = model.malletModel.wordsPerTopic;
		Object[][] topicWords = model.malletModel.getTopWords(numWords);
		Map<String, List<Topic>> result = inferLabel(doc, model, xmlConfig, topicWords);
		return result;
	}
	
	/**
	 * The argument topicWords has to be passed here due to performance reasons; this object is
	 * available in every model.malletModel by the method getTopWords()  
	 */
	public Map<String, List<Topic>> inferLabel(Document doc, Model model, XMLConfiguration xmlConfig,
											Object[][] topicWords) {
		int numIterations = xmlConfig.getInt("infer.parameters.parameter.numIterations");
		int thinning = xmlConfig.getInt("infer.parameters.parameter.savingInterval");
		int burnIn = xmlConfig.getInt("infer.parameters.parameter.firstSavingInterval");
		Map<String, List<Topic>> result = new HashMap<String, List<Topic>>();
		ParallelTopicModel malletModel = model.malletModel;
		TopicInferencer inferencer = malletModel.getInferencer();
		
		TokenSequence jcorePreprocessed = (TokenSequence) doc.preprocessedData;
		if (jcorePreprocessed.isEmpty()) {
			LOGGER.warn("Document tokens are empty");
		}
		List<TokenSequence> jcorePreprocessedList = new ArrayList<>();
		jcorePreprocessedList.add(jcorePreprocessed);
//		List<TokenSequence> jcorePreprocessedList = (List<TokenSequence>) doc.preprocessedData;
		InstanceList instances = malletPreprocess(jcorePreprocessedList);
		Instance instance = instances.get(0);
		
		double[] distribution = inferencer.getSampledDistribution(
				instance, numIterations, thinning, burnIn);
		List<Topic> topics = new ArrayList<Topic>();
		for (int i = 0; i < distribution.length; i++) {
			Topic topic = new Topic();
			topic.probability = distribution[i];
			topic.topicWords = topicWords[i];
			topic.id = i;
			topics.add(topic);
		}
		result.put(doc.id, topics);
		return result;
	}
	
	/**
	 * The argument topicWords has to be passed here due to performance reasons; this object is
	 * available in every model.malletModel by the method getTopWords()  
	 */
	public Map<String, List<Topic>> inferLabel(JCas cas, Model model, XMLConfiguration xmlConfig,
											Object[][] topicWords) {
		TokenSequence docLemmata = getLemmata(cas);
		Document doc = new Document();
		doc.id = JCoReTools.getDocId(cas);
		doc.preprocessedData = docLemmata;
		Map<String, List<Topic>> result = inferLabel(doc, model, xmlConfig);
		return result;
	}
		
	public Model readMalletModel(File file) {
		Model model = new Model();
		try {
			ParallelTopicModel malletParallelTopicModel = ParallelTopicModel.read(file);
			model.malletModel = malletParallelTopicModel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}
	
	public Model readModel(String filename) {
		Model model = new Model();
		try {
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			model = (Model) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
		} catch (ClassNotFoundException c) {
			System.out.println("Model class not found");
			c.printStackTrace();
		}
		return model;
    }
	
	//Services
	//Preprocessor
	public InstanceList preprocess(List<Document> docs) {
		List<TokenSequence> allLemmata = jcorePreprocess(docs);
		InstanceList instances = malletPreprocess(allLemmata);
		return instances;
	}
	
	public List<TokenSequence> jcorePreprocess(List<Document> docs) {
		TokenSequence foundLemmata = new TokenSequence();
		List<TokenSequence> allLemmata = new ArrayList<TokenSequence>();
		try {
			AnalysisEngine sentenceDetector = AnalysisEngineFactory.createEngine(
					"de.julielab.jcore.ae.jsbd.desc.jcore-jsbd-ae-biomedical-english");
			AnalysisEngine tokenizer = AnalysisEngineFactory.createEngine(
					"de.julielab.jcore.ae.jtbd.desc.jcore-jtbd-ae-biomedical-english");
			AnalysisEngine posTagger = AnalysisEngineFactory.createEngine(
					"de.julielab.jcore.ae.opennlp.postag.desc.jcore-opennlp"
					+ "-postag-ae-biomedical-english");
			AnalysisEngine bioLemmatizer = AnalysisEngineFactory.createEngine(
					"de.julielab.jcore.ae.biolemmatizer.desc.jcore-biolemmatizer-ae");
			JCas jCas = JCasFactory.createJCas();
			for (int i = 0; i < docs.size(); i++) {
				String sentences = docs.get(i).text;
				LOGGER.info("Attempt to process document: " + docs.get(i).id);
				if (sentences != null) {
					jCas.setDocumentText(sentences);
					sentenceDetector.process(jCas);
					tokenizer.process(jCas);
					posTagger.process(jCas);
					bioLemmatizer.process(jCas);
					foundLemmata = getLemmata(jCas);
					allLemmata.add(foundLemmata);
					jCas.reset();
				}
			}
		sentenceDetector.destroy();
		tokenizer.destroy();
		posTagger.destroy();
		bioLemmatizer.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("JCoRe preprocessing finished");
		return allLemmata;
	}
	
	public InstanceList malletPreprocess(List<TokenSequence> data) {
		ArrayList<Pipe> pipeList = new ArrayList<>();
		pipeList.add( new TokenSequenceRemoveStopwords(false, false));
		pipeList.add( new TokenSequence2FeatureSequence() );
		InstanceList instances = new InstanceList (new SerialPipes(pipeList));
		ArrayIterator dataListIterator = new ArrayIterator(data);
		instances.addThruPipe(dataListIterator);
		LOGGER.info("MALLeT instances created");
		return instances;
	}
	
	public TokenSequence getLemmata(JCas aJCas) {
		TokenSequence lemmata = new TokenSequence();
		AnnotationIndex<Annotation> tokenIndex = aJCas.getAnnotationIndex(Token.type);
		FSIterator<Annotation> tokenIterator = tokenIndex.iterator();
		while (tokenIterator.hasNext()) {
				Token token = (Token) tokenIterator.get();
				Lemma lemma = token.getLemma();
				if (lemma == null)
					throw new IllegalArgumentException("The input UIMA CAS is missing lemma annotations set to the tokens as the lemma feature.");
				String lemmaString = lemma.getValue();
				if (isNotNum(lemmaString) && isNotPunctuation(lemmaString)) { 
					lemmata.add(lemmaString);
				}
				tokenIterator.next();
		}
		return lemmata;
	}
	
	public String getId(JCas aJCas) {
		String id = "";
		AnnotationIndex<Annotation> headerIndex = aJCas.getAnnotationIndex(Header.type);
		FSIterator<Annotation> headerIterator = headerIndex.iterator();
		while (headerIterator.hasNext()) {
			Header header = (Header) headerIterator.next();
			id = header.getDocId();
			LOGGER.trace("Found id: " + id);
		}
		return id;
	}
	
	/**
	* Filters simple numbers that does not have real semantics
	*/
	public boolean isNotNum (String lemmaString) {
		String num = "\\s?-?\\d+.?\\d*\\s?";
		if (lemmaString.matches(num)) {
			return false;
		} else {
		return true;
		}
	}
	
	public boolean isNotPunctuation (String lemmaString) {
		String punct = "[.,:;!?\\-\\/()<>\\[\\]%\'+=]";
		if (lemmaString.matches(punct)) {
			return false;
		} else {
		return true;
		}
	}
	
	public void mapPubmedIdToMalletId(List<Document> docs, Model model) {
		model.pubmedIdModelId = new HashMap<String, Integer>();
		for (int i = 0; i < docs.size(); i++) {
			Document doci = docs.get(i);
			String dociId = doci.id;
			LOGGER.debug("Attempting to map PMID " + dociId + " to mallet doc " + i);
			model.pubmedIdModelId.put(dociId, i);
			LOGGER.info("PubMed citation IDs (PMIDs) are mapped to Mallet document IDs");
		}
	}
	
	public void mapMalletIdToPubmedId(List<Document> docs, Model model) {
		model.ModelIdpubmedId = new HashMap<Integer, String>();
		for (int i = 0; i < docs.size(); i++) {
			Document doci = docs.get(i);
			String dociId = doci.id;
			LOGGER.debug("Attempting to map Mallet DocID " + i + " to PMID " + dociId);
			model.ModelIdpubmedId.put(i, dociId);
		}
		LOGGER.info("Mallet document IDs are mapped to PubMed citation IDs (PMIDs)");
	}

//	public List<Topic> buildTopicsFromModel(Model model, InstanceList instances) {
//		ParallelTopicModel malletModel = model.malletModel;
//		ArrayList<TreeSet<IDSorter>> topicSortedWords = malletModel.getSortedWords();
//		Alphabet dataAlphabet = instances.getDataAlphabet();
//		List<Topic> topics = new ArrayList<Topic>();
//		for (int topic = 0; topic < topicSortedWords.size(); topic++) {
//			Topic newTopic = new Topic();
//			Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
//			while (iterator.hasNext()) {
//				IDSorter idCountPair = iterator.next();
//				newTopic.id = idCountPair.getID();
//				newTopic.topicWords = (String) dataAlphabet.lookupObject(idCountPair.getID());
//				System.out.println(newTopic.topicWords);
//			}
//		}
//		return topics;
//	}
	
	 public static double computeSimilarity(double[] vectorA, double[] vectorB) {
		    double dotProduct = 0.0;
		    double normA = 0.0;
		    double normB = 0.0;
		    for (int i = 0; i < vectorA.length; i++) {
		        dotProduct += vectorA[i] * vectorB[i];
		        normA += Math.pow(vectorA[i], 2);
		        normB += Math.pow(vectorB[i], 2);
		    }   
		    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
		}
	 
	 public Object[] getVocabulary(Model model) {
		 ParallelTopicModel malletModel = model.malletModel;
		 Alphabet alphabet = malletModel.getAlphabet();
		 Object[] alphabetArray = alphabet.toArray();
		 return alphabetArray;
	 }
}
