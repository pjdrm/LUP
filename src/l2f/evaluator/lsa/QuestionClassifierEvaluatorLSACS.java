package l2f.evaluator.lsa;

import l2f.corpus.CorpusClassifier;
import l2f.corpus.processor.UtteranceProcessor;
import l2f.nlp.Tokenizer;
import l2f.tests.QCEBaseTester;

public class QuestionClassifierEvaluatorLSACS extends QuestionClassifierEvaluatorLSADP{

	public QuestionClassifierEvaluatorLSACS(CorpusClassifier corpus, 
			int ngramOrder, 
			int maxPredictions, 
			Tokenizer tokenizer,
			int maxFactors,
			double featureInit,
			double initialLearningRate,
			int annealingRate,
			double regularization,
			double minImprovement,
			int minEpochs,
			int maxEpochs) {
		super(corpus, ngramOrder, maxPredictions, tokenizer, maxFactors, featureInit, initialLearningRate, annealingRate, regularization, minImprovement, minEpochs, maxEpochs);
		this.desc = "LSA cosine similarity " + getNgramName() + 
				" Max fact- " + maxFactors +
				" Features init- " + featureInit +
				" Initial learning rate- " + initialLearningRate +
				" Annealing- "+ annealingRate +
				" Regularization- " + regularization +
				" Min improv- " + minImprovement +
				" Min epochs- " + minEpochs +
				" Max epochs- " +  maxEpochs +
				utteranceProcessor.getDescription();
		this.tester = new QCEBaseTester(this);
	}
	
	public QuestionClassifierEvaluatorLSACS(UtteranceProcessor up,
			int ngramOrder,
			int maxPredictions,
			Tokenizer tokenizer,
			int maxFactors,
			double featureInit,
			double initialLearningRate,
			int annealingRate,
			double regularization,
			double minImprovement,
			int minEpochs,
			int maxEpochs) {
		super(up, ngramOrder, maxPredictions, tokenizer, maxFactors, featureInit, initialLearningRate, annealingRate, regularization, minImprovement, minEpochs, maxEpochs);
		this.desc = "LSA cosine similarity " + getNgramName() + 
				" Max fact- " + maxFactors +
				" Features init- " + featureInit +
				" Initial learning rate- " + initialLearningRate +
				" Annealing- "+ annealingRate +
				" Regularization- " + regularization +
				" Min improv- " + minImprovement +
				" Min epochs- " + minEpochs +
				" Max epochs- " +  maxEpochs +
				utteranceProcessor.getDescription();
		this.tester = new QCEBaseTester(this);
	}

	@Override
	double vectorCalc(double[] xs, Double[] ys, double[] scales) {
		double product = 0.0;
	    double xsLengthSquared = 0.0;
	    double ysLengthSquared = 0.0;
	    for (int k = 0; k < xs.length; ++k) {
	        double sqrtScale = Math.sqrt(scales[k]);
	        double scaledXs = sqrtScale * xs[k];
	        double scaledYs = sqrtScale * ys[k];
	        xsLengthSquared += scaledXs * scaledXs;
	        ysLengthSquared += scaledYs * scaledYs;
	        product += scaledXs * scaledYs;
	    }
	    return product / Math.sqrt(xsLengthSquared * ysLengthSquared);
	}

	private static final long serialVersionUID = -2744791456062459042L;
}
