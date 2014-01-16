package l2f.evaluator.arguments;

public enum ClassifierSet {
	SVM("svm"),
	RULES("rules");
	
	private String classifierType;
	ClassifierSet(String classifierType){
		this.classifierType = classifierType;
	}
	
	public String classifierType(){return classifierType;}
}
