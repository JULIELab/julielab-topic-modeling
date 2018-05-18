package de.julielab.topicmodeling.evaluator;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.mallet.topics.MarginalProbEstimator;
import cc.mallet.types.InstanceList;
import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.services.MalletTopicModeling;

public class HeldoutEvaluator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MalletTopicModeling.class);
	
	static XMLConfiguration xmlConfig;
	
	private static int numParticles;
	private static boolean usingResampling;
	private static PrintStream ps;
	
	public HeldoutEvaluator() {
	}

	public static void main(String[] args) {
		MalletTopicModeling tm = new MalletTopicModeling();
		
		File inFile = new File(args[0]);
		Model model = tm.readModel(args[1]);
		numParticles = Integer.parseInt(args[2]);
		usingResampling = Boolean.parseBoolean(args[3]);
		File outFile = new File(args[4]);
		
		try {
			xmlConfig = tm.loadConfig(args[5]);
			List<Document> docs = tm.readDocuments(inFile, xmlConfig);
			int numberFiles = xmlConfig.getInt("evaluate.heldout.text.number", docs.size());
			InstanceList instance;
			if (numberFiles != docs.size()) {
				List<Document> docsSample = new ArrayList<>(); 
				for (int i = 0; i < numberFiles; i++){
					docsSample.add(docs.get(i));
				}
				instance = tm.preprocess(docsSample);
			} else {
				instance = tm.preprocess(docs);
			}
			ps = new PrintStream(outFile);
			evaluate(model, instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void evaluate(Model model, InstanceList heldoutDocs) {
		MarginalProbEstimator estimator = model.malletModel.getProbEstimator();
		LOGGER.info("Will evaluate with " + heldoutDocs.size() + " document texts");
		double value = estimator.evaluateLeftToRight(
			heldoutDocs, numParticles, usingResampling, ps);
		LOGGER.info("Total heldout likelihood: " + value);
		LOGGER.info("Average heldout likelihood: " + value/heldoutDocs.size());
	}
}
