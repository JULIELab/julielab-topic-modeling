/** 
 * MedlineReader.java
 * 
 * Copyright (c) 2015, JULIE Lab.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the BSD-2-Clause License
 * 
 * Author: muehlhausen
 * 
 * Current version: 1.12
 * Since version:   1.0
 *
 * Creation date: Dec 11, 2006 
 * 
 * CollectionReader for MEDLINE (www.pubmed.gov) abstracts in XML that initializes some information in the CAS.
 **/

package de.julielab.jcore.reader.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasMultiplier_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnnotationFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.jcore.reader.xmlmapper.mapper.XMLMapper;
import de.julielab.jcore.types.Header;
import de.julielab.xml.JulieXMLConstants;
import de.julielab.xml.JulieXMLTools;

/**
 * CollectionReader for MEDLINE (www.pubmed.gov) Abstracts in XML that
 * initializes some information in the CAS
 * 
 * @author muehlhausen
 */

public class XMLMultiplier extends JCasMultiplier_ImplBase {

	/**
	 * UIMA Logger for this class
	 */
	private static Logger LOGGER = LoggerFactory.getLogger(XMLMultiplier.class);

	/**
	 * Configuration parameter defined in the descriptor. Name of configuration
	 * parameter that must be set to the path of a directory containing input
	 * files.
	 */
	public static final String PARAM_INPUT_DIR = "InputDirectory";

	/**
	 * Configuration parameter defined in the descriptor
	 */
	public static final String PARAM_INPUT_FILE = "InputFile";

	/**
	 * Configuration parameter defined in the descriptor
	 */
	public static final String PARAM_MAPPING_FILE = "MappingFile";

	/**
	 * Configuration parameter defined in the descriptor. Defaults to
	 * "de.julielab.jcore.types.Header". Must be assignment compatible to
	 * "de.julielab.jcore.types.Header". Only required when no header is built
	 * by the XML mapper.
	 */
	public static final String PARAM_HEADER_TYPE = "HeaderType";
	
	/**
	 * Configuration parameter defined in the descriptor. The Xpath defines 
	 * in what XML fractions the documents shall be splitted. 
	 */
	public static final String PARAM_FOR_EACH = "DocumentXpath";

	/**
	 * List of all files with abstracts XML
	 */
	private File[] files;

	/**
	 * Current file number
	 */
	private int currentIndex = 0;

	/**
	 * Mapper which maps XML to a cas with the jules type system via an XML
	 * configuration file.
	 */
	private XMLMapper xmlMapper;
	
	/**
	 * Detects the XML fractions 
	 */
	private Iterator<Map<String, Object>> rowIterator;
	
	/**
	 * Passes parameters to initialize() 
	 */
	@org.apache.uima.fit.descriptor.ConfigurationParameter(name = PARAM_MAPPING_FILE)
	private String mappingFileStr;
	@org.apache.uima.fit.descriptor.ConfigurationParameter(name = PARAM_FOR_EACH)
	private String forEach;

