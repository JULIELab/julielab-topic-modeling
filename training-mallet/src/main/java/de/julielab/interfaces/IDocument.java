package de.julielab.interfaces;

public interface IDocument {

	IDocument transformDocument();
	
	IConfig getConfig();
	
	void model(IConfig config);
}
