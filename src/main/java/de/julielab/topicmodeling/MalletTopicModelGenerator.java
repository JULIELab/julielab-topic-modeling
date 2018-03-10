package de.julielab.topicmodeling;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.mallet.topics.ParallelTopicModel;
import de.julielab.topicmodeling.businessobjects.Configuration;
import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.services.MalletTopicModeling;

public class MalletTopicModelGenerator {

	Configuration uselessConfig = new Configuration();
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MalletTopicModelGenerator.class);
	
	public MalletTopicModelGenerator() {
		
	}

	public static void main(String[] args) throws ConfigurationException {
		System.setProperty("logback.configurationFile", "src/main/resources/logback-complex.xml");
//		SLF4JBridgeHandler.removeHandlersForRootLogger();
//		SLF4JBridgeHandler.install();
//		java.util.logging.Logger.getLogger("cc.mallet.topics.ParallelTopicModel").setLevel(Level.INFO);
		try {
			MalletTopicModelGenerator generator = new MalletTopicModelGenerator();
			LOGGER.info("MALLeT-topic-model-generator started with"
						+ " config " + args[0] 
						+ " with data file location " + args[1] 
						+ ", and model will be written in file " + args[2] );
			Model model = generator.generateTopicModel(args[0], args[1], args[2]);
			if (args.length == 4) {
				if (args[3].equals("verify")) {
					generator.verifyModel(args[2], args[0]);
				} else {
					File topicsFile = new File(args[3]); 
					generator.printTopicsToFile(topicsFile, model);
				}
			}
			if (args.length == 5) {
				if (args[3].equals("verify")) {
					generator.verifyModel(args[2], args[0]);
				} 
				File topicsFile = new File(args[4]); 
				generator.printTopicsToFile(topicsFile, model);
			}
		} catch (Exception e) {
			System.out.println("Usage: \n"
								+ "Obligatory arguments: [0]<configuration file path>, "
								+ "[1]<file or folder path to documents to be modelled>, "
								+ "[2]<newly generated model file path> \n"
								+ "Optional arguments: "
								+ "[3]verify (verifies the model after generating), "
								+ "[3]/[4]<filename for monitoring topics> "
								+ "(prints the topics from the new model in a file)");
		}
	}

	public Model generateTopicModel(String configFileName, String docFilename, String modelFilename) 
			throws ConfigurationException {
		Configuration uselessConfig = new Configuration();
		MalletTopicModeling tm = new MalletTopicModeling(configFileName);
		File docFile = new File(docFilename);
		List<Document> docs = tm.readDocuments(docFile);
		Model model = tm.train(uselessConfig, docs);
		File modelFile = new File(modelFilename);
		tm.saveModel(model, modelFile);
		return model;
	}
	
	public void verifyModel(String modelFilename, String configFileName) throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling(configFileName);
		File modelFile = new File(modelFilename);
		Model model = tm.readModel(modelFile);
		ParallelTopicModel savedMalletModel = model.malletModel;
		Object[][] topicWords = savedMalletModel.getTopWords(savedMalletModel.numTypes);
		if (topicWords.length == tm.xmlConfig.getInt("train.parameters.parameter.numTopics")) {
			LOGGER.info("Topic model verified.");
		} else {
			LOGGER.info("Topic model verification failed.");
		}
	}
	
	public void printTopicsToFile(File file, Model model) throws IOException {
		FileWriter writer = new FileWriter(file);
		BufferedWriter buffWriter = new BufferedWriter(writer);
		ParallelTopicModel malletParallelTopicModel = model.malletModel;
		Object[][] topicWords = malletParallelTopicModel.getTopWords(malletParallelTopicModel.numTypes);
		for (int i = 0; i < topicWords.length; i++){
			buffWriter.write("Topic " + i + "\n");
			for (int j = 0; j < topicWords[i].length; j++){
				buffWriter.write("Word " + j + ": " + topicWords[i][j] + "\n");
			}
			buffWriter.write("\n");
		}
		buffWriter.close();
		LOGGER.info("Topics written in " + file.getAbsolutePath() + ".");
	}
}
