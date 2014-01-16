package l2f.evaluator.frames;

import java.io.Serializable;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrameAttribute implements Serializable{

	private static final long serialVersionUID = -5670962619737770723L;
	private ArrayList<AttributeValue> values = new ArrayList<AttributeValue>();
	private String name;
	private AttributeValue slot;

	public FrameAttribute(String name, ArrayList<AttributeValue> values){
		this.name = name;
		this.values.addAll(values);
	}

	public FrameAttribute(String name, AttributeValue slot){
		this.name = name;
		this.slot = slot;
	}

	public AttributeValue getSlot(){
		return slot;
	}
	public void addValues(ArrayList<AttributeValue> values){
		boolean newVal = true;
		AttributeValue val;
		AttributeValue av;
		int j = 0;
		int i = 0;
		while(j < values.size()){
			val = values.get(j);
			while(i < getAttrValues().size()){
				av = getAttrValues().get(i);
				if(av.isSameVal(val)){
					av.addValues(val);
					newVal = false;
					break;
				}
				i++;
			}
			if(newVal){
				getAttrValues().add(val);
			}
			newVal = true;
			j++;
			i = 0;
		}
	}

	public ArrayList<AttributeValue> getAttrValues(){
		return values;
	}

	public String getName(){
		return name;
	}

	public String toString(){
		return "Attribute name: " + getName() + "\nValues: " + getAttrValues().toString();
	}
	
	public String getAttrValue(FrameQuestion fq) {
		for(AttributeValue av : getAttrValues()){
			for(String value : av.getValues()){
				if(fq.getQuestion().contains(value)){
					return "CAT" + av.toString();
				}
			}

		}
		return "CAT_UNKNOWN_" + getName();
	}
	
	public String getSlotValues(String question) {
		for(AttributeValue av : getAttrValues()){
			for(String value : av.getValues()){
				if(question.contains(value)){
					return getName() + ": " + av.toString();
				}
			}

		}
//		System.out.println("NULL_SLOT\n" + question + "\n" + getAttrValues().toString());
//		System.exit(1);
		
		return getName() + ": NULL_SLOT";
	}

}
