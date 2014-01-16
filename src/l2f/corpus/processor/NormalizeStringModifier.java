package l2f.corpus.processor;

import l2f.nlp.NormalizerSimple;

public class NormalizeStringModifier extends StringModifier{

	@Override
	public String modify(String str) {
		return NormalizerSimple.normPunctLCaseDMarks(str);
	}

	@Override
	public String getDescription() {
		return "NormalizeString";
	}

}