	@org.apache.uima.fit.descriptor.ConfigurationParameter(name = PARAM_HEADER_TYPE, mandatory = false)
	private String headerTypeName;
	@ConfigurationParameter(name = PARAM_INPUT_DIR, mandatory = false)
	private String directoryName;
	@org.apache.uima.fit.descriptor.ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = false)
	private String isSingleFileProcessing;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.uima.collection.CollectionReader_ImplBase#initialize()
	 */
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {

		LOGGER.debug("initialize() - Initializing XML Multiplier...");

		super.initialize(aContext);
		
		headerTypeName = (String) aContext.getConfigParameterValue(PARAM_HEADER_TYPE);
		if (null == headerTypeName)
			headerTypeName = "de.julielab.jcore.types.Header";
		mappingFileStr = (String) aContext.getConfigParameterValue(PARAM_MAPPING_FILE);
		forEach = (String) aContext.getConfigParameterValue(PARAM_FOR_EACH);
		InputStream is = null;

		LOGGER.info("Header type set to {}. A header of this type is only created if no header is created using the XML mapping file.", headerTypeName);
		LOGGER.info("Mapping file is searched as file or classpath resource at {}", mappingFileStr);

		File mappingFile = new File(mappingFileStr);
		if (mappingFile.exists()) {
			try {
				is = new FileInputStream(mappingFile);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				throw new ResourceInitializationException(e1);
			}
		} else {
			if (!mappingFileStr.startsWith("/"))
				mappingFileStr = "/" + mappingFileStr;

			is = getClass().getResourceAsStream(mappingFileStr);
			if (is == null) {
				throw new IllegalArgumentException(
						"MappingFile "
								+ mappingFileStr
								+ " could not be found as a file or on the classpath (note that the prefixing '/' is added automatically if not already present for classpath lookup)");
			}
		}

		try {
			xmlMapper = new XMLMapper(is);
		} catch (FileNotFoundException e) {
			throw new ResourceInitializationException(e);
		} catch (IOException e) {
			e.printStackTrace();
		}

		files = getFilesFromInputDirectory(aContext);
	}

	@Override
	public void process(JCas cas) throws AnalysisEngineProcessException {

		File file = files[currentIndex++];

		LOGGER.debug("process(JCas) - Reading file " + file.getName());
		String fileName = file.getAbsolutePath();
		String[] fieldPaths = new String [1];
		fieldPaths[0] = ".";
		List<Map<String, String>> fields = new ArrayList<>();
		for (int i = 0; i < fieldPaths.length; i++) {
			String path = fieldPaths[i];
			Map<String, String> field = new HashMap<String, String>();
			field.put(JulieXMLConstants.NAME, "fieldvalue" + i);
			field.put(JulieXMLConstants.XPATH, path);
			fields.add(field);
		};
		rowIterator = JulieXMLTools.constructRowIterator(fileName, 1024, forEach, fields, false);

		try {
			xmlMapper.parse(file, cas);
			// if PMID filed was empty, set File Name

		} catch (Exception e) {
			LOGGER.error("Exception in process(): file: " + file, e);
			try {
				throw new CollectionException(e);
			} catch (CollectionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		try {
			checkHeader(file.getName(), cas);
		} catch (CollectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public AbstractCas next() throws AnalysisEngineProcessException {
		JCas cas = getEmptyJCas();
		Map<String, Object> row = rowIterator.next();
		String xmlFragment = (String) row.get("fieldvalue" + 0);
		cas.setDocumentText(xmlFragment);
		System.out.println(cas.getDocumentText());
		return cas;
	}

	@Override
	public boolean hasNext() throws AnalysisEngineProcessException {
		if (rowIterator.hasNext()) {
			return true;
		} else {
			return false;
		}
	}


	@SuppressWarnings("unchecked")
	private void checkHeader(String name, JCas cas) throws CollectionException {
		Type headerType = cas.getTypeSystem().getType(headerTypeName);
		if (null == headerType)
			throw new CollectionException(CASException.JCAS_INIT_ERROR, new Object[] { "Header type \"" + headerTypeName
					+ "\" could not be found in the type system." });
		FSIterator<Annotation> headerIter = cas.getAnnotationIndex(headerType).iterator();
		if (headerIter.hasNext()) {
			try {
				Header header = (Header) headerIter.next();
				if (header.getDocId() == null || header.getDocId() == "" || header.getDocId() == "-1") {
					header.setDocId(name);
				}
			} catch (ClassCastException e) {
				LOGGER.error("Configured header type is {}. However, the header type must be assignment compatible to de.julielab.jcore.types.Header.",
						headerTypeName);
				throw new CollectionException(e);
			}

		} else {
			Class<? extends Header> headerClass;
			try {
				headerClass = (Class<? extends Header>) Class.forName(headerTypeName);
			} catch (ClassNotFoundException e) {
				LOGGER.error("Header type class {} could not be found.", headerTypeName);
				throw new CollectionException(e);
			}
			Header header = AnnotationFactory.createAnnotation(cas, 0, 0, headerClass);
			header.setDocId(name);
			header.addToIndexes();
		}

	}

	/**
	 * Get files from directory that is specified in the configuration parameter
	 * PARAM_INPUTDIR of the collection reader descriptor.
	 * 
	 * @throws ResourceInitializationException
	 *             thrown if there is a problem with a configuration parameter
	 */
	private File[] getFilesFromInputDirectory(UimaContext aContext) throws ResourceInitializationException {

		currentIndex = 0;
		if (isSingleProcessing(aContext)) {
			return getSingleFile(aContext);
		}
		directoryName = (String) aContext.getConfigParameterValue(PARAM_INPUT_DIR);
		LOGGER.debug(PARAM_INPUT_DIR + "=" + directoryName);
		if (directoryName == null) {
			throw new ResourceInitializationException(ResourceInitializationException.RESOURCE_DATA_NOT_VALID, new Object[] { "null", PARAM_INPUT_DIR });
		}
		File inputDirectory = new File(directoryName.trim());
		if (!inputDirectory.exists() || !inputDirectory.isDirectory()) {
			throw new ResourceInitializationException(ResourceInitializationException.RESOURCE_DATA_NOT_VALID, new Object[] { directoryName, PARAM_INPUT_DIR });
		}
		return inputDirectory.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml") || name.endsWith(".xml.gz");
			}
		});
	}

	/**
	 * Get a list of the single file defined in the descriptor
	 * 
	 * @return
	 * @throws ResourceInitializationException
	 */
	private File[] getSingleFile(UimaContext aContext) throws ResourceInitializationException {

		LOGGER.info("getSingleFile() - XMLMultiplier is used in SINGLE FILE mode.");
		String singleFile = (String) aContext.getConfigParameterValue(PARAM_INPUT_FILE);

		if (singleFile == null) {
			return null;
		}
		File file = new File(singleFile.trim());
		if (!file.exists() || file.isDirectory()) {
			throw new ResourceInitializationException(ResourceInitializationException.RESOURCE_DATA_NOT_VALID,
					new Object[] { "file does not exist or is a directory" + PARAM_INPUT_FILE });
		}
		File[] fileList = new File[1];
		fileList[0] = file;
		return fileList;
	}

	/**
	 * Determines form the descriptor if this CollectionReader should only
	 * process a single file. . <b>The parameter must not have a value! It is
	 * sufficient to be defined to return <code>true</code></b>
	 * 
	 * @return <code>true</code> if there is a parameter defined called like the
	 *         value of PARAM_INPUT_FILE
	 */
	private boolean isSingleProcessing(UimaContext aContext) {

		Object value = aContext.getConfigParameterValue(PARAM_INPUT_FILE);
		if (null != value)
			isSingleFileProcessing = (String) value;
		return isSingleFileProcessing != null;
	}

	/**
	 * @see org.apache.uima.collection.CollectionReader#hasNext()
	 */
//	public boolean hasNext() throws IOException, CollectionException {
//		return currentIndex < files.length;
//	}

	/**
	 * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#getProgress()
	 */
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(currentIndex, files.length, Progress.ENTITIES) };
	}

	/**
	 * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#close()
	 */
	public void close() throws IOException {
	}
}
