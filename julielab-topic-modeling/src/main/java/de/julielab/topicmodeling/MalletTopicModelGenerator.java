package de.julielab.topicmodeling;


import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import cc.mallet.types.TokenSequence;
import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.services.MalletTopicModeling;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MalletTopicModelGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MalletTopicModelGenerator.class);

    public MalletTopicModelGenerator() {

    }

    public static void main(String[] args) throws ConfigurationException {
        System.setProperty("logback.configurationFile", "src/main/resources/logback-complex.xml");
        try {
            MalletTopicModelGenerator generator = new MalletTopicModelGenerator();
            LOGGER.info("Started with"
                    + " config " + args[0]
                    + " with data file location " + args[1]
                    + ", and model will be written in file " + args[2]);
            Model model = new Model();
            if (args[1].equals("none")) {
                model = generator.generateTopicModelFromDatabase(args[0], args[2]);
            } else {
                model = generator.generateTopicModel(args[0], args[1], args[2]);
            }
            if (args.length == 4) {
                if (args[3].equals("verify")) {
                    generator.verifyModel(args[2], args[0]);
                } else {
                    File topicsFile = new File(args[3]);
                    generator.printTopicsToFile(topicsFile, model);
                }
            }
            if (args.length == 5) {
                if (args[3].equals("verify")) {
                    generator.verifyModel(args[2], args[0]);
                }
                File topicsFile = new File(args[4]);
                generator.printTopicsToFile(topicsFile, model);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Usage: \n"
                    + "Obligatory arguments: [0]<configuration file path>, "
                    + "[1]<file or folder path to PUBMED documents to be modelled> "
                    + "type 'none' for DB connection from dbcConnection file, "
                    + "[2]<newly generated model file path> \n"
                    + "Optional arguments: "
                    + "[3]verify (verifies the model after generating), "
                    + "[3]/[4]<filename for monitoring topics> "
                    + "(prints the topics from the new model in a file)");
        }
    }

    public Model generateTopicModel(String configFileName, String docFilename, String modelFilename)
            throws ConfigurationException {
        MalletTopicModeling tm = new MalletTopicModeling();
        XMLConfiguration xmlConfig = tm.loadConfig(configFileName);
        File docFile = new File(docFilename);
        List<Document> docs = tm.readDocuments(docFile, xmlConfig);
        Model model = tm.train(docs, xmlConfig);
        tm.saveModel(model, modelFilename);
        return model;
    }

    public Model generateTopicModelFromDatabase(String configFileName, String modelFilename)
            throws ConfigurationException {
        MalletTopicModeling tm = new MalletTopicModeling();
        XMLConfiguration xmlConfig = tm.loadConfig(configFileName);
        List<Document> docs = tm.readXmiDb(tm, xmlConfig);
        List<TokenSequence> allDocLemmata = new ArrayList<>();
        List<String> allDocIds = new ArrayList<>();
        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            TokenSequence docLemmata = (TokenSequence) doc.preprocessedData;
            allDocLemmata.add(docLemmata);
            String docId = doc.id;
            allDocIds.add(docId);
        }
        LOGGER.info("Start preprocessing with Mallet pipes");
        InstanceList instances = tm.malletPreprocess(allDocLemmata);
        LOGGER.info("Start training with Mallet");
        Model model = tm.train(instances, xmlConfig);
        LOGGER.info("Start mapping Mallet IDs to PMIDs");
        tm.mapMalletIdToPubmedId(docs, model);
        tm.saveModel(model, modelFilename);
        LOGGER.info("Model is saved in file: " + modelFilename);
        return model;
    }

    public void verifyModel(String modelFilename, String configFileName) throws ConfigurationException {
        MalletTopicModeling tm = new MalletTopicModeling();
        XMLConfiguration xmlConfig = tm.loadConfig(configFileName);
        Model model = tm.readModel(modelFilename);
        ParallelTopicModel savedMalletModel = model.malletModel;
        Object[][] topicWords = savedMalletModel.getTopWords(savedMalletModel.numTypes);
        if (topicWords.length == xmlConfig.getInt("train.parameters.parameter.numTopics")) {
            LOGGER.info("Topic model verified.");
        } else {
            LOGGER.info("Topic model verification failed.");
        }
    }

    public void printTopicsToFile(File file, Model model) throws IOException {
        FileWriter writer = new FileWriter(file);
        BufferedWriter buffWriter = new BufferedWriter(writer);
        ParallelTopicModel malletParallelTopicModel = model.malletModel;
        Object[][] topicWords = malletParallelTopicModel.getTopWords(malletParallelTopicModel.numTypes);
        for (int i = 0; i < topicWords.length; i++) {
            buffWriter.write("Topic " + i + "\n");
            for (int j = 0; j < topicWords[i].length; j++) {
                buffWriter.write("Word " + j + ": " + topicWords[i][j] + "\n");
            }
            buffWriter.write("\n");
        }
        buffWriter.close();
        LOGGER.info("Topics written in " + file.getAbsolutePath() + ".");
    }
}
