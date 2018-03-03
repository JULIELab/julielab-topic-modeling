package de.julielab.interfaces;

public interface ISearchModel {

	void useTopicModel(String mode, IConfig config, IModel model);
	
	boolean existModel ();
	
	String setSearchMode(String mode);
	
	String transformQuery();
	
	IResult getResults();
}