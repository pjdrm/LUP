package l2f.evaluator;

import java.util.ArrayList;

import l2f.corpus.Utterance;

public class QCEAnswer {
	private ArrayList<Utterance> possibleAnswers;
	private String classifierDescription;
	private Double score;
	private String recognisedNE; 
	
	public QCEAnswer(ArrayList<Utterance> possibleAnswers, String recognisedNE, String classifierDescription, Double score){
		this.possibleAnswers = possibleAnswers;
		this.recognisedNE = recognisedNE;
		this.classifierDescription = classifierDescription;
		this.score = score;
	}
	
	public ArrayList<Utterance> getPossibleAnswers(){
		return possibleAnswers;
	}
	
	public ArrayList<String> getStringPossibleAnswers(){
		ArrayList<String> answers = new ArrayList<String>();
		for(Utterance u : getPossibleAnswers()){
			answers.add(u.getUtterance());
		}
		return answers;
	}
	
	public String getRecognisedNE(){
		return recognisedNE;
	}
	
	public String getClassifierDescription(){
		return classifierDescription;
	}
	
	public Double getScore(){
		return score;
	}
}
