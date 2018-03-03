package de.julielab.interfaces;

import java.util.ArrayList;

public interface IModel {
	
	Object[][] getTopics(IModel model);
	
	ArrayList<Object> getDocumentTopics(IModel model);
	
	IResult computeRanking(IQuery query, Object[][] topics, ArrayList<Object> DocumentTopics);
	
	IResult setResults();
}
