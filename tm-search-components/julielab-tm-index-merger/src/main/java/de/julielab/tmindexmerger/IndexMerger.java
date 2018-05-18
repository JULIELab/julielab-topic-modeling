package de.julielab.tmindexmerger;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.Topic;
import de.julielab.topicmodeling.services.MalletTopicModeling;

public class IndexMerger {

	public IndexMerger() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		try {
		IndexMerger im = new IndexMerger();
		im.mergeIndexes(args[0], args[1]);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Usage: " + "\n"
								+ "argument [0]: path to the folder "
								+ "that contains the model files to be merged;" + "\n"
								+ "argument [1]: filename the model with merged indexes"
								+ "will be saved in.");
		}
	}

	public void mergeIndexes(String indexesFilesDir, String mergedIndexesFilesName) {
		MalletTopicModeling tm = new MalletTopicModeling(); 
		Model topicModel = new Model();
		HashMap<String, List<Topic>> mergedIndex = new HashMap<>();
		File dir = new File (indexesFilesDir);
		if(dir.isDirectory()) {
			String[] modelFileNames = dir.list();
			for (int i = 0; i < modelFileNames.length; i++) {
				topicModel = tm.readModel(indexesFilesDir + modelFileNames[i]);
				mergedIndex.putAll(topicModel.index); 
			}
			topicModel.index = mergedIndex;
			 
			tm.saveModel(topicModel, mergedIndexesFilesName);
		}
	}
}
