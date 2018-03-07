
package de.julielab.topicmodeling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import de.julielab.topicmodeling.ReadingIn;
import de.julielab.topicmodeling.TrainingModel;

public class TestingMalletMain {
	
	private String externalLogData = "";

	public static void main(String[] args) throws Exception {
		
		if (args.length == 0) {
			System.out.print ("The following arguments are required: \n"
							+ "args[0]: .txt as configuration file with the following sample format: \n"
							+ "//Configuration ID \n"
							+ "sample_config \n"
							+ "//JulieXMLTools arguments: \n" 
							+ "//'fileName'<if more than one file, write comma-separated: <<filename>,<filename>>, 'forEach', 'fields' \n"
							+ "/var/data/data_corpora/MEDLINE/medline2018/original_download/ /PubmedArticleSet/PubmedArticle/MedlineCitation/Article/Abstract AbstractText OtherAbstract/AbstractText \n"
							+ "//ParallelTopicModel arguments/Dirichlet-Parameter/training including optimization: \n" 
							+ "//'numberOfTopics', 'alpha', 'beta', 'trainingIterations', 'numberOfThreads', 'optimizeInterval' (optional, default: 50) \n"
							+ "10 1.0 0.1 1000 2 \n"
							+ "//Output file name the model is written in \n"
							+ "/home/sample_model \n"
							+ "//Directory of the stopWordList \n"
							+ "/home/en.txt \n" 
							+ "args[1] (optional): filename where the logs are to be written in \n"
							+ "args[2] (optional): if a folder is chosen as data source, the number of files "
							+ "that are to be processed");
		}
		
		// read config file that contains 5 line separated arguments and comments marked by '//'
		File expConfig = new File (args[0]);
		BufferedReader reader = new BufferedReader(new FileReader (expConfig));
		ArrayList<String> config = new ArrayList<String>();
		while (reader.ready()) {
			String configLine = reader.readLine();
			if (!configLine.startsWith("//")) {
				config.add(configLine);
			}
		}
		reader.close();
		
		// sort arguments
		
		String configId = config.get(0);

		String args1 = config.get(1);
		String[] args1Split = args1.split("\\s");
		String fileName = args1Split[0];
		String forEach = args1Split[1];
		ArrayList<String> fieldPaths = new ArrayList<String>();
		for (int i = 2; i <=  args1Split.length - 1; i++)
			fieldPaths.add(args1Split[i]);
		
		String args2 = config.get(2);
		String[] args2Split = args2.split("\\s");
		int numTopics = Integer.parseInt (args2Split[0]);
		double alpha = Double.parseDouble(args2Split[1]);
		double beta = Double.parseDouble(args2Split[2]);
		int trainingIterations = Integer.parseInt(args2Split[3]);
		int numberOfThreads = Integer.parseInt(args2Split[4]);
		int optimizeInterval = 50;
		if (args2Split.length == 6) {
			optimizeInterval = Integer.parseInt(args2Split[5]);
		}
		boolean optimize = true;
		if (optimizeInterval == 0) {
			optimize = false;
		}

		
		String modelFileDirectory = config.get(3);
		
		String stopWordsFile = config.get(4);
		
		// for more than one file to read in
		String[] files = null;
		if (fileName.contains(",")) {
			files = fileName.split(",");
			for (int i = 0; i < files.length; i++) {
				fileName = fileName.concat(files[i] + ", ");
			}
		}
		
		// for a folder directory and its inherited files
		if (!(fileName.endsWith(".xml.gz") || fileName.endsWith(".xml.gzip") || 
				fileName.endsWith(".xml.zip")|| fileName.endsWith(".xml"))) {
			int numberOfFiles = 0;
			if (args.length == 3) {
				numberOfFiles = Integer.parseInt(args[2]);
			}
			files = getFilesFromFolder(fileName, numberOfFiles);
		}
		
		// logging
		String logFileName = args[1];
		Logger ptmLogger = ParallelTopicModel.logger;
		ptmLogger.setLevel(Level.INFO);	
		FileHandler fhandler = new FileHandler(logFileName);
		ptmLogger.addHandler(fhandler);
		fhandler.flush();
		
		String logData = "JULIELab-TM-LOG: Configuration ID: " + configId + "\n"
								+ "\n"
								+ "JULIELab-TM-LOG: XML data directory: " + fileName + "\n"
								+ "JULIELab-TM-LOG: iterated XML-item: " + forEach + "\n"
								+ "JULIELab-TM-LOG: extracted data from XML: " + fieldPaths.get(0) + ", " + fieldPaths.get(1) + "\n"
								+ "\n"
								+ "JULIELab-TM-LOG: chosen number of topics: " + numTopics + "\n"
								+ "JULIELab-TM-LOG: chosen Dirichlet-alpha: " + alpha + "\n"
								+ "JULIELab-TM-LOG: chosen Dirichlet-beta: " + beta + "\n"
								+ "JULIELab-TM-LOG: chosen training iterations: " + trainingIterations + "\n"
								+ "JULIELab-TM-LOG: chosen optimization: " + optimize + "; at interval " + optimizeInterval + "\n"
								+ "\n"
								+ "JULIELab-TM-LOG: model file directory for testing the model: " + modelFileDirectory + "\n";
		
		Date time1 = new Date();
		logData = logData.concat("\n" + "JULIELab-TM-LOG: Starting process at: " + time1);
		System.out.println(logData);
		
		// run Topic Modeling
		ReadingIn read = new ReadingIn();
		
		InstanceList instances = null;
		// for more than one file
		if (files != null) {
			List<String> foundItems = null;
			List<String> allFoundItems = read.data2Items(files[0], forEach, fieldPaths);
			for (int i = 1; i < files.length; i++){
				System.out.println("JULIELab-TM-LOG: Attempt to process file no " + i + 
						" of total " +  files.length + ": " + files[i]);
				foundItems = read.data2Items(files[i], forEach, fieldPaths);
				allFoundItems.addAll(foundItems);
			}
			instances = read.items2Instances(allFoundItems, stopWordsFile);
			logData = logData.concat("\n" + "JULIELab-TM-LOG: Total Items found: " + allFoundItems.size());
		// for one file
		} else {
		List<String> foundItems = read.data2Items(fileName, forEach, fieldPaths);
		instances = read.items2Instances(foundItems, stopWordsFile);
		}
		System.out.println("JULIELab-TM-LOG: instances created");
		fhandler.flush();
		
		TrainingModel model = new TrainingModel(instances, numTopics, alpha, beta, trainingIterations, 
				optimizeInterval, numberOfThreads, modelFileDirectory);
		System.out.println("JULIELab-TM-LOG: model trained and written");
		fhandler.flush();
		
		UsingModel.computeTopicStatistics(modelFileDirectory);
		System.out.println("JULIELab-TM-LOG: topic statistics computed");
		fhandler.flush();
		
		Date time2 = new Date();
		logData = logData.concat("\n" + "JULIELab-TM-LOG: Finishing process at: " + time2);
		
		// write logs if log file directory is set
		if (args[1] != null) {
			writeLogToFile(logData, logFileName, read, model, ptmLogger, fhandler);
		}
		fhandler.flush();
		
	}

