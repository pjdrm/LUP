package l2f.evaluator.frames;

import java.util.ArrayList;

import l2f.evaluator.QuestionClassifierEvaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrameClassificationScore {
	private Double frameScrore;
	private Frame frame;
	private int nAttr;
	private Double recCatsPercent;
	ArrayList<String> recCats;
	protected static final Logger logger = LoggerFactory.getLogger(QuestionClassifierEvaluator.class);

	public FrameClassificationScore(Frame fq, Double score, int nAttr, ArrayList<String> recCats){
		this.frame = fq;
		this.frameScrore = score;
		this.nAttr = nAttr;
		this.recCatsPercent = calcRecPercent(recCats);
	}

	private Double calcRecPercent(ArrayList<String> recCats) {
		this.recCats = recCats;
		Double nCats = 0.0;
		for(String cat : recCats){
			/*if(cat.contains("CAT_UNKNOWN") || !getFrame().hasValueInAttr(QuestionClassifierEvaluatorFrames.getValueFromCat(cat))){
				continue;
			}*/
				

			nCats++;
		}
		return nCats / recCats.size();
	}

	public Frame getFrame(){
		return frame;
	}

	public Double getScore(){
		return frameScrore;
	}

	public int getNAttr(){
		return nAttr;
	}

	public Double getRecognizedCats() {
		return recCatsPercent;
	}
	
	public ArrayList<String> getRecCats(){
		return recCats;
	}
}
