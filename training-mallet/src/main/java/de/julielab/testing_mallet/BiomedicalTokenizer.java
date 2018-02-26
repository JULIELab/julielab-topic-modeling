package de.julielab.testing_mallet;

import java.io.File;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
import cc.mallet.util.CharSequenceLexer;
import cc.mallet.pipe.CharSequence2TokenSequence;
import de.julielab.jcore.ae.jtbd.Tokenizer;

public class BiomedicalTokenizer extends Pipe {

	public BiomedicalTokenizer() {
		
	}

	public Instance pipe(Instance inst) {
//		ClassLoader classLoader = getClass().getClassLoader();
		Object tokenList = null;
		CharSequence token = null;
		try {
//			CharSequence charData = (CharSequence) inst.getData();
			File model = new File ("D:/jtbd-biomed-original_mallet.gz");
			Tokenizer tok = new Tokenizer();
			tok.readModel(model);
//			String stringData = charData.toString();
			tokenList = tok.predict((String) inst.getData());
			token = (CharSequence) tokenList;
//			TokenSequence tokenSeq = (TokenSequence) tokenList;
//			inst.setData(tokenList);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		inst.setData(tokenList);
		inst.setData(token);
		return inst;
	}
	
//	public BiomedicalTokenizer(Alphabet dataDict, Alphabet targetDict) {
//		super(dataDict, targetDict);
//		
//		Tokenizer tok = new Tokenizer();
//		File model = new File ("jtbd-biomed-original_mallet.gz");
//		int[] indices;
//		
//		String data = dataDict.lookupObjects(indices);
//		tok.readModel(model);
//		tok.predict(data);
//	}

	// Serialization 
	
	private static final long serialVersionUID = 1;
	
}
