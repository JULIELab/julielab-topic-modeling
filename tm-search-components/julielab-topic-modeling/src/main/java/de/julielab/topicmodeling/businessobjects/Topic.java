package de.julielab.topicmodeling.businessobjects;

public class Topic implements java.io.Serializable {

	static final long serialVersionUID = 1;
	
	public double probability;
	public Object[] topicWords;
	public int id;
	
	public String modelId;
	public String modelVersion;
	
	public Topic() {
		// TODO Auto-generated constructor stub
	}

}
