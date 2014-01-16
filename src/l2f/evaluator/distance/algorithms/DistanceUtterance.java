package l2f.evaluator.distance.algorithms;

import java.util.List;

public class DistanceUtterance implements Comparable<DistanceUtterance>{
	private List<String> nGramUtterance;
	private String cat;
	private double score = 0.0;
	private double originalScore;
	private double highestScore;
	
	public DistanceUtterance(List<String> nGramUtterance, String cat){
		this.nGramUtterance = nGramUtterance;
		this.cat = cat;
	}
	
	public DistanceUtterance(List<String> nGramUtterance, String cat, double score){
		this(nGramUtterance, cat);
		this.score = score;
		this.originalScore = score;
		this.highestScore = score;
	}
	
	public double getScore(){
		return score;
	}
	
	public void setScore(double score){
		this.score = score;
	}
	
	public void setOriginalScore(double score){
		this.originalScore = score;
		this.highestScore = score;
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
	
	public List<String> getNGramUtterance(){
		return nGramUtterance;
	}
	
	public String getCat(){
		return cat;
	}

	@Override
	public int compareTo(DistanceUtterance arg0) {
		int dif = (int) (arg0.getScore()*100000 - getScore()*100000);
		if(dif != 0)
			return dif;
		else
			return (int) (arg0.getHighestScore()*100000 - getHighestScore()*100000);
	}
}
