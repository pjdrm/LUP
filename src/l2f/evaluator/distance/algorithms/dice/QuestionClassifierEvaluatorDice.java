package l2f.evaluator.distance.algorithms.dice;

import l2f.corpus.CorpusClassifier;
import l2f.corpus.processor.UtteranceProcessor;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.evaluator.distance.algorithms.DistanceAlgorithmArguments;
import l2f.evaluator.distance.algorithms.QuestionClassifierEvaluatorDistance;
import l2f.evaluator.distance.algorithms.set.intersection.SetIntersection;
import l2f.nlp.Tokenizer;


public class QuestionClassifierEvaluatorDice extends QuestionClassifierEvaluatorDistance{

	private SetIntersection setIntersection;
	
	public QuestionClassifierEvaluatorDice(CorpusClassifier cc, DistanceAlgorithmArguments args, Tokenizer tokenizer, int maxPredictions, int k_neighbours, SetIntersection setIntersection) {
		super(cc, args, tokenizer, maxPredictions, k_neighbours, setIntersection.toString());
		this.distanceAlg = new DiceAlgorithm(setIntersection);
		this.setIntersection = setIntersection;
	}
	
	public QuestionClassifierEvaluatorDice(UtteranceProcessor ut, DistanceAlgorithmArguments args, Tokenizer tokenizer, int maxPredictions, int k_neighbours, SetIntersection setIntersection) {
		super(ut, args, tokenizer, maxPredictions, k_neighbours, setIntersection.toString());
		this.distanceAlg = new DiceAlgorithm(setIntersection);
		this.setIntersection = setIntersection;
	}

	@Override
	public QuestionEvaluatorSet getType() {
		return QuestionEvaluatorSet.DICE;
	}
	
	@Override
	public String getDescription() {
		return "Dice -" + daArgs.getFeatureSet().getShortName() + "-" + utteranceProcessor.getDescription() + " k-neighbours " + k_neighbours + " SetIntersectionType " + setIntersectionDesc;
	}

	@Override
	public QuestionClassifierEvaluator clone() {
		return new QuestionClassifierEvaluatorDice(this.utteranceProcessor, this.daArgs, this.tokenizer, this.maxPredictions, this.k_neighbours, this.setIntersection);
	}
}
