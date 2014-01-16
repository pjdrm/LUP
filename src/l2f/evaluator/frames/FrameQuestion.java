package l2f.evaluator.frames;

import java.io.Serializable;
import java.util.ArrayList;


public class FrameQuestion implements Serializable{
	
	private String question;
	private String questionCategory;
	private String frameCat;
	private ArrayList<String> slotValues;
	
	public FrameQuestion(String question, String frameCat, ArrayList<String> slotValues){
		this.question = question;
		this.frameCat = frameCat;
		this.questionCategory = frameCat;
		this.slotValues = slotValues;
	}
	
	public void setQuestion(String question){
		this.question = question;
	}
	
	public String getQuestion(){
		return question;
	}
	
	public String getQuestionCat(){
		return questionCategory;
	}
	
	public void setQuestionCat(String cat){
		questionCategory = cat;
	}
	
	public String getFrameCat(){
		return frameCat;
	}
	
	public ArrayList<String> getSlotValues(){
		return slotValues;
	}
	
	public String toString(){
		return getFrameCat() + " " + getQuestion();
	}
	
	private static final long serialVersionUID = -1455641502711700521L;
}
