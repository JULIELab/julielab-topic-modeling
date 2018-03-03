package de.julielab.UsingModel;

import java.io.File;
import java.util.ArrayList;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.LabelAlphabet;

import de.julielab.interfaces.IModel;
import de.julielab.interfaces.IQuery;
import de.julielab.interfaces.IResult;


public class ModelProperties implements IModel{

	public ModelProperties(String modelFileDirectory) throws Exception {
		File byteModel = new File(modelFileDirectory);
		ParallelTopicModel model = ParallelTopicModel.read(byteModel);
	}
	
	public ModelProperties(ParallelTopicModel malletModel) throws Exception {
		ParallelTopicModel model = malletModel;
	}

	@Override
	public IResult setResults() {
		IResult results;
		return results;
	}
	
	@Override
	public Object[][] getTopics(IModel model) {
		Object[][] topics;
		return topics;
	}
	
	@Override
	public ArrayList<Object> getDocumentTopics(IModel model) {
		ArrayList<Object> documentTopics;
		return documentTopics;
	}
	
	@Override
	public IResult computeRanking(IQuery query, Object[][] topics, ArrayList<Object> documentTopics) {
		IResult result;
		return result;
	}

//	public static void main(String[] args) throws Exception {
//		String modelFileDirectory = args[0];
//		File byteModel = new File(modelFileDirectory);
//		ParallelTopicModel topicModelTest = ParallelTopicModel.read(byteModel);
////		File outputDocumentTopics = new File(args[1]);
////		PrintWriter outDocumentTopics = new PrintWriter(outputDocumentTopics);
//////		File outputTopicDocuments = new File(args[2]);
//////		PrintWriter outTopicDocuments = new PrintWriter(outputTopicDocuments);
////		File outputDensity = new File(args[2]);
////		PrintWriter outDensity = new PrintWriter(outputDensity);
//////		Alphabet alphabet = topicModelTest.getAlphabet();
//////		System.out.println(alphabetString);
////		
//////		topicModelTest.printDenseDocumentTopics(out);
////		System.out.println("Model name: " + modelFileDirectory);
////		System.out.println("\n");
////		topicModelTest.printDocumentTopics(outDocumentTopics);
//////		topicModelTest.printTopicDocuments(outTopicDocuments);
////		topicModelTest.printDenseDocumentTopics(outDensity);
//		
////		Alphabet tmAlphabet = topicModelTest.getAlphabet();
////		for  (int i = 0; i < 10; i++) {
////			Object alphabet = tmAlphabet.lookupObject(i);
////			System.out.println("Data Alphabet (i.e. text, vocabulary) at index " + i + ": " +  alphabet);	
////		}
////		
////		LabelAlphabet topics = topicModelTest.topicAlphabet;
////		Object topic = topics.lookupObject(1);
////		for  (int i = 0; i < 10; i++) {
////			topic = topics.lookupObject(i);
////			System.out.println("Label alphabet (i.e. topics) at index " + i + ": " + topic);	
////		}
////		
////		Object[] topicArray = topics.toArray();
////		for (int i = 0; i < topicArray.length; i++){
////			System.out.println("Topic at array index " + i + ": " + topicArray[i]);
////		}
////		
////		
////		int index = topics.lookupIndex(topic);
////		System.out.println("Index at topic1: " + index);
////		Label label = topics.lookupLabel(topic);
//		
//		Object[][] topicWords = topicModelTest.getTopWords(topicModelTest.numTypes);
//		for (int i = 0; i < topicWords.length; i++){
//			System.out.println("Topic " + i);
//			for (int j = 0; j < 15; j++){
//				System.out.println("Word " + j + ": " + topicWords[i][j]);
//				System.out.println("Class: " + topicWords[i][j].getClass());
//			}
//			System.out.println("\n"); 
//		}
//		int[] tokensPerTopic = topicModelTest.getTokensPerTopic();
//		System.out.println("Tokens per topic: " + tokensPerTopic[0]);
//	}
	
}
