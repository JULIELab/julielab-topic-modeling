
package de.julielab.jcore.consumer.topiclabel;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.jcore.types.Annotation;
import de.julielab.jcore.types.Header;
import de.julielab.jcore.types.Lemma;
import de.julielab.jcore.types.Token;
import de.julielab.jcore.types.Topics;

public class TopicLabelConsumer extends  CasConsumer_ImplBase {

	private final static Logger log = LoggerFactory.getLogger(TopicLabelConsumer.class);
	
	/**
	 * This method is called a single time by the framework at component
	 * creation. Here, descriptor parameters are read and initial setup is done.
	 */
	@Override
	public void initialize() throws ResourceInitializationException {
		// TODO
	}

	/**
	 * This method is called for each document going through the component. This
	 * is where the actual work happens.
	 */
	@Override
	public void processCas(final CAS aJCas) throws AnalysisEngineProcessException {
		// TODO
//		Topics topicsFeatures = (Topics) aJCas.getDocumentAnnotation();
//		IntegerArray ids = topicsFeatures.getIDs();
//		DoubleArray weights = topicsFeatures.getWeights();
//		StringArray topicWords = topicsFeatures.getTopicWords();
//		String modelId = topicsFeatures.getModelID();
//		String modelVersion = topicsFeatures.getModelVersion();
//		
//		Header header = (Header) aJCas.getAnnotationIndex();
	}
}
