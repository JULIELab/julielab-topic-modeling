package de.julielab.topicmodeling.businessobjects;

import java.util.HashMap;
import java.util.List;

import cc.mallet.topics.ParallelTopicModel;

public class Model implements java.io.Serializable {

	static final long serialVersionUID = 0;
	
	public ParallelTopicModel malletModel;
	public HashMap<String, Integer> pubmedIdModelId;
	public HashMap<Integer, String> ModelIdpubmedId;
	public String modelId;
	public String modelVersion;
	public HashMap<String, List<Topic>> index;
	
	public Model() {
		// TODO Auto-generated constructor stub
	}

}
