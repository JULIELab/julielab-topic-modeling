package de.julielab.topicmodeling.services;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import cc.mallet.topics.ParallelTopicModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import de.julielab.topicmodeling.businessobjects.Configuration;
import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.services.MalletTopicModeling;

public class MalletTopicModelingTest {

	//Test Reader
	@Test
	public void testReadDocumentsNotEmpty() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling("D:/jUnit_tests/config_template.xml");
		File file = new File("D:/jUnit_tests/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		assertNotEquals(0, docs.size());
	}
	
	@Test
	public void testReadDocumentsFirstThreeDocs() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling("D:/jUnit_tests/config_template.xml");
		File file = new File("D:/jUnit_tests/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		
		Document doc0 = docs.get(0);
		String doc0Text = doc0.text;
		String doc0Id = doc0.id;
		assertEquals(null, doc0Text);
		assertEquals("973217", doc0Id);
		
		Document doc1 = docs.get(1);
		String doc1Text = doc1.text;
		String doc1Id = doc1.id;
		assertEquals(null, doc1Text);
		assertEquals("1669026", doc1Id);
		
		Document doc2 = docs.get(2);
		String doc2Text = doc2.text;
		String doc2Id = doc2.id;
		assertEquals("Modification of the hexahydronaphthalene "
				+ "ring 5-position in simvastatin 2a via oxygenation and oxa "
				+ "replacement afforded two series of derivatives which were "
				+ "evaluated in vitro for inhibition of "
				+ "3-hydroxy-3-methylglutaryl-coenzyme "
				+ "A reductase and acutely in vivo for oral effectiveness as "
				+ "inhibitors of cholesterogenesis in the rat. Of the compounds "
				+ "selected for further biological evaluation, the 6 "
				+ "beta-methyl-5-oxa 10 and 5 alpha-hydroxy 16 derivatives of "
				+ "3,4,4a,5-tetrahydro 2a, as well as, the 6 beta-epimer 14 "
				+ "of 16 proved orally active as hypocholesterolemic agents "
				+ "in cholestyramine-primed dogs. Subsequent acute oral "
				+ "metabolism studies in dogs demonstrated that compounds "
				+ "14 and 16 evoke lower peak plasma drug activity and "
				+ "area-under-the-curve values than does compound 10 and "
				+ "led to the selection of 14 and 16 for toxicological evaluation.",
				doc2Text);
		assertEquals("1875346", doc2Id);
	}
	
	@Test
	public void testReadDocumentsDocCount() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling("D:/jUnit_tests/config_template.xml");
		File file = new File("D:/jUnit_tests/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		
		int docCount = 0;
		for (int i = 0; i < docs.size(); i++) {
			docCount++;
		}
		assertEquals(177, docCount);
	}
	
	//Test Trainer
	@Test
	public void testTrainConfiguration() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling("D:/jUnit_tests/config_template.xml");
//		System.out.println(ConfigurationUtils.toString(tm.xmlConfig));
		
		assertEquals(100, tm.xmlConfig.getInt("train.parameters.parameter.numTopics")); 
		assertEquals(1.0, tm.xmlConfig.getDouble("train.parameters.parameter.alphaSum"), 0.0);
		assertEquals(0.1, tm.xmlConfig.getDouble("train.parameters.parameter.beta"), 0.0);
		assertEquals(1, tm.xmlConfig.getInt("train.parameters.parameter.numThreads"));
		assertEquals(1000, tm.xmlConfig.getInt("train.parameters.parameter.numIterations"));
		assertEquals(50, tm.xmlConfig.getInt("train.parameters.parameter.optimizationInterval"));
	}
	
	@Test
	public void testTrain() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling("D:/jUnit_tests/config_template.xml");
		Configuration config = new Configuration();
		
		File file = new File("D:/jUnit_tests/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		Model model = tm.train(config, docs);
		ParallelTopicModel malletParallelTopicModel = model.malletModel;
		assertEquals(177, malletParallelTopicModel.getDocumentTopics(false, false).length);
		Object[][] topicWords = malletParallelTopicModel.getTopWords(malletParallelTopicModel.numTypes);
		assertEquals(100, topicWords.length);
		for (int i = 0; i < topicWords.length; i++){
			System.out.println("Topic " + i);
			for (int j = 0; j < 15; j++){
				System.out.println("Word " + j + ": " + topicWords[i][j]);
			}
		}
	}
	
	@Test
	public void testSaveModelReadModel() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling("D:/jUnit_tests/config_template.xml");
		Configuration config = new Configuration();
		
		File file = new File("D:/jUnit_tests/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		Model model = tm.train(config, docs);
		
		File modelFile = new File("D:/jUnit_tests/test_model");
		tm.saveModel(model, modelFile);
		Model savedModel = tm.readModel(modelFile);
		ParallelTopicModel savedMalletModel = savedModel.malletModel;
		Object[][] topicWords = savedMalletModel.getTopWords(savedMalletModel.numTypes);
		assertEquals(100, topicWords.length);
	}
	
	@Test
	public void testNoOptimization() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling(
				"D:/jUnit_tests/config_template_no_optimization.xml");
		Configuration config = new Configuration();
		
		File file = new File("D:/jUnit_tests/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		Model model = tm.train(config, docs);
		int malletOptimizationSetting = model.malletModel.optimizeInterval;
		double malletAlphaSumSetting = model.malletModel.alphaSum;
		double malletBetaSetting = model.malletModel.beta;
		assertTrue(malletOptimizationSetting == 0);
		assertTrue(malletAlphaSumSetting == 1.0);
		assertTrue(malletBetaSetting == 0.1);
	}
}
