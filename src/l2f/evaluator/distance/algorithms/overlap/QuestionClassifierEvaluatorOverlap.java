package l2f.evaluator.distance.algorithms.overlap;

import l2f.corpus.CorpusClassifier;
import l2f.corpus.processor.UtteranceProcessor;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.evaluator.distance.algorithms.DistanceAlgorithmArguments;
import l2f.evaluator.distance.algorithms.QuestionClassifierEvaluatorDistance;
import l2f.evaluator.distance.algorithms.set.intersection.SetIntersection;
import l2f.nlp.Tokenizer;

public class QuestionClassifierEvaluatorOverlap extends QuestionClassifierEvaluatorDistance{
	private SetIntersection setIntersection;

	public QuestionClassifierEvaluatorOverlap(CorpusClassifier cc, DistanceAlgorithmArguments args, Tokenizer tokenizer, int maxPredictions, int k_neighbours, SetIntersection setInterstion) {
		super(cc, args, tokenizer, maxPredictions, k_neighbours, setInterstion.toString());
		this.setIntersection = setInterstion;
		this.distanceAlg = new OverlapAlgorithm(setInterstion);
	}

	public QuestionClassifierEvaluatorOverlap(
			UtteranceProcessor utteranceProcessor,
			DistanceAlgorithmArguments args, Tokenizer tokenizer, int maxPredictions, int k_neighbours, SetIntersection setInterstion) {
		super(utteranceProcessor, args, tokenizer, maxPredictions, k_neighbours, setInterstion.toString());
		this.setIntersection = setInterstion;
		this.distanceAlg = new OverlapAlgorithm(setInterstion);
	}

	@Override
	public QuestionEvaluatorSet getType() {
		return QuestionEvaluatorSet.OVERLAP;
	}

	@Override
	public String getDescription() {
		return "Overlap -" + daArgs.getFeatureSet().getShortName() + "-" + utteranceProcessor.getDescription()  + " k-neighbours " + k_neighbours + " SetIntersectionType " + setIntersectionDesc;
	}

	public QuestionClassifierEvaluator clone() {
		return new QuestionClassifierEvaluatorOverlap(this.utteranceProcessor, this.daArgs, this.tokenizer, this.maxPredictions, this.k_neighbours, this.setIntersection);
	}
}
