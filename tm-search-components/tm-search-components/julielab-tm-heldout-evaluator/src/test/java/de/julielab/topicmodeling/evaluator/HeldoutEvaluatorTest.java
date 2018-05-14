package de.julielab.topicmodeling.evaluator;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.services.MalletTopicModeling;

public class HeldoutEvaluatorTest {

	public HeldoutEvaluatorTest() {
		
	}
	
	@Test
	public void testEvaluate() {
		String[] args = new String[6];
		args[0] = "src/test/resources/heldout_docs/pubmed18n0740_7_eval_files.xml";
		args[1] = "src/test/resources/model_eval_tm_b";
		args[2] = "50";
		args[3] = "true";
		args[4] = "src/test/resources/likelihood_pm_740_true_50_tm_b";
		args[5] = "src/test/resources/config_template.xml";
		HeldoutEvaluator.main(args);
	}
	
	@Test
	public void getDocs() throws ConfigurationException {
		String[] args = new String[6];
		args[0] = "src/test/resources/heldout_docs/pubmed18n0740.xml";
		args[5] = "src/test/resources/config_template.xml";
		MalletTopicModeling tm = new MalletTopicModeling();
		XMLConfiguration xmlConfig = tm.loadConfig(args[5]);
		List<Document> docs = tm.readDocuments(new File(args[0]), xmlConfig);
		for (int i = 0; i < 176; i++) {
			System.out.println("Doc text #" + i + ": " + docs.get(i).text);	
			System.out.println(docs.size());
		}
	}
}
