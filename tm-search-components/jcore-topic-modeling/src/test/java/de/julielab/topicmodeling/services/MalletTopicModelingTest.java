package de.julielab.topicmodeling.services;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.TMSearchResult;
import de.julielab.topicmodeling.businessobjects.Topic;
import de.julielab.topicmodeling.services.MalletTopicModeling;

public class MalletTopicModelingTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(MalletTopicModelingTest.class);
	
	static Model model = new Model();
	
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
		
		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		model = tm.train(docs);
		ParallelTopicModel malletParallelTopicModel = model.malletModel;
		// actually there are 177 PMIDs found and only 96 abstract texts in the file!
		// TO DO: find out why there are 112 documents (more than abstract texts!) attached by topics 
		assertEquals(112, malletParallelTopicModel.getDocumentTopics(false, false).length);
		Object[][] topicWords = malletParallelTopicModel.getTopWords(malletParallelTopicModel.numTypes);
		assertEquals(100, topicWords.length);
		for (int i = 0; i < topicWords.length; i++){
//			System.out.println("Topic " + i);
			for (int j = 0; j < topicWords[i].length; j++){
				Object topicWord = topicWords[i][j];
				assertEquals(String.class, topicWord.getClass());
//				System.out.println("Word " + j + ": " + topicWords[i][j]);
			}
		}
	}
	
	@Test
	public void testSaveModelReadModel() throws ConfigurationException {
		// TODO: saveModel does not write any file
		MalletTopicModeling tm = new MalletTopicModeling("src/test/resources/config_template.xml");
		
		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		Model model = tm.train(docs);
		
		String filename = "src/test/resources/test_topic_model.ser";
		tm.saveModel(model, filename);
		Model savedModel = tm.readModel("src/test/resources/test_topic_model.ser");
		ParallelTopicModel savedMalletModel = savedModel.malletModel;
		Object[][] topicWords = savedMalletModel.getTopWords(savedMalletModel.numTypes);
		assertEquals("UnitTest", savedModel.modelId);
		assertEquals("0.0", savedModel.modelVersion);
		assertEquals(100, topicWords.length);
	}
	
	@Test
	public void testSaveMalletModelReadMalletModel() throws ConfigurationException {
		// TODO: model fields are alway null
		MalletTopicModeling tm = new MalletTopicModeling("src/test/resources/config_template.xml");
		
//		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
//		List<Document> docs = tm.readDocuments(file);
//		Model model = tm.train(docs);
		
		File modelFile = new File("src/test/resources/test_model");
		tm.saveMalletModel(model, modelFile);
		Model savedModel = tm.readMalletModel(modelFile);
		ParallelTopicModel savedMalletModel = savedModel.malletModel;
		Object[][] topicWords = savedMalletModel.getTopWords(savedMalletModel.numTypes);
		assertEquals(100, topicWords.length);
	}
	
	@Test
	public void testNoOptimization() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template_no_optimization.xml");
		
		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		Model model = tm.train(docs);
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
		
		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
