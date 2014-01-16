package l2f.evaluator.frames;

import java.util.ArrayList;

public class Answer {
	private String frameCat;
	private ArrayList<FrameAttribute> frameAttrs = new ArrayList<FrameAttribute>();
	
	public Answer(String frameCat, ArrayList<FrameAttribute> frameAttrs){
		this.frameCat = frameCat;
		this.frameAttrs = frameAttrs;
	}
	
	public String getFramecat(){
		return frameCat;
	}
	
	public ArrayList<FrameAttribute> getSlots(){
		return frameAttrs;
	}
}
