package de.julielab.topicmodeling.services;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import de.julielab.topicmodeling.businessobjects.Configuration;
import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.Query;
import de.julielab.topicmodeling.businessobjects.TMSearchResult;
import de.julielab.topicmodeling.businessobjects.Topic;
import de.julielab.xml.JulieXMLConstants;
import de.julielab.xml.JulieXMLTools;

public class MalletTopicModeling implements ITopicModeling {

	XMLConfiguration xmlConfig;
	
	public MalletTopicModeling(String configFile) throws ConfigurationException {
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<XMLConfiguration> builder =
		    new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class)
		    .configure(params.xml()
		        .setFileName(configFile));
		XMLConfiguration xmlConfig = builder.getConfiguration();
	}

	//Trainer
	public Model train(Configuration config, Document docs) {
		Model model = new Model();
		return model;		
	}
	
	public void saveModel(Model model, File file) {
		
	}
	
	public List<Document> readDocuments(File file) {
		List<Document> docsWithId = readIds(file);
		List<Document> docsWithIdAndText = readTexts(file, docsWithId);
		return docsWithIdAndText;
////		String itemsLog = "JULIELab-TM-LOG: Number of found items in " 
////											+ fileName + ": " 
////											+ foundCount + "\n";
////		
////		String logData = "";
////		logData = logData.concat(itemsLog);
	}
		
	//helper
	public List<Document> readIds(File file) {
		String fileName = file.getAbsolutePath();
	
		String forEachIdPath = xmlConfig.getString("train.file.id.iteratePath");
		String idField = xmlConfig.getString("train.file.id.idField");
		
		String[] fieldPaths = new String [2];
		fieldPaths[0] = idField;
		List<Map<String, String>> fields = new ArrayList<>();
		for (int i = 0; i < fieldPaths.length; i++) {
			String path = fieldPaths[i];
			Map<String, String> field = new HashMap<String, String>();
			field.put(JulieXMLConstants.NAME, "fieldvalue" + i);
			field.put(JulieXMLConstants.XPATH, path);
			fields.add(field);
		}
		Iterator<Map<String, Object>> idRowIterator = JulieXMLTools.constructRowIterator(
			fileName, 1024, forEachIdPath, fields, false);
		
		List<Document> docs = new ArrayList<Document>();
		Document doc = new Document();
//		int foundCount = 0;
		while (idRowIterator.hasNext()) {
			Map<String, Object> row = idRowIterator.next();
			List<String> rowValues = new ArrayList<>();
			for (int i = 0; i < fieldPaths.length; i++) {
				String value = (String) row.get("fieldvalue" + i);
				rowValues.add(value);
				if (value != null) {
//					foundCount++;
					doc.ID = value;
					docs.add(doc);
				}
			}
		}
		return docs;
	}
	
	public List<Document> readTexts(File file, List<Document> docsWithId) {
		String fileName = file.getAbsolutePath();
	
		String forEachTextPath = xmlConfig.getString("train.file.text.iteratePath");
		String textField = xmlConfig.getString("train.file.text.textField");
		String alternativeTextField = xmlConfig.getString("train.file.text.alternativeTextField");
		
		String[] fieldPaths = new String [2];
		fieldPaths[0] = textField;
		fieldPaths[1] = alternativeTextField;
		List<Map<String, String>> fields = new ArrayList<>();
		for (int i = 0; i < fieldPaths.length; i++) {
			String path = fieldPaths[i];
			Map<String, String> field = new HashMap<String, String>();
			field.put(JulieXMLConstants.NAME, "fieldvalue" + i);
			field.put(JulieXMLConstants.XPATH, path);
			fields.add(field);
		}
		Iterator<Map<String, Object>> textRowIterator = JulieXMLTools.constructRowIterator(
			fileName, 1024, forEachTextPath, fields, false);
		
//		int foundCount = 0;
		while (textRowIterator.hasNext()) {
			Map<String, Object> row = textRowIterator.next();
			List<String> rowValues = new ArrayList<>();
			for (int i = 0; i < fieldPaths.length; i++) {
				String value = (String) row.get("fieldvalue" + i);
				rowValues.add(value);
				if (value != null) {
//					foundCount++;
					docsWithId.get(i).text = value;
				}
			}
		}
		return docsWithId;
	}
	
	public List<Document> readMeshTerms(File file, List<Document> docsWithId) {
		//ToDo: implement this method
		return docsWithId;
	}
	
	//User
	public TMSearchResult search(Query query, Model model) {
		TMSearchResult result = new TMSearchResult();
		return result;
	}
	
	public Map<String, List<Topic>> label(Document docs, Model model) {
		Map<String, List<Topic>> result = null;
		return result;
	}
	
	public Model readModel(File file) {
		Model model = new Model();
		return model;
	}
}
