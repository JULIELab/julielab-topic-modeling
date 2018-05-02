
package de.julielab.jcore.ae;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.mallet.types.TokenSequence;
import de.julielab.jcore.types.Topics;
import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.Topic;
import de.julielab.topicmodeling.services.MalletTopicModeling;

public class TopicLabeler extends JCasAnnotator_ImplBase {

	private final static Logger log = LoggerFactory.getLogger(TopicLabeler.class);
	
	public static final String PARAM_TOPIC_MODEL_CONFIG = "TopicModelConfig";
	
	public static final String PARAM_TOPIC_MODEL_FILE = "TopicModelFile";
	
	
	@ConfigurationParameter(name = PARAM_TOPIC_MODEL_CONFIG, mandatory = true)
    private File model_config;
    @ConfigurationParameter(name = PARAM_TOPIC_MODEL_FILE, mandatory = true)
    private String model_file;
    MalletTopicModeling tm;
    File modelFile;
    
    
	/**
	 * This method is called a single time by the framework at component
	 * creation. Here, descriptor parameters are read and initial setup is done.
	 */
	@Override
	public void initialize(final UimaContext aContext) throws ResourceInitializationException {
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
	public void process(final JCas aJCas) throws AnalysisEngineProcessException {
		try {
			int topicWordsDisplayed = tm.xmlConfig.getInt("infer.parameters.parameter.topicWordsDisplayed");
			TokenSequence docLemmata = tm.getLemmata(aJCas);
			String id = tm.getId(aJCas);
			Document doc = new Document();
			doc.preprocessedData = docLemmata;
			doc.id = id;
			Model savedModel = tm.readModel(modelFile);
			String modelID = savedModel.modelId;
			String modelVersion = savedModel.modelVersion;
//			HashMap<Integer, Double> result = tm.inferTopicWeightLabel(doc, savedModel);
//			Map<String, List<Topic>> wordResult = tm.inferTopicWordLabel(doc, savedModel);
			Map<String, List<Topic>> result = tm.inferLabel(doc, savedModel);
			DoubleArray topicWeights = new DoubleArray(aJCas, result.size());
			IntegerArray topicIds = new IntegerArray(aJCas, result.size());
			StringArray topicWords = new StringArray(aJCas, result.size());
			log.info("Labeled document " + id);
			for (int i = 0; i < result.size(); i++) {
				double topicWeight = result.get(doc.id).get(i).probability;
				int topicId = result.get(doc.id).get(i).id;
				for (int k = 0; k < topicWordsDisplayed; k++){
					String topicWord = (String) result.get(doc.id).get(i).topicWords[k];
					topicWords.set(k, topicWord);
				}
				topicWeights.set(i, topicWeight);
				topicIds.set(i, topicId);
			}
			Topics documentTopics = new Topics(aJCas);
			documentTopics.setIDs(topicIds);
			documentTopics.setWeights(topicWeights);
			documentTopics.setModelID(modelID);
			if (modelVersion != "") {
				documentTopics.setModelVersion(modelVersion);
			}
			documentTopics.setTopicWords(topicWords);
			aJCas.addFsToIndexes(documentTopics);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
