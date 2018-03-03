package de.julielab.UsingModel;

import de.julielab.interfaces.IConfig;
import de.julielab.interfaces.IDocument;
import de.julielab.UsingModel.txtJTMConfig;

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
