# julielab-topic-modeling
This is the master thesis implementation work of Philipp Sieg. The major goal is to provide a software package for 
1. generating topic models, 
2. indexing documents with models, 
3. using the indexes for information retrieval, and 
4. evaluating models both qualitatively and quantitatively. 

The folder tm-search-components contains the final versions of each component. The topic modeling functionality depends strongly on 
an implementation from the Machine Learning for Language Toolkit (MALLET, McCallum 2002). The NLP preprocessing components are 
taken from JCoRe (Hahn et al. 2016).
The implementation of this topic modeling module consists of 5 components: 

#### julielab-topic-modeling
This is the main project and the main dependency for the other projects. It implements the major functions for reading,
preprocessing, training, and infering labels to documents. It contains the TopicModelGenerator, a commandline-tool for generating
topic models on a given data set. The data set my be read from a file system or a database.

#### julielab-tm-heldout-evaluator
This is a simple commandline-tool for calculating and monitoring a held-out document evaluation. It uses the MALLET implementation of the left-to-right algorithm developed by Wallach (2008).

#### jcore-topic-indexing-ae


Additionally, the project provides configuration templates you can find here: https://github.com/JULIELab/julielab-topic-modeling/tree/master/configs

### References

Hahn, Udo/Matthies, Franz/Faessler, Erik/Hellrich, Johannes (2016): UIMA-Based JCoRe 2.0 	Goes GitHub and Maven Central ― State-of-the-Art Software Resource Engineering 	and Distribution of NLP Pipelines. In: Nicoletta Calzolari (Conference Chair), Khalid 	Choukri, Thierry Declerck, Marko Grobelnik, Bente Maegaard, Joseph Mariani, 	Asuncion Moreno, Jan Odijk, Stelios Piperidis (Hgg.): Proceedings of the Tenth 	International Conference on Language Resources and Evaluation (LREC 2016),. 	Portorož, Slovenia, S. 2502-2509.

McCallum, Andrew Kachites (2002): MALLET: A Machine Learning for Language Toolkit. http://mallet.cs.umass.edu.

Wallach, Hanna M. (2008): Structured topic models for language. PhD thesis, University of 	Cambridge.

Wallach, Hanna M./Murray, Iain/Salakhutdinov, Ruslan/Mimno, David (2009): Evaluation 	methods for topic models. In: Proceedings of the International Conference of Machine 	Learning, S. 1105-1112.

