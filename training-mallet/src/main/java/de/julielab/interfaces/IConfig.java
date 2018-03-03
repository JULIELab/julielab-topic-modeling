package de.julielab.interfaces;

import java.io.File;

public interface IConfig {
	
	void setConfig();
	
	String readConfig(File config);
}
