package l2f.evaluator.arguments;

public enum QuestionEvaluatorSet {
	DICE("dice"),
	OVERLAP("overlap"),
	JACCARD("jaccard"),
	JACCARDOVERLAP("jaccardOverlap"),
	JACCARDOVERLAPTFIDF("jotfidf"),
	SVM("svm"),
	CLM("clm"),
	CE("crossEntropy"),
	LR("logisticRegression"),
	LSA("lsa"),
	VSM("vsm"),
	IW("importantWords"),
	FRAMESFF("framesFF"),
	FRAMESAF("framesAF");
	
	private String type;
	QuestionEvaluatorSet(String type){
		this.type = type;
	}
	
	public String type(){return type;}
}
