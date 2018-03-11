package de.julielab.topicmodeling.services;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import cc.mallet.topics.ParallelTopicModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import de.julielab.topicmodeling.businessobjects.Configuration;
import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.Topic;
import de.julielab.topicmodeling.services.MalletTopicModeling;

public class MalletTopicModelingTest {

	//Test Reader
	@Test
	public void testReadDocumentsFromFolder() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template.xml");
		File file = new File("src/test/resources/test_folder_data/");
		List<Document> docs = tm.readDocuments(file);
		assertEquals(177, docs.size());
	}
	
	@Test
	public void testReadDocumentsNotEmpty() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template.xml");
		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		assertNotEquals(0, docs.size());
	}
	
	@Test
	public void testReadDocumentsFirstThreeDocs() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template.xml");
		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
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
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template.xml");
		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
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
		MalletTopicModeling tm = new MalletTopicModeling("src/test/resources/config_template.xml");
		Configuration config = new Configuration();
		
		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		Model model = tm.train(config, docs);
		ParallelTopicModel malletParallelTopicModel = model.malletModel;
		// actually there are 177 PMIDs found and only 96 abstract texts in the file!
		// TO DO: find out why there are 112 documents (more than abstract texts!) attached by topics 
		assertEquals(112, malletParallelTopicModel.getDocumentTopics(false, false).length);
		Object[][] topicWords = malletParallelTopicModel.getTopWords(malletParallelTopicModel.numTypes);
		assertEquals(100, topicWords.length);
		for (int i = 0; i < topicWords.length; i++){
			System.out.println("Topic " + i);
			for (int j = 0; j < topicWords[i].length; j++){
				System.out.println("Word " + j + ": " + topicWords[i][j]);
			}
		}
	}
	
	@Test
	public void testSaveModelReadModel() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling("src/test/resources/config_template.xml");
		Configuration config = new Configuration();
		
		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		Model model = tm.train(config, docs);
		
		File modelFile = new File("src/test/resources/test_model");
		tm.saveModel(model, modelFile);
		Model savedModel = tm.readModel(modelFile);
		ParallelTopicModel savedMalletModel = savedModel.malletModel;
		Object[][] topicWords = savedMalletModel.getTopWords(savedMalletModel.numTypes);
		assertEquals(100, topicWords.length);
	}
	
	@Test
	public void testNoOptimization() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template_no_optimization.xml");
		Configuration config = new Configuration();
		
		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		Model model = tm.train(config, docs);
		int malletOptimizationSetting = model.malletModel.optimizeInterval;
		double malletAlphaSumSetting = model.malletModel.alphaSum;
		double malletBetaSetting = model.malletModel.beta;
		assertTrue(malletOptimizationSetting == 0);
		assertTrue(malletAlphaSumSetting == 1.0);
		assertTrue(malletBetaSetting == 0.1);
	}
	
	@Test
	public void testMapPubmedIdToMalletId() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template_no_optimization.xml");
		Configuration config = new Configuration();
		
		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		Model model = tm.train(config, docs);
		tm.mapPubmedIdToMalletId(docs, model);
		HashMap<String, Object> map = model.pubmedIdModelId;
		assertEquals(map.get("1669026"), 1);
		assertEquals(map.get("28647898"), 170);
		assertEquals(map.get("29049350"), 167);
		Collection<Object> malletIds = map.values();
		Object[] malletIdsArray = malletIds.toArray();
		for (int i = 0; i < malletIdsArray.length; i++){
			int malletId = (int) malletIdsArray[i];
			if (malletId == 176) {
				assertEquals(malletId, map.get("23203896"));
				break;
			}
		}
	}
	
	@Test
	public void testMapPubmedIdToMalletIdFromFolder() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template_no_optimization.xml");
		Configuration config = new Configuration();
		
		File file = new File("src/test/resources/test_folder_data/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		Model model = tm.train(config, docs);
		tm.mapPubmedIdToMalletId(docs, model);
		HashMap<String, Object> map = model.pubmedIdModelId;
		assertEquals(map.get("1669026"), 1);
		assertEquals(map.get("28647898"), 170);
		assertEquals(map.get("29049350"), 167);
		Collection<Object> malletIds = map.values();
		Object[] malletIdsArray = malletIds.toArray();
		for (int i = 0; i < malletIdsArray.length; i++){
			int malletId = (int) malletIdsArray[i];
			if (malletId == 176) {
				assertEquals(malletId, map.get("23203896"));
				break;
			}
		}
	}
	
	@Test
	public void testInferLabel() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template.xml");
		
		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		File modelFile = new File("src/test/resources/test_model");
		Model savedModel = tm.readModel(modelFile);
		for (int i = 0; i < docs.size(); i++) {
			Document doc = docs.get(i);
			if (doc.text != null ) {
				Map<String, List<Topic>> result = tm.inferLabel(docs.get(i), savedModel);
				System.out.println(docs.get(i).id);
				List<Topic> topics = result.get(doc.id);
				if (topics != null) {
					for (int j = 0; j < topics.size(); j++) {
						System.out.println("Topic " + j + ": " + topics.get(j).probability);
						for (int k = 0; k < topics.get(j).topicWords.length; k++) {
							System.out.println(topics.get(j).topicWords[k]);
						}
					}
				}
			}
			System.out.println("\n");
		}
	}
	
//	@Test
//	public void testBuildTopicsFromModel() throws ConfigurationException {
//		MalletTopicModeling tm = new MalletTopicModeling(
//				"src/test/resources/config_template.xml");
//		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
//		List<Document> docs = tm.readDocuments(file);
//		File modelFile = new File("src/test/resources/test_model");
//		Model savedModel = tm.readModel(modelFile);
//		
//		InstanceList instances = tm.preprocess(docs);
//		List<Topic> topics = tm.buildTopicsFromModel(savedModel, instances);
//		for (int i = 0; i < topics.size(); i++) {
//			Topic topic = topics.get(i);
//			System.out.println(topic.topicWords);
//			assertEquals(topic.id, i);
//		}
//	}
}
