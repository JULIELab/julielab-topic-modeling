// modified version taken from testing-mallet version 1.1.0

package de.julielab.testing_mallet;

import java.io.File;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;

public class UsingModel { 
	
	public static void computeTopicStatistics (String modelFileDirectory) throws Exception {
		File byteModel = new File (modelFileDirectory);
		ParallelTopicModel topicModelTest = ParallelTopicModel.read(byteModel);
		Alphabet alphabet = topicModelTest.getAlphabet();
//		String alphabetString = 
				alphabet.toString();
//		System.out.println(alphabetString);
	}
}