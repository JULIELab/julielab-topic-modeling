# julielab-topic-modeling
This is the master thesis implementation work of Philipp Sieg. The major goal is to provide a software package for 
1. generating topic models, 
2. indexing documents with models, 
3. using the indexes for information retrieval, and 
4. evaluating models both qualitatively and quantitatively. 

This implementation of a topic modeling module consists of 5 components: 

### julielab-topic-modeling
This is the main project and the main dependency for the other projects. It implements the major functions for reading,
preprocessing, training, and infering labels to documents. It contains the TopicModelGenerator, a commandline-tool for generating
topic models on a given data set. The data set my be read from a file system or a database.

Additionally, the project provides configuration templates you can find here: https://github.com/JULIELab/julielab-topic-modeling/tree/master/configs

### julielab-tm-heldout-evaluator

The folder tm-search-components contains the final versions of each component. 
