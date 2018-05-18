package de.julielab.tmindexmerger.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import de.julielab.tmindexmerger.IndexMerger;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.services.MalletTopicModeling;

public class IndexMergerTest {

	public IndexMergerTest() {
		// TODO Auto-generated constructor stub
	}
	
	@Test
	public void testMergeIndexes() throws ConfigurationException, UnknownHostException {
		String indexesDir = "src/test/resources/models_with_index/";
		String mergedIndexesFilesName = "test_merged_indexes_model.ser";
		MalletTopicModeling tm = new MalletTopicModeling();
		IndexMerger im = new IndexMerger();
		im.mergeIndexes(indexesDir, indexesDir + mergedIndexesFilesName);
		
		Model savedIndexedModel = tm.readModel("src/test/resources/"
				+ "models_with_index/test_merged_indexes_model.ser"); 
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
}