	public static void writeLogToFile(String logData, String logFileName, ReadingIn read, 
			TrainingModel model, Logger ptmLogger, Handler fhandler) throws IOException {
		TestingMalletMain malletMain = new TestingMalletMain();
		// get log from ReadingIn
		String readLogData = read.getLogData();
		malletMain.setLogData(readLogData);
		logData = logData.concat("\n" + malletMain.externalLogData + "\n");
		// get log from TrainingModel				
		String modelLogData = model.getLogData();
		malletMain.setLogData(modelLogData);
		logData = logData.concat("\n" + malletMain.externalLogData + "\n");
		ptmLogger.info(logData);
		fhandler.flush();
	}
	
	public static String[] getFilesFromFolder(String folderName, int numberOfFiles) {
		FilenameFilter xmlFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith(".xml.gz") || name.endsWith(".xml.zip")
						|| name.endsWith(".xml.gzip") || name.endsWith(".xml")) {
					return true;
				} else {
					return false;
				}
			}
		};
		File folder = new File (folderName); 
		if (numberOfFiles != 0) {
			File[] fileList = folder.listFiles(xmlFilter);
			String[] files = new String[numberOfFiles];
			for (int i = 0; i < numberOfFiles; i++) {
				files[i] = fileList[i].getPath();
			}
			return files;
		} else {
			File[] fileList = folder.listFiles(xmlFilter);
			String[] files = new String[fileList.length];
			for (int i = 0; i < fileList.length; i++) {
					files[i] = fileList[i].getPath();
			}
			return files;
		}
	}
	
	public void setLogData(String log) {
		this.externalLogData = log;
	}
}