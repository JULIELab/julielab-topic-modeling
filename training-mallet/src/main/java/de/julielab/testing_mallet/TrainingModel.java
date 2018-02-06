// modified version taken from testing-mallet version 1.1.0

package de.julielab.testing_mallet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;

public class TrainingModel {
	
		private String logData = "";

		public TrainingModel(InstanceList instances, int numTopics, double alphaSum, double beta, 
				int trainingIterations, int optimizeInterval, int numberOfThreads, String modelFileDirectory) 
				throws IOException {
			
			ParallelTopicModel model = new ParallelTopicModel(numTopics, alphaSum, beta);

			model.addInstances(instances);			 
			
			// Use two parallel samplers, which each look at one half the corpus and combine
			//  statistics after every iteration.
			model.setNumThreads(numberOfThreads);

			// Run the model for 50 iterations and stop (this is for testing only, 
			//  for real applications, use 1000 to 2000 iterations)
			model.setNumIterations(trainingIterations);
			model.setOptimizeInterval(optimizeInterval);
			model.estimate();

			// Show the words and topics in the first instance

			// The data alphabet maps word IDs to strings
			Alphabet dataAlphabet = instances.getDataAlphabet();
			
			FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
			LabelSequence topics = model.getData().get(0).topicSequence;
			
			Formatter tokenOut = new Formatter(new StringBuilder(), Locale.US);
			for (int position = 0; position < tokens.getLength(); position++) {
				tokenOut.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
			}
			tokenOut.close();
//			System.out.println(tokenOut);
			
			// Estimate the topic distribution of the first instance, 
			//  given the current Gibbs state.
			double[] topicDistribution = model.getTopicProbabilities(0);

			// Get an array of sorted sets of word ID/count pairs
			ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
			
			// Show top 5 words in topics with proportions for the first document
			String logTopic = "";
			logTopic = logTopic.concat("Topic-ID" + "\t" + "Topic weights" + "\t" + "Topics [token count or index position?]" + "\n");
			for (int topic = 0; topic < numTopics; topic++) {
				Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
				
				Formatter topicOut = new Formatter(new StringBuilder(), Locale.US);
				topicOut.format("%d\t%.3f\t", topic, topicDistribution[topic]);
				int rank = 0;
				while (iterator.hasNext() && rank < 5) {
					IDSorter idCountPair = iterator.next();
					topicOut.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
					rank++;
				}
				logTopic = logTopic.concat(topicOut.toString() + "\n");
				System.out.println(topicOut);
			}
			logData = logData.concat(logTopic);
		
			// Create a new instance with high probability of topic 0
			StringBuilder topicZeroText = new StringBuilder();
			Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();

			int rank = 0;
			while (iterator.hasNext() && rank < 5) {
				IDSorter idCountPair = iterator.next();
				topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
				rank++;
			}

			// Create a new instance named "test instance" with empty target and source fields.
			InstanceList testing = new InstanceList(instances.getPipe());
			testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));

			TopicInferencer inferencer = model.getInferencer();
			double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
			System.out.println("0\t" + testProbabilities[0]);
			Object testInstance = testing.get(0).getName();
			logData = logData.concat("\n" + testInstance + "\n");
			logData = logData.concat("0\t" + testProbabilities[0]);

			// Write model into file 
			File modelFile = new File (modelFileDirectory);
			model.write(modelFile);
			
//			model.printDocumentTopics(new File("D:/DocumentTopics.txt"));
//			PrintStream ps = new PrintStream(new FileOutputStream ("D:/State.txt"));
//			model.printState(ps);
//			model.printTopicWordWeights(new File("D:/TopicWordWeights.txt"));
//			model.printTopWords(new File("D:/TopWords.txt"), 50, true);
//			model.printTypeTopicCounts(new File("D:/TypeTopicCounts.txt"));
		}
		
		public String getLogData() {
			return logData;
		}
}
