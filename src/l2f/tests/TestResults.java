package l2f.tests;


public class TestResults implements Comparable<TestResults>{
	private double score;
	private double testSize;
	private Double accuracy;
	private String wrongAnswersText = "";
	private String interactions = "";
	private String qceDescription = "";
	private Double frameAccuracy = null;
	private Double attrAccuracy = null;
	
	public TestResults(double score, double testSize, String wrongAnswers, String interactions, String qceDescription){
		this.score = score;
		this.wrongAnswersText = wrongAnswers;
		this.interactions = interactions;
		this.qceDescription = qceDescription;
		this.testSize = testSize;
		this.accuracy = score/testSize;
	}
	
	public TestResults(double correctAnswers, int testSize, String wrongAnswers, String interactions, String qceDescription, Double frameAcc, Double attrAcc){
		this(correctAnswers, testSize, wrongAnswers, interactions, qceDescription);
		this.frameAccuracy = frameAcc;
		this.attrAccuracy = attrAcc;
	}
	
	public double getCorrectAnswers(){
		return score;
	}
	
	public double getTestSize(){
		return testSize;
	}
	
	public String getWrongAnswersText(){
		return wrongAnswersText;
	}
	
	public String getInteractions(){
		return interactions;
	}
	
	public String getQCEDescription(){
		return qceDescription;
	}
	
	public Double getAccuracy(){
		return accuracy;
	}
	
	public Double getFrameAccuracy(){
		return frameAccuracy;
	}
	
	public Double getAttributeAccuracy(){
		return attrAccuracy;
	}
	
	@Override
	public int compareTo(TestResults tr) {
		return (int) (tr.getAccuracy()*100000 - getAccuracy()*100000);
	}

}
