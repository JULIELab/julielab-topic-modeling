package de.julielab.topicmodeling.services;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.services.MalletTopicModeling;

public class MalletTopicModelingTest {

	@Test
	public void testReadDocumentsNotEmpty() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling("D:/config_template.xml");
		File file = new File("D:/test_samples/pubmedsample18n0001copy.xml");
		List<Document> docs = tm.readDocuments(file);
		assertNotEquals(0, docs.size());
	}
	
	@Test
	public void testReadDocumentsFirstThreeDocs() throws ConfigurationException {
		MalletTopicModeling tm = new MalletTopicModeling("D:/config_template.xml");
		File file = new File("D:/test_samples/pubmedsample18n0001copy.xml");
		List<Document> docs = tm.readDocuments(file);
		
		Document doc0 = docs.get(0);
		String doc0Text = doc0.text;
		String doc0Id = doc0.id;
		assertEquals("", doc0Text);
		assertEquals("973217", doc0Id);
		
		Document doc1 = docs.get(1);
		String doc1Text = doc1.text;
		String doc1Id = doc1.id;
		assertEquals("", doc1Text);
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
		MalletTopicModeling tm = new MalletTopicModeling("D:/config_template.xml");
		File file = new File("D:/test_samples/pubmedsample18n0001copy.xml");
		List<Document> docs = tm.readDocuments(file);
		
		int docCount = 0;
		for (int i = 0; i < docs.size(); i++) {
			docCount++;
		}
		assertEquals(15377, docCount);
	}
}
