package de.julielab.topicmodeling.businessobjects;

import java.util.HashMap;

import cc.mallet.topics.ParallelTopicModel;

public class Model {

	//hier unsicher, ob der Import von dem konkreten Typ ParallelTopicModel sinnvoll ist
	public ParallelTopicModel malletModel;
	public HashMap<String, Integer> pubmedIdModelId;
	public HashMap<Integer, String> ModelIdpubmedId;
	
	public Model() {
		// TODO Auto-generated constructor stub
	}

}
