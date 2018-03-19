/**
 * XMLReaderTest.java
 *
 * Copyright (c) 2015, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD-2-Clause License
 *
 * Author: muehlhausen
 *
 * Current version: 1.9
 * Since version:   1.0
 *
 * Creation date: Dec 11, 2006
 *
 * Test for class de.julielab.jcore.reader.XMLReader
 **/

package de.julielab.jcore.reader;

import junit.framework.TestCase;

import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.jcore.reader.xml.XMLMultiplier;

/**
 * Test for class XML Reader
 */
public class XMLMultiplierTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLMultiplierTest.class);

    private static final boolean DEBUG_MODE = true;

    /**
     * Path to the MedlineReader descriptor without inputDir parameter (and no single file attribute)
     */
    private static final String DESC_XML_MULTIPLIER_DIR = "src/test/resources/PubmedXMLMultiplierDescriptor";

    /**
     * Default constructor
     */
    public XMLMultiplierTest() {
        super();
        if (DEBUG_MODE) {
            LOGGER.info("XMLReader test is in DEBUG_MODE !!!!!!!!!!!!");
        }
    }

    /**
     * Object to be tested
     */
    private AnalysisEngine xmlMultiplier;
    
        /**
     * Test main functionality of the {@link CollectionReader}
     *
     * @throws ResourceInitializationException
         * @throws IOException 
         * @throws InvalidXMLException 
     */
    public void testProcessSingleFile() throws ResourceInitializationException, InvalidXMLException, IOException {
//        TypeSystemDescription tsDesc = TypeSystemDescriptionFactory.createTypeSystemDescription(
//                "de.julielab.jcore.types.FileTypeSystemDescription");
        
        xmlMultiplier = AnalysisEngineFactory.createEngine(DESC_XML_MULTIPLIER_DIR, 
        		XMLMultiplier.PARAM_INPUT_DIR, "src/test/resources/pubmedXML",
                XMLMultiplier.PARAM_MAPPING_FILE, "src/test/resources/medlineMappingFile.xml",
                XMLMultiplier.PARAM_INPUT_FILE, "src/test/resources/pubmedXML/pubmedsample18n0001copy.xml");
        try {
//        JCas cas = JCasFactory.createJCas("de.julielab.jcore.types.FileTypeSystemDescriptor");
        JCas cas = JCasFactory.createJCas("src/test/resources/FileTypeSystemDescriptor");	
        xmlMultiplier.process(cas);
        System.out.println(cas.getDocumentText());
//        assertEquals(expectedText, cas.getDocumentText());
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    public void testProcessDirectory() throws ResourceInitializationException, InvalidXMLException, IOException {
      
      xmlMultiplier = AnalysisEngineFactory.createEngine(DESC_XML_MULTIPLIER_DIR, 
      		XMLMultiplier.PARAM_INPUT_DIR, "src/test/resources/pubmedXML",
              XMLMultiplier.PARAM_MAPPING_FILE, "src/test/resources/medlineMappingFile.xml");
      try {
      JCas cas = JCasFactory.createJCas("src/test/resources/FileTypeSystemDescriptor");	
      xmlMultiplier.process(cas);
      System.out.println(cas.getDocumentText());
      } catch (Exception e) {
      	e.printStackTrace();
      }
  }
}
