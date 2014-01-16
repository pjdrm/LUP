package l2f.out.of.domain.threshold;

import l2f.corpus.Utterance;

public class QCEScore implements Comparable<QCEScore>{
	private Utterance inputUtterance;
	private Utterance outputUtterance;
	private Double accuracy;
	private boolean isCorrectAnswer;
	
	public QCEScore(Utterance inputUtterance, Utterance outputUtterance, Double accuracy, boolean isCorrectAnswer){
		this.inputUtterance = inputUtterance;
		this.outputUtterance = outputUtterance;
		this.accuracy = accuracy;
		this.isCorrectAnswer = isCorrectAnswer;
	}
	
	public Utterance getInputUtterance(){
		return inputUtterance;
	}
	
	public Utterance getOutputUtterance(){
		return outputUtterance;
	}
	
	public Double getAccuracy(){
		return accuracy;
	}
	
	public boolean isCorrectAnswer(){
		return isCorrectAnswer;
	}

	@Override
	public int compareTo(QCEScore qceScore) {
		return (int) Math.signum(-(getAccuracy()*100 - qceScore.getAccuracy()*100));
	}
	
	@Override
	public String toString(){
		return isCorrectAnswer() + " " + getAccuracy() + " <i>" + getInputUtterance().getCat() + " " + getInputUtterance().getUtterance() + "</i> <o>" + getOutputUtterance().getCat() + "</o>"; // <o>" + getOutputUtterance().getCat() + " " + getOutputUtterance().getUtterance() + "</o> " ; 
	}

}
