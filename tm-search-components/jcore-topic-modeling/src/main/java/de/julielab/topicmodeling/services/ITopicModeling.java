package de.julielab.topicmodeling.services;

import java.io.File;
import java.util.List;
import java.util.Map;

import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.TMSearchResult;
import de.julielab.topicmodeling.businessobjects.Topic;

public interface ITopicModeling {

	//Trainer
	Model train(List<Document> docs);
	
	void saveModel(Model model, String filename);
	
	List<Document> readDocuments(File file);
	
	//User
	TMSearchResult search(Document query, Model model);
	
	Map<String, List<Topic>> inferLabel(Document docs, Model model);
	
	Model readModel(String filename); 
}