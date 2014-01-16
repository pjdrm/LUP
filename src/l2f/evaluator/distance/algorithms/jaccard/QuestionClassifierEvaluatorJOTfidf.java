package l2f.evaluator.distance.algorithms.jaccard;

import l2f.corpus.Corpus;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.processor.UtteranceProcessor;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.evaluator.distance.algorithms.DistanceAlgorithmArguments;
import l2f.evaluator.distance.algorithms.QuestionClassifierEvaluatorDistance;
import l2f.nlp.SimpleTokenizer;
import l2f.nlp.Tokenizer;

public class QuestionClassifierEvaluatorJOTfidf extends QuestionClassifierEvaluatorDistance{
	
	public QuestionClassifierEvaluatorJOTfidf(CorpusClassifier cc, DistanceAlgorithmArguments args, Tokenizer tokenizer, int maxPredictions, int k_neighbours) {
		super(cc, args, tokenizer, maxPredictions, k_neighbours, "None");
		this.distanceAlg = new JaccardOverlapTfidf(getTrainCorpus());
	}

	public QuestionClassifierEvaluatorJOTfidf(
			UtteranceProcessor utteranceProcessor,
			DistanceAlgorithmArguments args, Tokenizer tokenizer, int maxPredictions, int k_neighbours) {
		super(utteranceProcessor, args, tokenizer, maxPredictions, k_neighbours, "None");
		this.distanceAlg = new JaccardOverlapTfidf(getTrainCorpus());
	}

	@Override
	public QuestionEvaluatorSet getType() {
		return QuestionEvaluatorSet.JACCARDOVERLAPTFIDF;
	}
	
	@Override
	public String getDescription() {
		return "Jaccard Overlap TFIDF -" + daArgs.getFeatureSet().getShortName() + "-" + utteranceProcessor.getDescription()  + " k-neighbours " + k_neighbours;
	}
	
	@Override
	public void setCorpus(Corpus corpus) {
		super.setCorpus(corpus);
		this.distanceAlg = new JaccardOverlapTfidf(getTrainCorpus());
		
	}
	
	public QuestionClassifierEvaluator clone() {
		return new QuestionClassifierEvaluatorJOTfidf(this.utteranceProcessor, this.daArgs, this.tokenizer, this.maxPredictions, this.k_neighbours);
	}
}