//		Model model = tm.train(docs);
		tm.mapPubmedIdToMalletId(docs, model);
		HashMap<String, Integer> map = model.pubmedIdModelId;
		Integer int1 = 1;
		Integer int170 = 170;
		Integer int167 = 167;
		assertEquals(map.get("1669026"), int1);
		assertEquals(map.get("28647898"), int170);
		assertEquals(map.get("29049350"), int167);
		Collection<Integer> malletIds = map.values();
		Object[] malletIdsArray = malletIds.toArray();
		for (int i = 0; i < malletIdsArray.length; i++){
			Integer malletId = (Integer) malletIdsArray[i];
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
		
		File file = new File("src/test/resources/test_folder_data/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
//		Model model = tm.train(docs);
		tm.mapPubmedIdToMalletId(docs, model);
		HashMap<String, Integer> map = model.pubmedIdModelId;
		Integer int1 = 1;
		Integer int170 = 170;
		Integer int167 = 167;
		assertEquals(map.get("1669026"), int1);
		assertEquals(map.get("28647898"), int170);
		assertEquals(map.get("29049350"), int167);
		Collection<Integer> malletIds = map.values();
		Object[] malletIdsArray = malletIds.toArray();
		for (int i = 0; i < malletIdsArray.length; i++){
			Integer malletId = (Integer) malletIdsArray[i];
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
		// muss hier vorher als ganzes TM Model gespeichert sein statt nur dem MalletModel!
		Model savedModel = tm.readModel("src/test/resources/test_topic_model");
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
	
	@Test
	public void testInferLabelDatabase() throws ConfigurationException {
		String pmid = "";
		String subset = "";
		
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template.xml");
		
		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
		List<Document> docList = tm.readXmiDb(tm, subset);
		Document doc = docList.get(0);
		Model savedModel = tm.readModel("src/test/resources/test_model");
		HashMap<Integer, Double> result = tm.inferTopicWeightLabel(doc, savedModel);
		System.out.println(savedModel.pubmedIdModelId.get(pmid));
		assertNotEquals(null, result);
		assertEquals(savedModel.malletModel.numTopics, result.size());
	}
	
//	@Test
//	public void testJSBD() throws ConfigurationException {
//		MalletTopicModeling tm = new MalletTopicModeling(
//				"src/test/resources/config_template.xml");
//		
//		File file = new File("D:/server_logs/pubmed18n0740/pubmed18n0740.xml");
//		List<Document> docs = tm.readDocuments(file);
//		tm.jcorePreprocess(docs);
//	}
	
	@Test
	public void testSearch() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template.xml");
		
//		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
//		List<Document> docs = tm.readDocuments(file);
//		Model model = tm.train(docs);
		model = tm.readModel("src/test/resources/models_with_index/test_merged_indexes_model.ser");
		Document query0 = new Document();
		Document query1 = new Document();
		Document query2 = new Document();
		Document queryABC = new Document();
		List<Document> queryList = new ArrayList<>();
		query0.id = "0";
		query0.text = "pregnancy birth children";
//		queryList.add(query0);
//		query0.preprocessedData = tm.jcorePreprocess(queryList);
		LOGGER.info("Search query 0");
		TMSearchResult result0 = tm.search(query0, model);
		LOGGER.info("Result 0: " + result0.pubmedID);
		query1.id = "1";
		query1.text = "health cancer patient";
		queryList.add(query1);
		query1.preprocessedData = tm.jcorePreprocess(queryList);
		LOGGER.info("Search query 1");
		TMSearchResult result1 = tm.search(query1, model);
		LOGGER.info("Result 1: " + result1.pubmedID);
		query2.id = "2";
		query2.text = "test";
		queryList.add(query2);
		query2.preprocessedData = tm.jcorePreprocess(queryList);
		LOGGER.info("Search query 2");
		TMSearchResult result2 = tm.search(query2, model);
		LOGGER.info("Result 2: " + result2.pubmedID);
		queryABC.id = "abc";
		queryABC.text = "mouse tumor";
		queryList.add(queryABC);
		queryABC.preprocessedData = tm.jcorePreprocess(queryList);
		LOGGER.info("Search query ABC");
		TMSearchResult resultABC = tm.search(queryABC, model);
		LOGGER.info("Result ABC: " + resultABC.pubmedID);
	}
	
	@Test
	public void testSearchModelOnly() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template.xml");
		
		model = tm.readModel("src/test/resources/test_topic_model.ser");
		Document query0 = new Document();
		Document query1 = new Document();
		Document query2 = new Document();
		Document queryABC = new Document();
		List<Document> queryList = new ArrayList<>();
		query0.id = "0";
		query0.text = "pregnancy birth children";
		queryList.add(query0);
		query0.preprocessedData = tm.jcorePreprocess(queryList);
		LOGGER.info("Search query 0");
		TMSearchResult result0 = tm.searchModelOnly(query0, model);
		LOGGER.info("Result 0: " + result0.pubmedID);
		query1.id = "1";
		query1.text = "health cancer patient";
		queryList.add(query1);
		query1.preprocessedData = tm.jcorePreprocess(queryList);
		LOGGER.info("Search query 1");
		TMSearchResult result1 = tm.searchModelOnly(query1, model);
		LOGGER.info("Result 1: " + result1.pubmedID);
		query2.id = "2";
		query2.text = "test";
		queryList.add(query2);
		query2.preprocessedData = tm.jcorePreprocess(queryList);
		LOGGER.info("Search query 2");
		TMSearchResult result2 = tm.searchModelOnly(query2, model);
		LOGGER.info("Result 2: " + result2.pubmedID);
		queryABC.id = "abc";
		queryABC.text = "mouse tumor";
		queryList.add(queryABC);
		queryABC.preprocessedData = tm.jcorePreprocess(queryList);
		LOGGER.info("Search query ABC");
		TMSearchResult resultABC = tm.searchModelOnly(queryABC, model);
		LOGGER.info("Result ABC: " + resultABC.pubmedID);
	}
	
	@Test
	public void testSearchNoOptimization() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template_no_optimization.xml");
		
		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		Model model = tm.train(docs);
		Document query0 = new Document();
		Document query1 = new Document();
		Document query2 = new Document();
		Document queryABC = new Document();
		// TODO: test with index
		query0.id = "0";
		query0.text = "pregnancy birth children";
		LOGGER.info("Search query 0 (NoOptimization)");
		TMSearchResult result0 = tm.search(query0, model);
		LOGGER.info("Result 0 (NoOptimization): " + result0.pubmedID);
		query1.id = "1";
		query1.text = "health cancer patient";
		LOGGER.info("Search query 1 (NoOptimization)");
		TMSearchResult result1 = tm.search(query1, model);
		LOGGER.info("Result 1 (NoOptimization): " + result1.pubmedID);
		query2.id = "2";
		query2.text = "test";
		LOGGER.info("Search query 2 (NoOptimization)");
		TMSearchResult result2 = tm.search(query2, model);
		LOGGER.info("Result 2 (NoOptimization): " + result2.pubmedID);
		queryABC.id = "abc";
		queryABC.text = "mouse tumor";
		LOGGER.info("Search query ABC (NoOptimization)");
		TMSearchResult resultABC = tm.search(queryABC, model);
		LOGGER.info("Result ABC (NoOptimization): " + resultABC.pubmedID);
	}
	
	@Test
	public void testBigSearch() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template.xml");
		
		File file = new File("D:/server_logs/pubmed18n0740.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		Model model = tm.train(docs);
		Document query0 = new Document();
		Document query1 = new Document();
		Document query2 = new Document();
		Document queryABC = new Document();
		// TODO: test with index
		query0.id = "0";
		query0.text = "pregnancy birth children";
		LOGGER.info("Big Search query 0");
		TMSearchResult result0 = tm.search(query0, model);
		LOGGER.info("Big Result 0: " + result0.pubmedID);
		query1.id = "1";
		query1.text = "health cancer patient";
		LOGGER.info("Big Search query 1");
		TMSearchResult result1 = tm.search(query1, model);
		LOGGER.info("Big Result 1: " + result1.pubmedID);
		query2.id = "2";
		query2.text = "test";
		LOGGER.info("Big Search query 2");
		TMSearchResult result2 = tm.search(query2, model);
		LOGGER.info("Big Result 2: " + result2.pubmedID);
		queryABC.id = "abc";
		queryABC.text = "mouse tumor";
		LOGGER.info("Big Search query ABC");
		TMSearchResult resultABC = tm.search(queryABC, model);
		LOGGER.info("Big Result ABC: " + resultABC.pubmedID);
	}
	
	//dummy test
	@Test
	public void testPreprocessing() throws ConfigurationException {
		// TODO: delete?
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template.xml");
		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
		List<Document> docs = tm.readDocuments(file);
//		InstanceList instances = tm.preprocess(docs);
		tm.preprocess(docs);
		
//		Iterator<Instance> institer = instances.iterator();
//		while (institer.hasNext()) {
//			Instance inst = institer.next();
//			Alphabet data = inst.getDataAlphabet();
//			Iterator<Object> dataiter =  data.iterator();
//			while (dataiter.hasNext()) {
//				System.out.println(institer.next());
//			}
//			assertTrue(data.contains("3-hydroxy-3-methylglutaryl-coenzyme"));
//		}
//		Instance instWithAlphaNum = instances.get(1);
		Instance instWithAlphaNum = (Instance) docs.get(1).preprocessedData;
		Alphabet instWords = instWithAlphaNum.getAlphabet();
		System.out.println(instWords);
		assertTrue(instWords.contains("3-hydroxy-3-methylglutaryl-coenzyme"));
		assertFalse(instWords.contains("16"));
//		Instance instWithAlphaNum_5 = instances.get(5);
		Instance instWithAlphaNum_5 = (Instance) docs.get(5).preprocessedData;
		Alphabet instWords_5 = instWithAlphaNum_5.getAlphabet();
		assertFalse(instWords_5.contains("18-29"));
//		Instance instWithAlphaNum_10 = instances.get(10);
		Instance instWithAlphaNum_10 = (Instance) docs.get(5).preprocessedData;
		Alphabet instWords_10 = instWithAlphaNum_10.getAlphabet();
		assertFalse(instWords_10.contains("-0.6"));
	}	
	
	@Test
	public void testBigSearchNoOptimization() throws ConfigurationException, IOException {
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template_no_optimization.xml");
		
		File file = new File("D:/server_logs/pubmed18n0740.xml.gz");
		List<Document> docs = tm.readDocuments(file);
		Model model = tm.train(docs);
		Document query0 = new Document();
		Document query1 = new Document();
		Document query2 = new Document();
		Document queryABC = new Document();
		query0.id = "0";
		query0.text = "pregnancy birth children";
		LOGGER.info("Big Search query 0 (NoOptimization)");
		TMSearchResult result0 = tm.search(query0, model);
		LOGGER.info("Big Result 0 (NoOptimization): " + result0.pubmedID);
		query1.id = "1";
		query1.text = "health cancer patient";
		LOGGER.info("Big Search query 1 (NoOptimization)");
		TMSearchResult result1 = tm.search(query1, model);
		LOGGER.info("Big Result 1 (NoOptimization): " + result1.pubmedID);
		query2.id = "2";
		query2.text = "test";
		LOGGER.info("Big Search query 2 (NoOptimization)");
		TMSearchResult result2 = tm.search(query2, model);
		LOGGER.info("Big Result 2 (NoOptimization): " + result2.pubmedID);
		queryABC.id = "abc";
		queryABC.text = "mouse tumor";
		LOGGER.info("Big Search query ABC (NoOptimization)");
		TMSearchResult resultABC = tm.search(queryABC, model);
		LOGGER.info("Big Result ABC (NoOptimization): " + resultABC.pubmedID);
	}
	
	
	@Test
	public void testReadXmiDb() throws ConfigurationException {
		String subset = "testmodeling";
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template.xml");
		List<Document> lemmataFromDb = tm.readXmiDb(tm, subset);
		assertNotEquals(null, lemmataFromDb);
		assertEquals(Token.class, lemmataFromDb.get(0).preprocessedData);
	}
	
	@Test
	public void testGetVocabulary() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling(
				"src/test/resources/config_template.xml");
		File modelFile = new File("src/test/resources/test_model");
		Model model = tm.readMalletModel(modelFile);
		Object[] voc = tm.getVocabulary(model);
		for (int i = 0; i < voc.length; i++) {
			assertEquals(String.class, voc[i].getClass());
		}
	}
	
	@Test
	public void testMergeIndexes() throws ConfigurationException, UnknownHostException {
		String indexesDir = "src/test/resources/models_with_index/";
		String mergedIndexesFilesName = "test_merged_indexes_model.ser";
		MalletTopicModeling tm = new MalletTopicModeling("src/test/resources/config_template.xml");
		tm.mergeIndexes(indexesDir, indexesDir + mergedIndexesFilesName);
		
		Model savedIndexedModel = tm.readModel("src/test/resources/"
				+ "models_with_index/test_merged_indexes_model.ser"); 
		System.out.println(savedIndexedModel.index);
		assertTrue(savedIndexedModel.index.containsKey("7610069"));
		assertTrue(savedIndexedModel.index.containsKey("8283956"));
		assertTrue(savedIndexedModel.index.containsKey("11442408"));
		assertTrue(savedIndexedModel.index.containsKey("12390745"));
		assertTrue(savedIndexedModel.index.size() == 4);
		assertNotNull(savedIndexedModel.malletModel);
		assertNotNull(savedIndexedModel.modelId);
		assertNotNull(savedIndexedModel.ModelIdpubmedId);
		assertNotNull(savedIndexedModel.modelVersion);
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
