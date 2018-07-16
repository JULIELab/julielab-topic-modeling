package de.julielab.topicmodeling;

import cc.mallet.topics.ParallelTopicModel;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.services.MalletTopicModeling;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MalletTopicModelInformer {
    public static void main(String args[]) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: " + MalletTopicModelInformer.class.getSimpleName() + " <mode: doctopics|docIdMap> <model path> <output-file>");
            System.exit(1);
        }
        MalletTopicModeling malletTopicModeling = new MalletTopicModeling();
        Model model = malletTopicModeling.readModel(args[1]);
        if (args[0].equalsIgnoreCase("doctopics"))
            printTopicsPerDocumentToFile(new File(args[2]), model);
        else if (args[0].equalsIgnoreCase("docIdMap"))
            printDocumentIdMap(new File(args[2]), model);
    }

    private static void printDocumentIdMap(File file, Model model) throws IOException {
        HashMap<Integer, String> modelIdpubmedId = model.ModelIdpubmedId;
        try (BufferedWriter bw = FileUtilities.getWriterToFile(file)) {
            for (Map.Entry<Integer, String> entry : modelIdpubmedId.entrySet()) {
                bw.write(entry.getKey() + "\t" + entry.getValue());
                bw.newLine();
            }
        }
    }

    public static void printTopicsPerDocumentToFile(File file, Model model) throws FileNotFoundException, UnsupportedEncodingException {
        ParallelTopicModel malletModel = model.malletModel;
        try (PrintWriter pw = new PrintWriter(file, "UTF-8")) {
            malletModel.printDenseDocumentTopics(pw);
        }
    }
}
