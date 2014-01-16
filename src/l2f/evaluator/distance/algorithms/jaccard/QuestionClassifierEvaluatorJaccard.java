package l2f.evaluator.distance.algorithms.jaccard;

import l2f.corpus.CorpusClassifier;
import l2f.corpus.processor.UtteranceProcessor;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.evaluator.distance.algorithms.DistanceAlgorithmArguments;
import l2f.evaluator.distance.algorithms.QuestionClassifierEvaluatorDistance;
import l2f.evaluator.distance.algorithms.dice.QuestionClassifierEvaluatorDice;
import l2f.evaluator.distance.algorithms.set.intersection.SetIntersection;
import l2f.nlp.SimpleTokenizer;
import l2f.nlp.Tokenizer;

public class QuestionClassifierEvaluatorJaccard extends QuestionClassifierEvaluatorDistance{
	private SetIntersection setIntersection;
	
	public QuestionClassifierEvaluatorJaccard(CorpusClassifier cc, DistanceAlgorithmArguments args, Tokenizer tokenizer, int maxPredictions, int k_neighbours, SetIntersection setIntersection) {
		super(cc, args, tokenizer, maxPredictions, k_neighbours, setIntersection.toString());
		this.setIntersection = setIntersection;
		this.distanceAlg = new JaccardAlgorithm(setIntersection);
	}

	public QuestionClassifierEvaluatorJaccard(UtteranceProcessor utteranceProcessor, DistanceAlgorithmArguments args, Tokenizer tokenizer, int maxPredictions, int k_neighbours, SetIntersection setIntersection) {
		super(utteranceProcessor, args, tokenizer, maxPredictions, k_neighbours, setIntersection.toString());
		this.setIntersection = setIntersection;
		this.distanceAlg = new JaccardAlgorithm(setIntersection);
	}

	@Override
	public QuestionEvaluatorSet getType() {
		return QuestionEvaluatorSet.JACCARD;
	}
	
	@Override
	public String getDescription() {
		return "Jaccard -" + daArgs.getFeatureSet().getShortName() + "-" + utteranceProcessor.getDescription()  + " k-neighbours " + k_neighbours + " SetIntersectionType " + setIntersectionDesc;
	}
	
	@Override
	public QuestionClassifierEvaluator clone() {
		return new QuestionClassifierEvaluatorJaccard(this.utteranceProcessor, this.daArgs, this.tokenizer, this.maxPredictions, this.k_neighbours, this.setIntersection);
	}
}
