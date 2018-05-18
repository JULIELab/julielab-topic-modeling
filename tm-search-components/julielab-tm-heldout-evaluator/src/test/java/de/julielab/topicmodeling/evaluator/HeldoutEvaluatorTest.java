package de.julielab.topicmodeling.evaluator;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class HeldoutEvaluatorTest {

	public HeldoutEvaluatorTest() {
		
	}
	
	@Test
	public void testEvaluate() {
		String[] args = new String[6];
		args[0] = "src/test/resources/heldout_docs/pubmed18n0740_7_eval_files.xml";
		args[1] = "src/test/resources/test_topic_model.ser";
		args[2] = "50";
		args[3] = "true";
		args[4] = "src/test/resources/likelihood_test";
		args[5] = "src/test/resources/config_template.xml";
		HeldoutEvaluator.main(args);
		File likelihood = new File ("src/test/resources/likelihood_test");
		assertTrue(likelihood.exists());
	}
	
//	@Test
//	public void getDocs() throws ConfigurationException {
//		String[] args = new String[6];
//		args[0] = "src/test/resources/heldout_docs/pubmed18n0740.xml";
//		args[5] = "src/test/resources/config_template.xml";
//		MalletTopicModeling tm = new MalletTopicModeling();
//		XMLConfiguration xmlConfig = tm.loadConfig(args[5]);
//		List<Document> docs = tm.readDocuments(new File(args[0]), xmlConfig);
//		for (int i = 0; i < 176; i++) {
//			System.out.println("Doc text #" + i + ": " + docs.get(i).text);	
//			System.out.println(docs.size());
//		}
//	}
}
