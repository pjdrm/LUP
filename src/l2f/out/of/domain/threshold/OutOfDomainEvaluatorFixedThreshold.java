package l2f.out.of.domain.threshold;

import l2f.evaluator.QCEAnswer;
import l2f.evaluator.distance.algorithms.QuestionClassifierEvaluatorDistance;
import l2f.out.of.domain.OutOfDomainEvaluator;
import l2f.out.of.domain.OutOfDomainResult;

public class OutOfDomainEvaluatorFixedThreshold extends OutOfDomainEvaluator{

	public double threshold;
	private QuestionClassifierEvaluatorDistance qceDistance;
	
	public OutOfDomainEvaluatorFixedThreshold(QuestionClassifierEvaluatorDistance qceDistance, double threshold) {
		super(qceDistance.getCorpus());
		this.qceDistance = qceDistance;
		this.threshold = threshold;
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
			System.out.println("Creating " + getDescription() + " classifier");
	}

	@Override
	public String getDescription() {
		return "FixedThreshold " + threshold + " " + qceDistance.getDescription();
	}

}
