package de.julielab.testing_mallet;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Alphabet;
//import cc.mallet.types.Instance;
//import edu.ucdenver.ccp.nlp.biolemmatizer.BioLemmatizer;
//import edu.ucdenver.ccp.nlp.biolemmatizer.LemmataEntry;

public class BiomedicalLemmatizer extends Pipe {

	public BiomedicalLemmatizer() {
		
	}
	
//	public Instance pipe(Instance inst) {
//		BioLemmatizer bioLem = new BioLemmatizer ();
//		LemmataEntry lemma = bioLem.lemmatizeByLexicon(inst.getData().toString(), null);
//		inst.setData(lemma);
//		return inst;
//	}

	public BiomedicalLemmatizer(Alphabet dataDict, Alphabet targetDict) {
		super(dataDict, targetDict);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1;
}
