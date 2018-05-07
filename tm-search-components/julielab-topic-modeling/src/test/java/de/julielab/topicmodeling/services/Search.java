package de.julielab.topicmodeling.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.TokenSequence;
import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.TMSearchResult;
import de.julielab.topicmodeling.businessobjects.Topic;

public class Search {

	private MalletTopicModeling tm;
	private XMLConfiguration xmlConfig;
	
	public Search() {
		try {
			tm = new MalletTopicModeling();
			xmlConfig = tm.loadConfig("src/test/resources/config_template.xml");
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public TMSearchResult search(Document query, Model model) {
		double probabilityThreshold = xmlConfig.getDouble("search.parameters.parameter"
				+ ".probabilityThreshold"); 
		
		TMSearchResult result = new TMSearchResult();
		result.malletId = new ArrayList<Integer>();
		result.probabilities = new ArrayList<Double>();
		result.pubmedID = new ArrayList<String>();
		
		if (query.preprocessedData == null) {
			List<Document> queryInList = new ArrayList<>();
			queryInList.add(query);
			List<TokenSequence> preprocessedQueryInList = tm.jcorePreprocess(queryInList);
			query.preprocessedData = preprocessedQueryInList.get(0);
		}
		Map<String, List<Topic>> queryInstance = tm.inferLabel(query, model, xmlConfig);
		List<Topic> queryTopics = queryInstance.get(query.id);
		
		List<Integer> relevantProbabilitiesIndex = new ArrayList<Integer>();
		for (int i = 0; i < queryTopics.size(); i++) {
			if (queryTopics.get(i).probability >= probabilityThreshold) {
				relevantProbabilitiesIndex.add(i);
			}	
		}
		double[] queryProbabilities = new double[relevantProbabilitiesIndex.size()];
		HashMap<Integer, Double> cosineSimilarities = new HashMap<Integer, Double>();
		for (int i = 0; i < relevantProbabilitiesIndex.size(); i++) {
			double queryProbability = queryTopics.get(i).probability;
			queryProbabilities[i] = queryProbability;
		}
		
		ParallelTopicModel malletModel = model.malletModel;
		double[][] documentsTopics = malletModel.getDocumentTopics(false, false);
		for (int i = 0 ; i < relevantProbabilitiesIndex.size(); i++) {
			double[] documentTopics = documentsTopics[relevantProbabilitiesIndex.get(i)];
			double cosineSimilarity = MalletTopicModeling.computeSimilarity(queryProbabilities, documentTopics);
			cosineSimilarities.put(i, cosineSimilarity);
		}
		HashMap<String, List<Topic>> index = model.index;
		for (int i = 0; i < index.size(); i++) {
			List<Topic> documentTopics = index.get(index.keySet().toArray()[i]);
			if (documentTopics != null) {
				double[] documentProbabilities = new double[relevantProbabilitiesIndex.size()];
				for (int k = 0 ; k < relevantProbabilitiesIndex.size(); k++) {
					for (int m = 0; m < documentTopics.size(); m++) {
						if (documentTopics.get(m).id == relevantProbabilitiesIndex.get(k) 
								&& relevantProbabilitiesIndex.get(k) != null) {
							documentProbabilities[k] = documentTopics.get(m).probability;
						}
					}
				}
				double cosineSimilarity = MalletTopicModeling.computeSimilarity
						(queryProbabilities, documentProbabilities);
				cosineSimilarities.put(i, cosineSimilarity);
			}
		}
		List<Entry<Integer, Double>> list = new LinkedList<Entry<Integer, Double>>(
				cosineSimilarities.entrySet());
        Collections.sort(list, new Comparator<Entry<Integer, Double>>() {
            public int compare(Entry<Integer, Double> o1,
            	Entry<Integer, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        int displayedHits = xmlConfig.getInt("search.results.displayedHits", list.size());
        // displayHits as cosine threshold
        for(int i = 0; i < displayedHits; i++) {
        	Entry<Integer, Double> entry = list.get(i);
            result.malletId.add(entry.getKey());
            result.probabilities.add(entry.getValue());
            result.pubmedID.add(model.ModelIdpubmedId.get(entry.getKey()));
        }
		return result;
	}

	
	// old malfunctioning search() 
//	public TMSearchResult search(Document query, Model model) {
//		double probabilityThreshold = xmlConfig.getDouble("search.parameters.parameter"
//				+ ".probabilityThreshold"); 
//		
//		TMSearchResult result = new TMSearchResult();
//		result.malletId = new ArrayList<Integer>();
//		result.probabilities = new ArrayList<Double>();
//		result.pubmedID = new ArrayList<String>();
//		
////		List<Document> queryInList = new ArrayList<>();
////		queryInList.add(query);
////		query.preprocessedData = jcorePreprocess(queryInList);
//		Map<String, List<Topic>> queryInstance = inferLabel(query, model);
//		List<Topic> queryTopics = queryInstance.get(query.id);
//		double[] queryProbabilities = new double[queryTopics.size()];
//		List<Integer> relevantProbabilitiesIndex = new ArrayList<Integer>();
//		for (int i = 0; i < queryTopics.size(); i++) {
//			if (queryTopics.get(i).probability >= probabilityThreshold) {
//				relevantProbabilitiesIndex.add(i);
//				double queryProbability = queryTopics.get(i).probability;
//				queryProbabilities[i] = queryProbability;
//			}	
//		}
//		HashMap<Integer, Double> cosineSimilarities = new HashMap<Integer, Double>();
//		
//		ParallelTopicModel malletModel = model.malletModel;
//		double[][] documentsTopics = malletModel.getDocumentTopics(false, false);
//		for (int i = 0 ; i < relevantProbabilitiesIndex.size(); i++) {
//			double[] documentTopics = documentsTopics[relevantProbabilitiesIndex.get(i)];
//			double cosineSimilarity = computeSimilarity(queryProbabilities, documentTopics);
//			cosineSimilarities.put(i, cosineSimilarity);
//		}
//		HashMap<String, List<Topic>> index = model.index;
//		for (int i = 0; i < index.size(); i++) {
////			List<Topic> documentTopics = index.get(i);
//			List<Topic> documentTopics = index.get(index.keySet().toArray()[i]);
//			if (documentTopics != null) {
//				double[] documentProbabilities = new double[documentTopics.size()];
////				for (int k = 0 ; k < relevantProbabilitiesIndex.size(); k++) {
//				for (int k = 0 ; k < documentTopics.size(); k++) {
//					Topic documentTopic = documentTopics.get(k);
////					Topic documentTopic = documentTopics.get(relevantProbabilitiesIndex.get(k));
//					if (documentTopic.id == relevantProbabilitiesIndex.get(k)) {
//						documentProbabilities[k] = documentTopic.probability;
//					}
////					double cosineSimilarity = computeSimilarity(queryProbabilities, documentProbabilities);
////					cosineSimilarities.put(k, cosineSimilarity);
//				}
//			double[] intersectingProbabilities = new double[documentProbabilities.length];
//			for (int m = 0; m < documentProbabilities.length; m++) {
//				intersectingProbabilities[m] = queryProbabilities[m];
//			}
//			double cosineSimilarity = computeSimilarity(queryProbabilities, documentProbabilities);
////			double cosineSimilarity = computeSimilarity(queryProbabilities, documentProbabilities);
//			cosineSimilarities.put(i, cosineSimilarity);
//			}
//		}
//		List<Entry<Integer, Double>> list = new LinkedList<Entry<Integer, Double>>(
//				cosineSimilarities.entrySet());
//        Collections.sort(list, new Comparator<Entry<Integer, Double>>() {
//            public int compare(Entry<Integer, Double> o1,
//            	Entry<Integer, Double> o2) {
//                return o1.getValue().compareTo(o2.getValue());
//            }
//        });
//
//        int displayedHits = xmlConfig.getInt("search.results.displayedHits", list.size());
//        // displayHits as cosine threshold
//        for(int i = 0; i < displayedHits; i++) {
//        	Entry<Integer, Double> entry = list.get(i);
//            result.malletId.add(entry.getKey());
//            result.probabilities.add(entry.getValue());
//            result.pubmedID.add(model.ModelIdpubmedId.get(entry.getKey()));
//        }
//		return result;
//	}s
}
