package l2f.evaluator.frames;

import java.io.Serializable;
import java.util.ArrayList;

import l2f.evaluator.QuestionClassifierEvaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Frame implements Serializable{

	private static final long serialVersionUID = -7856661940753269115L;
	private String id;
	private ArrayList<String> framesQuestions = new ArrayList<String>();
	private ArrayList<FrameAttribute> framesAttrs = new ArrayList<FrameAttribute>();
	protected static final Logger logger = LoggerFactory.getLogger(QuestionClassifierEvaluator.class);

	public Frame(String id){
		setId(id);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public ArrayList<String> getFramesQuestions() {
		return framesQuestions;
	}

	public void addQuestion(String question){
		getFramesQuestions().add(question);
	}

	public void addAttribute(FrameAttribute attr){
		getFramesAttributes().add(attr);
	}

	public ArrayList<FrameAttribute> getFramesAttributes() {
		return framesAttrs;
	}

	public String toString(){
		String str = "Frame id: " + getId() + "\nQuestions:\n";
		for(String question: getFramesQuestions()){
			str += question + "\n";
		}
		str += "Frame attributes:\n";

		for(FrameAttribute attr: getFramesAttributes()){
			str += attr.toString() + "\n";
		}
		return str;
	}

	public boolean hasValueInAttr(AttributeValue attributeValue) {
		for(FrameAttribute frameAttr : getFramesAttributes()){
			for(AttributeValue av : frameAttr.getAttrValues()){
				if(av.isSameVal(attributeValue))
					return true;
			}
		}
//		logger.info("Discarding: " + attributeValue.toString() + " FrameID: " + getId() + " Attr: " + getFramesAttributes());
		return false;
	}

}