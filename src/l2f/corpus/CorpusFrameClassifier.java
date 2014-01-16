package l2f.corpus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import l2f.config.Config;
import l2f.evaluator.frames.Frame;
import l2f.evaluator.frames.FrameAttribute;
import l2f.evaluator.frames.FrameQuestion;

public class CorpusFrameClassifier extends Corpus implements Serializable{
	
	private HashMap<String, FrameAttribute> attributesMap = new HashMap<String, FrameAttribute>();
	private ArrayList<Frame> frames = new ArrayList<Frame>();
	private ArrayList<FrameQuestion> frameQuestions = new ArrayList<FrameQuestion>();
	private ArrayList<FrameQuestion> testFrameQuestions = new ArrayList<FrameQuestion>();
	private ArrayList<FrameQuestion> trainFrameQuestions = new ArrayList<FrameQuestion>();
	
	public CorpusFrameClassifier(){
		
	}
	
	public CorpusFrameClassifier(HashMap<String, FrameAttribute> attributesMap, ArrayList<Frame> frames, ArrayList<FrameQuestion> frameQuestions){
		this.attributesMap = attributesMap;
		this.frames = frames;
		this.frameQuestions = frameQuestions;
		
		int numberOfLines = frameQuestions.size();
		int numberOfTestLines = (int)Math.round(numberOfLines*Config.testPercentage);
		ArrayList<Integer> testLines = getRandomTestLines(numberOfTestLines, numberOfLines);
		int i = 0;

		for(FrameQuestion fq : frameQuestions) {
			fq.setQuestion(fq.getQuestion());
			if(testLines.contains(i))
				testFrameQuestions.add(fq);
			else
				trainFrameQuestions.add(fq);
			i++;
		}
	}
	
	public CorpusFrameClassifier(CorpusFrameClassifier cfc){
		this.attributesMap = cfc.getAttributesMap();
		this.frames = cfc.getFrames();
		this.frameQuestions = cfc.getFrameQuestions();
	}
	
	public HashMap<String, FrameAttribute> getAttributesMap() {
		return attributesMap;
	}
	
	public ArrayList<Frame> getFrames() {
		return frames;
	}
	
	public ArrayList<FrameQuestion> getFrameQuestions() {
		return frameQuestions;
	}
	
	public ArrayList<FrameQuestion> getTrainFrameQuestions() {
		return trainFrameQuestions;
	}
	
	public ArrayList<FrameQuestion> getTestFrameQuestions() {
		return testFrameQuestions;
	}
	
	public void setTestFrameQuestions(ArrayList<FrameQuestion> fq){
		this.testFrameQuestions = fq;
	}
	
	public void setTrainFrameQuestions(ArrayList<FrameQuestion> fq){
		this.trainFrameQuestions = fq;
	}
	
	public ArrayList<Integer> getRandomTestLines(int numberOfTestLines, int totalOfLines) {
		ArrayList<Integer> testLines = new ArrayList<Integer>();
		int lineCounter = 0;
		Integer lineNumber = 0;
		Random randomGenerator = new Random();

		while(lineCounter < numberOfTestLines){
			lineNumber = randomGenerator.nextInt(totalOfLines);
			if(!testLines.contains(lineNumber)){
				testLines.add(lineNumber);
				lineCounter++;
			}
		}
		return testLines;
	}
	
	public String toString(){
		String str = "Frames:\n";
		for(Frame f : getFrames())
			str += f.toString() + "\n";
		
		str += "\nTrain:\n";
		for(FrameQuestion fq : getTrainFrameQuestions())
			str += "Q: " + fq.getQuestion() + " CAT: " + fq.getQuestionCat() + "\n";
		
		str += "\nTest:\n";
		for(FrameQuestion fq : getTestFrameQuestions())
			str += "Q: " + fq.getQuestion() + " CAT: " + fq.getQuestionCat() + "\n";
		
		return str;
		
	}

	public void fullTrain() {
		getTrainFrameQuestions().addAll(getTestFrameQuestions());
	}
	
	public void resetToFrameCorpus(){
		for(FrameQuestion fq : getTrainFrameQuestions()){
			fq.setQuestionCat(fq.getFrameCat());
		}
		
		for(FrameQuestion fq : getTestFrameQuestions()){
			fq.setQuestionCat(fq.getFrameCat());
		}
	}
	private static final long serialVersionUID = -7028543772349936777L;
}
