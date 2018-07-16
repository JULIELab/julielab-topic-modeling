package de.julielab.topicmodeling.businessobjects;

public class Topic implements java.io.Serializable {

	static final long serialVersionUID = 1;
	
	public double probability;
	/**
	 * @deprecated it doesn't seem very practical to carry the topic words around especially since they need to be
	 * compute quite costly. From now on we test the approach of offering a separate possibility to get the words per
	 * topic (which is an output of the training, if activated)
	 */
	public Object[] topicWords;
	public int id;
	
	public String modelId;
	public String modelVersion;
	
	public Topic() {
		// TODO Auto-generated constructor stub
	}

}
