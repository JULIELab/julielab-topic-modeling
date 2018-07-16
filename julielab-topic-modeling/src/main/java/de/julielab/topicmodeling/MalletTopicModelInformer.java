package de.julielab.topicmodeling;

import cc.mallet.topics.ParallelTopicModel;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.services.MalletTopicModeling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class MalletTopicModelInformer {
    public static void main(String args[]) throws FileNotFoundException, UnsupportedEncodingException {
        if (args.length != 2) {
            System.err.println("Usage: " + MalletTopicModelInformer.class.getSimpleName() + " <model path> <topic-document-output-file>");
            System.exit(1);
        }
        MalletTopicModeling malletTopicModeling = new MalletTopicModeling();
        Model model = malletTopicModeling.readModel(args[0]);
        printTopicsPerDocumentToFile(new File(args[1]), model);
    }

    public static void printTopicsPerDocumentToFile(File file, Model model) throws FileNotFoundException, UnsupportedEncodingException {
        ParallelTopicModel malletModel = model.malletModel;
        try (PrintWriter pw = new PrintWriter(file, "UTF-8")) {
            malletModel.printDenseDocumentTopics(pw);
        }
    }
}
