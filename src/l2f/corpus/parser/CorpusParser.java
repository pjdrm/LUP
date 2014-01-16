package l2f.corpus.parser;

import l2f.corpus.CorpusClassifier;

public interface CorpusParser {
	public CorpusClassifier parseCorpus(String corpusPath, String corpusProperties);
	public boolean canProcessCorpus(String corpusPath);
}
