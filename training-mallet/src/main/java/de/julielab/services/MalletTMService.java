package de.julielab.services;

import java.util.Iterator;
import java.util.List;

public class MalletTMService implements ITMService {

	public MalletTMService(MalletTopicModelService topicModel) {

	}
	
	LabelledDocument label(List<?> queryResults) {
		List<String> corpus = toCorpus(queryResults);
		ParallelTopicModel newModel = topicModel.model(corpus);
		newModel.labelDocuments(queryResults);
		return labelledDocument;
	}
	
	Result search(String query) {
		Result result;
		return result;
	}

	Model modelFraction(String topic) {
		Model model;
		return model;
	}
}
