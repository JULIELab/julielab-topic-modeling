// modified version taken from testing-mallet version 1.1.0

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
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import de.julielab.xml.JulieXMLConstants;
import de.julielab.xml.JulieXMLTools;


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
		
	public InstanceList items2Instances(List<String> foundItems) {		
		// -====== modified TopicModel Source Code: get instances =====-
		
		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add( new CharSequenceLowercase() );
		pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
		pipeList.add( new TokenSequenceRemoveStopwords(new File("D:/mallet-2.0.7/mallet-2.0.7/stoplists/en.txt"), "UTF-8", false, false, false) );
		pipeList.add( new TokenSequence2FeatureSequence() );

		InstanceList instances = new InstanceList (new SerialPipes(pipeList));
		
		ArrayIterator dataListIterator = new ArrayIterator (foundItems);
		instances.addThruPipe(dataListIterator);
		
		return instances;
		
		// Display instances and their counted number (int count)
//		ListIterator<Instance> listinstances = instances.listIterator();
//		int count = 1;
//		while (listinstances.hasNext()) {
//			System.out.println(count);
//			Instance doc = listinstances.next();
//			Object text = doc.getData();
//			if (count > 6000) {
//				System.out.println(text);
//			}
//			++count;
//		}
		
		// Display certain data
//		Instance doc1 = instances.get(1);
//		Object text1 = doc1.getData();
//		System.out.print(text1);
//		Instance doc2 = instances.get(2);
//		Object text2 = doc2.getData();
//		System.out.print(text2);

	}
	
	public String getLogData() {
		return logData;
	}
}
