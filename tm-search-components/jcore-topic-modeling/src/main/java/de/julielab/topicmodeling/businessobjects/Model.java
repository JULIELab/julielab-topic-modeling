package de.julielab.topicmodeling.businessobjects;

import java.util.HashMap;
import java.util.List;

import cc.mallet.topics.ParallelTopicModel;

public class Model {

	//hier unsicher, ob der Import von dem konkreten Typ ParallelTopicModel sinnvoll ist
	public ParallelTopicModel malletModel;
	public HashMap<String, Integer> pubmedIdModelId;
	public HashMap<Integer, String> ModelIdpubmedId;
	public String modelId;
	public String modelVersion;
	public HashMap<Document, List<Topic>> index;
	
	public Model() {
		// TODO Auto-generated constructor stub
	}

}
