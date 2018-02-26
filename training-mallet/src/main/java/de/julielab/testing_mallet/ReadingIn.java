
package de.julielab.testing_mallet;

import cc.mallet.types.*;

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import de.julielab.xml.JulieXMLConstants;
import de.julielab.xml.JulieXMLTools;
import de.julielab.jcore.types.*;
import de.julielab.jcore.types.Token;
import de.julielab.jcore.ae.jtbd.main.TokenAnnotator;
import de.julielab.jcore.ae.biolemmatizer.*;
import de.julielab.jcore.ae.jsbd.main.SentenceAnnotator;


public class ReadingIn {
	
	private String logData = "";
	
	public List<String> data2Items (String fileName, String forEach, ArrayList<String> fieldPaths) {
		
		// -====== (modified) JulieXMLToolsCLIRecords Source Code: XML2row Mapping =====-
		
		ArrayList<String> args = new ArrayList<String>();
		args.add(fileName);
		args.add(forEach);
		for (int i = 0; i <= fieldPaths.size() - 1; i++) {
			args.add(fieldPaths.get(i));
		}
		//next line is originally from XML tool but does not work with changed "args", "fieldPaths" from
		//String[] to ArrayList<String>
//		System.arraycopy(args, 2, fieldPaths, 0, fieldPaths.size());
		
		List<Map<String, String>> fields = new ArrayList<>();
		for (int i = 0; i < fieldPaths.size(); i++) {
			String path = fieldPaths.get(i);
			Map<String, String> field = new HashMap<String, String>();
			field.put(JulieXMLConstants.NAME, "fieldvalue" + i);
			field.put(JulieXMLConstants.XPATH, path);
			fields.add(field);
		}
		
		Iterator<Map<String, Object>> rowIterator = JulieXMLTools.constructRowIterator(fileName, 1024, forEach, fields, false);
		
		PrintStream out = null;
			try {
				out = new PrintStream(System.out, true, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			
		int foundCount = 0;
		List<String> foundItems = new ArrayList<>();
		while (rowIterator.hasNext()) {
			Map<String, Object> row = rowIterator.next();
			List<String> rowValues = new ArrayList<>();
			for (int i = 0; i < fieldPaths.size(); i++) {
				String value = (String) row.get("fieldvalue" + i);
				rowValues.add(value);
				if (value != null) {
					foundCount++;
					foundItems.add("\n" + value);
				}
			}
			out.println(StringUtils.join(rowValues, "\t"));
			
		}

		String itemsLog = "JULIELab-TM-LOG: Number of found items in " 
													+ fileName + ": " 
													+ foundCount + "\n";
		logData = logData.concat(itemsLog);
//		System.out.println(itemsLog);
		
		return foundItems;
		// end of (modified) JulieXMLToolsCLIRecords Source Code
	}
		
	public InstanceList items2Instances(List<String> foundItems, String stopWordsFile) {		
		// -====== modified TopicModel Source Code: get instances =====-
		
		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		
		// For compatibility: get stoplist from resources folder
//		ClassLoader classLoader = getClass().getClassLoader();
//		try {
//			File stopwordsFile = new File(classLoader.getResource("en.txt").toURI());
//			// Pipes: lowercase, tokenize, remove stopwords, map to features
//			pipeList.add( new CharSequenceLowercase() );
//			pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
//			pipeList.add( new TokenSequenceRemoveStopwords(stopwordsFile, "UTF-8", false, false, false) );
//			pipeList.add( new TokenSequence2FeatureSequence() );
//		} catch (URISyntaxException e) {
//				e.printStackTrace();
//		}
		
		

//		InputStream stopwordsStream = new File(classLoader.getResourceAsStream("en.txt"));
		
		// Pipes: lowercase, tokenize, remove stopwords, map to features
		// UIMAfit approach
		// TO DO: add pipes for Tokenization and BioLemmatizer
//		List<String> foundLemmata = new ArrayList<String>();
//		TokenSequence foundLemmata = new TokenSequence();
//		List<TokenSequence> allLemmata = new ArrayList<TokenSequence>();
//		try {
//		
//			for (int i = 0; i < foundItems.size(); i++) {
//				String sentences = foundItems.get(i);
//				
//				JCas jCas = JCasFactory.createJCas();
//				jCas.setDocumentText(sentences);
//				detectSentence(jCas);
//				tokenize(jCas);
//				lemmatize(jCas);
//				foundLemmata = getLemmata(jCas);
//				allLemmata.add(foundLemmata);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		List<TokenSequence> allLemmata = text2Lemmata(foundItems);
		
//		pipeList.add( new CharSequenceLowercase() );
//		pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
		pipeList.add( new TokenSequenceRemoveStopwords(new File (stopWordsFile), "UTF-8", false, false, false) );
		pipeList.add( new TokenSequenceRemoveNonAlpha() );
		pipeList.add( new TokenSequence2FeatureSequence() );


		InstanceList instances = new InstanceList (new SerialPipes(pipeList));
		
//		ArrayIterator dataListIterator = new ArrayIterator (foundItems);
		ArrayIterator dataListIterator = new ArrayIterator (allLemmata);
		instances.addThruPipe(dataListIterator);
		
		return instances;
	}
	
	public List<TokenSequence> text2Lemmata(List<String> foundItems) {
		TokenSequence foundLemmata = new TokenSequence();
		List<TokenSequence> allLemmata = new ArrayList<TokenSequence>();
		try {
			SentenceAnnotator jsbd = new SentenceAnnotator();
			AnalysisEngine sentenceDetector = AnalysisEngineFactory.createEngine(jsbd.getClass(), 
					"ModelFilename", "de/julielab/jcore/ae/jsbd/model/jsbd-biomed-original_mallet.gz");
			TokenAnnotator jtbd = new TokenAnnotator();
			AnalysisEngine tokenizer = AnalysisEngineFactory.createEngine(jtbd.getClass(), 
					"ModelFilename", "de/julielab/jcore/ae/jtbd/model/jtbd-biomed-original_mallet.gz");
			BioLemmatizer biolemm = new BioLemmatizer();
			AnalysisEngine bioLemmatizer = AnalysisEngineFactory.createEngine(biolemm.getClass());
			for (int i = 0; i < foundItems.size(); i++) {
				String sentences = foundItems.get(i);
				JCas jCas = JCasFactory.createJCas();
				jCas.setDocumentText(sentences);
				sentenceDetector.process(jCas);
				tokenizer.process(jCas);
				bioLemmatizer.process(jCas);
				foundLemmata = getLemmata(jCas);
				allLemmata.add(foundLemmata);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allLemmata;
	}

//	public void detectSentence(JCas aJCas) {
//		try {
//			SentenceAnnotator jsbd = new SentenceAnnotator();
//			AnalysisEngine sentenceDetector = AnalysisEngineFactory.createEngine(jsbd.getClass(), 
//					"ModelFilename", "de/julielab/jcore/ae/jsbd/model/jsbd-biomed-original_mallet.gz");
//			sentenceDetector.process(aJCas);
//			sentenceDetector.destroy();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public void tokenize(JCas aJCas) {
//		try {
//			TokenAnnotator jtbd = new TokenAnnotator();
//			AnalysisEngine tokenizer = AnalysisEngineFactory.createEngine(jtbd.getClass(), 
//					"ModelFilename", "de/julielab/jcore/ae/jtbd/model/jtbd-biomed-original_mallet.gz");
//			tokenizer.process(aJCas);
//			tokenizer.destroy();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public void lemmatize(JCas aJCas) {
//		try {
//			BioLemmatizer biolemm = new BioLemmatizer();
//			AnalysisEngine bioLemmatizer = AnalysisEngineFactory.createEngine(biolemm.getClass());
//			bioLemmatizer.process(aJCas);
//			bioLemmatizer.destroy();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public TokenSequence getLemmata(JCas aJCas) {
//		List<String> lemmata = new ArrayList<String>();
		TokenSequence lemmata = new TokenSequence();
		AnnotationIndex<Annotation> tokenIndex = aJCas.getAnnotationIndex(Token.type);
		FSIterator<Annotation> tokenIterator = tokenIndex.iterator();
		while (tokenIterator.hasNext()) {
			Token token = (Token) tokenIterator.get();
			Lemma lemma = token.getLemma();
			String lemmaString = lemma.getValue();
//			lemmata.add(lemmaString);
			lemmata .add(lemmaString);
			tokenIterator.next();
		}
		return lemmata;
	}	
	
	public String getLogData() {
		return logData;
	}
}
