package l2f.evaluator.vsm;

import l2f.corpus.Utterance;

public class VectorUtterance implements Comparable<VectorUtterance>{
	private Utterance utterance;
	private double[] vectorUtteranceRepresentation;
	private double[] vectorAnswerRepresentation;
	private double score;
	private double originalScore;
	private double highestScore;
	
	public VectorUtterance(Utterance utterance, double[] vectorUtteranceRepresentation, double[] vectorAnswerRepresentation){
		this.utterance = utterance;
		this.vectorUtteranceRepresentation = vectorUtteranceRepresentation;
		this.vectorAnswerRepresentation = vectorAnswerRepresentation;
	}
	
	public Utterance getUtterance(){
		return utterance;
	}
	
	public double[] getAnswerVectorRepresentation(){
		return vectorAnswerRepresentation;
	}
	
	public double[] getUtteranceVectorRepresentation(){
		return vectorUtteranceRepresentation;
	}
	
	public double getScore(){
		return score;
	}
	
	public void setScore(double score){
		this.score = score;
	}
	
	public void setOriginalScore(double score){
		this.originalScore = score;
	}
	
	public double getOriginalScore(){
		return originalScore;
	}
	public double getHighestScore(){
		//get score is returning the number of neighbours in the same cat!
		return highestScore/getScore();
	}
	
	public void highestScore(double score){
		if(score > highestScore)
			this.highestScore = score;
	}
	
	@Override
	public int compareTo(VectorUtterance arg0) {
		int dif = (int) (arg0.getScore()*100000 - getScore()*100000);
		if(dif != 0)
			return dif;
		else
			return (int) (arg0.getHighestScore()*100000 - getHighestScore()*100000);
	}
}
