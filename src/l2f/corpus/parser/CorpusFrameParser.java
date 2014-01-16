package l2f.corpus.parser;

import l2f.corpus.CorpusFrameClassifier;

public interface CorpusFrameParser {
	public CorpusFrameClassifier parseCorpus(String corpusPath);
	public boolean canProcessCorpus(String corpusPath);
}
