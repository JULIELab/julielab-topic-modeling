package de.julielab.topicmodeling.services;


import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.TMSearchResult;

public class SearchTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MalletTopicModelingTest.class);

	public SearchTest() {
	}

	@Test
	public void testSearch() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling();
		
//		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
//		List<Document> docs = tm.readDocuments(file);
//		Model model = tm.train(docs);
		Search search = new Search();
		Model model = tm.readModel("src/test/resources/models_with_index/test_merged_indexes_model.ser");
		Document query0 = new Document();
		Document query1 = new Document();
		Document query2 = new Document();
		Document queryABC = new Document();
		
		query0.id = "0";
		query0.text = "pregnancy birth children";
		LOGGER.info("Search query 0");
		TMSearchResult result0 = search.search(query0, model);
		LOGGER.info("Result 0: " + result0.pubmedID);
		
		query1.id = "1";
		query1.text = "health cancer patient";
		LOGGER.info("Search query 1");
		TMSearchResult result1 = search.search(query1, model);
		LOGGER.info("Result 1: " + result1.pubmedID);
		
		query2.id = "2";
		query2.text = "test";
		LOGGER.info("Search query 2");
		TMSearchResult result2 = search.search(query2, model);
		LOGGER.info("Result 2: " + result2.pubmedID);
		
		queryABC.id = "abc";
		queryABC.text = "mouse tumor";
		LOGGER.info("Search query ABC");
		TMSearchResult resultABC = search.search(queryABC, model);
		LOGGER.info("Result ABC: " + resultABC.pubmedID);
	}
//	old test for old search
//	@Test
//	public void testSearch() throws ConfigurationException {
//		MalletTopicModeling tm = new MalletTopicModeling(
//				"src/test/resources/config_template.xml");
//		
////		File file = new File("src/test/resources/pubmedsample18n0001.xml.gz");
////		List<Document> docs = tm.readDocuments(file);
////		Model model = tm.train(docs);
//		model = tm.readModel("src/test/resources/models_with_index/test_merged_indexes_model.ser");
//		Document query0 = new Document();
//		Document query1 = new Document();
//		Document query2 = new Document();
//		Document queryABC = new Document();
//		List<Document> queryList = new ArrayList<>();
//		
//		query0.id = "0";
//		query0.text = "pregnancy birth children";
//		queryList.add(query0);
//		List<TokenSequence> preprocessedQueryList = tm.jcorePreprocess(queryList);
//		query0.preprocessedData = preprocessedQueryList.get(0); 
//		LOGGER.info("Search query 0");
//		TMSearchResult result0 = tm.search(query0, model);
//		LOGGER.info("Result 0: " + result0.pubmedID);
//		
//		query1.id = "1";
//		query1.text = "health cancer patient";
//		queryList.add(query1);
//		query1.preprocessedData = tm.jcorePreprocess(queryList);
//		LOGGER.info("Search query 1");
//		TMSearchResult result1 = tm.search(query1, model);
//		LOGGER.info("Result 1: " + result1.pubmedID);
//		
//		query2.id = "2";
//		query2.text = "test";
//		queryList.add(query2);
//		query2.preprocessedData = tm.jcorePreprocess(queryList);
//		LOGGER.info("Search query 2");
//		TMSearchResult result2 = tm.search(query2, model);
//		LOGGER.info("Result 2: " + result2.pubmedID);
//		
//		queryABC.id = "abc";
//		queryABC.text = "mouse tumor";
//		queryList.add(queryABC);
//		queryABC.preprocessedData = tm.jcorePreprocess(queryList);
//		LOGGER.info("Search query ABC");
//		TMSearchResult resultABC = tm.search(queryABC, model);
//		LOGGER.info("Result ABC: " + resultABC.pubmedID);
//	}
}
