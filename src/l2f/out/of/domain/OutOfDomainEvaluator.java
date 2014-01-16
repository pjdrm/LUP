package l2f.out.of.domain;

import l2f.corpus.Corpus;

public abstract class OutOfDomainEvaluator {
	protected Corpus corpus;

	public OutOfDomainEvaluator(){
		
	}
	
	public OutOfDomainEvaluator(Corpus corpus){
		this.corpus = corpus;
	}
	
	public abstract  OutOfDomainResult isOutOfDomain(String strUtterance);
	
	public abstract void run();
	
	public abstract String getDescription();
	
	public Corpus getCorpus(){
		return corpus;
	}
}
