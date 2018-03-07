package de.julielab.UsingModel;

import de.julielab.UsingModel.txtJTMConfig;
import de.julielab.services.IConfig;
import de.julielab.services.IDocument;

public class pubmedXML2instances implements IDocument {

	public pubmedXML2instances() {
		
	}
	
	@Override 
	public txtJTMConfig getConfig() {
		txtJTMConfig config;
		return config;
	}
	
	@Override 
	public IDocument transformDocument() {
		IDocument document;
		return document;
	}
	
	@Override
	public void model(IConfig config) {
		
	}
}
