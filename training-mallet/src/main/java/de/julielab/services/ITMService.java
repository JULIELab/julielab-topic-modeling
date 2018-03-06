package de.julielab.services;

import java.util.List;

public interface ITMService {
	
	LabelledDocument label(List<?> queryResults);
	
	Result search(String query);

	Model modelFraction(String topic);
}