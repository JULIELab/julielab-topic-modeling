package de.julielab.topicmodeling.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveNonAlpha;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import cc.mallet.types.TokenSequence;
import de.julielab.jcore.reader.xml.XMLReader;
import de.julielab.jcore.types.AbstractText;
import de.julielab.jcore.types.Annotation;
import de.julielab.jcore.types.Lemma;
import de.julielab.jcore.types.Token;
import de.julielab.jcore.types.pubmed.Header;
import de.julielab.topicmodeling.businessobjects.Configuration;
import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.Query;
import de.julielab.topicmodeling.businessobjects.TMSearchResult;
import de.julielab.topicmodeling.businessobjects.Topic;

public class MalletTopicModeling implements ITopicModeling {

	XMLConfiguration xmlConfig;
	
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
		ParallelTopicModel malletParallelModel = new ParallelTopicModel(
				xmlConfig.getInt("numTopics"), xmlConfig.getDouble("alphaSum"), 
				xmlConfig.getDouble("beta"));
		Model model = new Model();
		try {
			InstanceList instances = preprocess(docs);
			malletParallelModel.addInstances(instances);			 
			malletParallelModel.setNumThreads(xmlConfig.getInt("numThreads"));
			malletParallelModel.setNumIterations(xmlConfig.getInt("numIterations"));
			malletParallelModel.setOptimizeInterval(xmlConfig.getInt("optimizationInterval"));
			malletParallelModel.estimate();
			model.malletModel = malletParallelModel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}
	
	public void saveModel(Model model, File file) {
		ParallelTopicModel newModel = model.malletModel;
		newModel.write(file);
	}
	
	public List<Document> readDocuments(File file) {
		List<Document> docs = new ArrayList<Document>();
		String pmid = "";
//		String text = "";
		try {
			JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
			CAS cas = jCas.getCas();
			XMLReader pubmedReader = (XMLReader) CollectionReaderFactory.createReader(
					"de.julielab.jcore.reader.xml.desc.jcore-pubmed-reader", 
					XMLReader.PARAM_INPUT_FILE, file);
			while (pubmedReader.hasNext()) {
				pubmedReader.getNext(cas);
				AnnotationIndex<Header> headers = jCas.getAnnotationIndex(Header.type);
				FSIterator<Header> headerIterator = headers.iterator();
				while (headerIterator.hasNext()) {
					Document doc = new Document();
					Header head = headerIterator.get();
					pmid = head.getDocId();
					doc.id = pmid;
					docs.add(doc);
				}
				AnnotationIndex<AbstractText> abstractTexts = jCas.getAnnotationIndex(
						AbstractText.type);
				FSIterator<AbstractText> textIterator = abstractTexts.iterator();
				while (textIterator.hasNext()) {
					AbstractText abstractText = (AbstractText) textIterator.next();
					abstractText.getCoveredText();
//					doc.text = text;
				}
				jCas.reset();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	return docs;
	}
		
	//User
	public TMSearchResult search(Query query, Model model) {
		TMSearchResult result = new TMSearchResult();
		return result;
	}
	
	public Map<String, List<Topic>> inferLabel(Document docs, Model model) {
		Map<String, List<Topic>> result = null;
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
					"de.julielab.jcore.ae.jtbd.desc.jcore-jtbd-ae-biomedical-english");
			for (int i = 0; i < docs.size(); i++) {
				String sentences = docs.get(i).text;
				JCas jCas = JCasFactory.createJCas();
				jCas.setDocumentText(sentences);
				sentenceDetector.process(jCas);
				tokenizer.process(jCas);
				posTagger.process(jCas);
				bioLemmatizer.process(jCas);
				foundLemmata = getLemmata(jCas);
				allLemmata.add(foundLemmata);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allLemmata;
	}
	
	public InstanceList malletPreprocess(List<TokenSequence> data) {
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		pipeList.add( new TokenSequenceRemoveStopwords(false, false));
		pipeList.add( new TokenSequenceRemoveNonAlpha() );
		pipeList.add( new TokenSequence2FeatureSequence() );
		InstanceList instances = new InstanceList (new SerialPipes(pipeList));
		ArrayIterator dataListIterator = new ArrayIterator (data);
		instances.addThruPipe(dataListIterator);
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
}
