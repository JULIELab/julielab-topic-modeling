package de.julielab.topicmodeling.services;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveNonAlpha;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.TokenSequence;
import de.julielab.jcore.types.Annotation;
import de.julielab.jcore.types.Lemma;
import de.julielab.jcore.types.Token;
import de.julielab.topicmodeling.businessobjects.Configuration;
import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.Query;
import de.julielab.topicmodeling.businessobjects.TMSearchResult;
import de.julielab.topicmodeling.businessobjects.Topic;
import de.julielab.xml.JulieXMLConstants;
import de.julielab.xml.JulieXMLTools;

public class MalletTopicModeling implements ITopicModeling {

	private static final Logger LOGGER = LoggerFactory.getLogger(MalletTopicModeling.class);
	
	public XMLConfiguration xmlConfig;
	String forEach = "/PubmedArticleSet/PubmedArticle/MedlineCitation";
	
	String idField = "PMID";
	String textField = "Article/Abstract/AbstractText";
	String alternativeTextField = "OtherAbstract/AbstractText";
	
	public MalletTopicModeling(String configFile) throws ConfigurationException {
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<XMLConfiguration> builder =
		    new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class)
		    .configure(params.xml()
		        .setFileName(configFile));
		xmlConfig = builder.getConfiguration();
	}

	//Trainer
	public Model train(Configuration config, List<Document> docs) {
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
		LOGGER.info("Model is trained");
		mapPubmedIdToMalletId(docs, model);
		LOGGER.info("PubMed citation IDs (PMIDs) are mapped to Mallet document IDs");
		return model;
	}
	
	public void saveModel(Model model, File file) {
		ParallelTopicModel newModel = model.malletModel;
		newModel.write(file);
		LOGGER.info("Model is saved in " + file.getName());
	}
	
	public List<Document> readDocuments(File file) {
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
			for (int i = 0; i < xmlFiles.length; i++) {
				LOGGER.info("Attempt to read " + xmlFiles[i].getName() + ", no. " + (i + 1)  
							+ " of total " + xmlFiles.length);
				List<Document> docsFileI = readDocuments(xmlFiles[i]);
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
	
	//User
	public TMSearchResult search(Query query, Model model) {
		TMSearchResult result = new TMSearchResult();
		return result;
	}
	
	public Map<String, List<Topic>> inferLabel(Document doc, Model model) {
		int numIterations = xmlConfig.getInt("infer.parameters.parameter.numIterations");
		int thinning = xmlConfig.getInt("infer.parameters.parameter.savingInterval");
		int burnIn = xmlConfig.getInt("infer.parameters.parameter.firstSavingInterval");
		int topicWordsDisplayed = xmlConfig.getInt("infer.parameters.parameter.topicWordsDisplayed");
		Map<String, List<Topic>> result = new HashMap<String, List<Topic>>();
		ParallelTopicModel malletModel = model.malletModel;
		TopicInferencer inferencer = malletModel.getInferencer();
		
		List<Document> docs = new ArrayList<Document>();
		docs.add(doc);
		InstanceList instances = preprocess(docs);
		Instance instance = instances.get(0);
		
		double[] distribution = inferencer.getSampledDistribution(
				instance, numIterations, thinning, burnIn);
		List<Topic> topics = new ArrayList<Topic>();
		for (int i = 0; i < distribution.length; i++) {
			Topic topic = new Topic();
			topic.probability = distribution[i];
			Object[][] topicWords = malletModel.getTopWords(topicWordsDisplayed);
			topic.id = i;
			topic.topicWords = topicWords[i];
			topics.add(topic);
		}
		result.put(doc.id, topics);
		return result;
	}
	
	public Model readModel(File file) {
		Model model = new Model();
		try {
			ParallelTopicModel malletParallelTopicModel = ParallelTopicModel.read(file);
			model.malletModel = malletParallelTopicModel;
		} catch (Exception e) {
			e.printStackTrace();
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
			for (int i = 0; i < docs.size(); i++) {
				String sentences = docs.get(i).text;
				if (sentences != null) {
					JCas jCas = JCasFactory.createJCas();
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("JCoRe preprocessing finished");
		return allLemmata;
	}
	
	public InstanceList malletPreprocess(List<TokenSequence> data) {
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		pipeList.add( new TokenSequenceRemoveStopwords(false, false));
		pipeList.add( new TokenSequenceRemoveNonAlpha() );
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
			String lemmaString = lemma.getValue();
			lemmata.add(lemmaString);
			tokenIterator.next();
		}
		return lemmata;
	}
	
	public void mapPubmedIdToMalletId(List<Document> docs, Model model) {
		model.pubmedIdModelId = new HashMap<String, Object>();
		for (int i = 0; i < docs.size(); i++) {
			Document doci = docs.get(i);
			String dociId = doci.id;
			LOGGER.info("Attempting to map PMID " + dociId + " to mallet doc " + i);
			model.pubmedIdModelId.put(dociId, i);
		}
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
}
