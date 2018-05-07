package de.julielab.topicmodeling.services;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.XMLConfiguration;

import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.TMSearchResult;
import de.julielab.topicmodeling.businessobjects.Topic;

public interface ITopicModeling {

	//Trainer
	Model train(List<Document> docs, XMLConfiguration xmlConfig);
	
	void saveModel(Model model, String filename);
	
	List<Document> readDocuments(File file, XMLConfiguration xmlConfig);
	
	//User
	TMSearchResult search(Document query, Model model, XMLConfiguration xmlConfig);
	
	Map<String, List<Topic>> inferLabel(Document docs, Model model, XMLConfiguration xmlConfig);
	
	Model readModel(String filename); 
}
