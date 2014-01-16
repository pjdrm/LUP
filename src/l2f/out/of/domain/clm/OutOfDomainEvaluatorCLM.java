package l2f.out.of.domain.clm;

import l2f.AnswerTypeSet;
import l2f.evaluator.clm.QuestionClassifierEvaluatorCLM;
import l2f.out.of.domain.OutOfDomainEvaluator;
import l2f.out.of.domain.OutOfDomainResult;

public class OutOfDomainEvaluatorCLM extends OutOfDomainEvaluator{

	private QuestionClassifierEvaluatorCLM qceCLM;
	
	public OutOfDomainEvaluatorCLM(QuestionClassifierEvaluatorCLM qceCLM){
		super(qceCLM.getCorpus());
		this.qceCLM = qceCLM;
		this.qceCLM.setAlwaysAnswers(false);
	}
	
	@Override
	public OutOfDomainResult isOutOfDomain(String strUtterance) {
		String answer = qceCLM.answerQuestion(strUtterance).get(0);
		return new OutOfDomainResult(answer.equals(AnswerTypeSet.OUTDOMAIN.type()));
	}

	@Override
	public void run() {
	}

	@Override
	public String getDescription() {
		return "CLM";
	}

}
