
package de.julielab.jcore.consumer.topiclabel;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.jcore.types.Topics;
import de.julielab.jcore.types.pubmed.Header;
import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.Topic;
import de.julielab.topicmodeling.services.MalletTopicModeling;

public class TopicLabelConsumer extends  CasConsumer_ImplBase {

	private final static Logger log = LoggerFactory.getLogger(TopicLabelConsumer.class);
	
	public static final String PARAM_TOPIC_MODEL_CONFIG = "TopicModelConfig";
	
	public static final String PARAM_TOPIC_MODEL_FILE = "TopicModelFile";
	
	@ConfigurationParameter(name = PARAM_TOPIC_MODEL_CONFIG, mandatory = true)
    private File model_config;
    @ConfigurationParameter(name = PARAM_TOPIC_MODEL_FILE, mandatory = true)
    private String model_file;
    MalletTopicModeling tm;
	File modelFile;
	public XMLConfiguration xmlConfig;
	
	/**
	 * This method is called a single time by the framework at component
	 * creation. Here, descriptor parameters are read and initial setup is done.
	 */
	@Override
	public void initialize() throws ResourceInitializationException {
		try {
			tm = new MalletTopicModeling(PARAM_TOPIC_MODEL_CONFIG);
			modelFile = new File(PARAM_TOPIC_MODEL_FILE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is called for each document going through the component. This
	 * is where the actual work happens.
	 */
	@Override
	public void processCas(final CAS aJCas) throws AnalysisEngineProcessException {
		Model savedModel = tm.readModel(modelFile);
		populateModelIndex(savedModel, aJCas);
	}
	
	public void populateModelIndex(Model model, CAS cas) {
		HashMap<Document, List<Topic>> index = model.index;
		int topicWordsDisplayed = xmlConfig.getInt("infer.parameters.parameter.topicWordsDisplayed");
		try {
			Topics topicsFeatures = (Topics) cas.getDocumentAnnotation();
			IntegerArray ids = topicsFeatures.getIDs();
			DoubleArray weights = topicsFeatures.getWeights();
			StringArray topicWords = topicsFeatures.getTopicWords();
			String modelId = topicsFeatures.getModelID();
			String modelVersion = topicsFeatures.getModelVersion();
			List<Topic> topics = new ArrayList<>();
			for (int i = 0; i < weights.size(); i++) {
				Topic topic = new Topic();
				topic.probability = weights.get(i);
				topic.id = ids.get(i);
				for (int k = 0; k < topicWordsDisplayed; k++) {
					String topicWord = topicWords.get(k);
					topic.topicWords[i] = topicWord; 
				}
				topic.modelId = modelId;
				topic.modelVersion = modelVersion;
				topics.add(topic);
			}	
			Document doc = new Document();
			Header header = (Header) cas.getAnnotationIndex();
			doc.id = header.getDocId();
			index.put(doc, topics);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
