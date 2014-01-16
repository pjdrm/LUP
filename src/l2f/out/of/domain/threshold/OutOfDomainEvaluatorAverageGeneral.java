package l2f.out.of.domain.threshold;

import java.text.DecimalFormat;

import l2f.corpus.Utterance;
import l2f.evaluator.QCEAnswer;
import l2f.evaluator.distance.algorithms.DistanceAlgorithm;
import l2f.evaluator.distance.algorithms.NgramGenerator;
import l2f.evaluator.distance.algorithms.QuestionClassifierEvaluatorDistance;
import l2f.nlp.SimpleTokenizer;
import l2f.nlp.Tokenizer;
import l2f.out.of.domain.OutOfDomainEvaluator;
import l2f.out.of.domain.OutOfDomainResult;

public class OutOfDomainEvaluatorAverageGeneral extends OutOfDomainEvaluator{
	public double threshold;
	private QuestionClassifierEvaluatorDistance qceDistance;
	private DistanceAlgorithm distanceAlgorithm;
	private int nGramOrder;
	private DecimalFormat df = new DecimalFormat("#.##");
	
	public OutOfDomainEvaluatorAverageGeneral(QuestionClassifierEvaluatorDistance qceDistance) {
		super(qceDistance.getCorpus());
		this.qceDistance = qceDistance;
		this.distanceAlgorithm = qceDistance.getDistanceAlgorithm();
		this.nGramOrder = qceDistance.getNgramOrder();
	}
	
	@Override
	public OutOfDomainResult isOutOfDomain(String strUtterance) {
		strUtterance = getCorpus().getUtteranceProcessor().processString(strUtterance);
		boolean isOutOfDomain;
		QCEAnswer qceAnswer = qceDistance.answerWithQCEAnswer(strUtterance);
		String cat = qceAnswer.getPossibleAnswers().get(0).getCat();
		String debug = qceAnswer.getPossibleAnswers().get(0).getUtterance();
		debug = "CAT " + cat + " " + debug.substring(debug.indexOf('\n')+1);
		Double score = qceAnswer.getScore();
		if(score >= threshold)
			isOutOfDomain = false;
		else
			isOutOfDomain = true;
		
		return new OutOfDomainResult(isOutOfDomain, score, threshold, debug);
	}

	@Override
	public void run() {
		Double total = 0.0;
		Utterance currentUtterance;
		Utterance ut;
		Tokenizer tokenizer = new SimpleTokenizer();
		double nComparisons = 0.0;
		int j;
		System.out.println("Creating " + getDescription() + " classifier");
		for(int i = 0; i < corpus.getTrainUtterances().size(); i++){
			currentUtterance = corpus.getTrainUtterances().get(i);
			j = i;
			for(; j < corpus.getTrainUtterances().size(); j++){
				if(i == j)
					continue;
				ut = corpus.getTrainUtterances().get(j);
				total += distanceAlgorithm.distance(NgramGenerator.getNGrams(nGramOrder, tokenizer.tokenize(ut.getUtterance())), NgramGenerator.getNGrams(nGramOrder, tokenizer.tokenize(currentUtterance.getUtterance())));
				nComparisons++;
			}
		}
		threshold = total / nComparisons;
		System.out.println("Threshold: " + threshold + " " + total + " " + nComparisons);
	}

	@Override
	public String getDescription() {
		return "AverageGeneral " + df.format(threshold) + " " + qceDistance.getDescription();
	}
}
