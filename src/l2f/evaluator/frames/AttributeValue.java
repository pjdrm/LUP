package l2f.evaluator.frames;

import java.io.Serializable;
import java.util.ArrayList;

public class AttributeValue implements Serializable{
	private static final long serialVersionUID = 3925300661938374959L;
	private ArrayList<String> values = new ArrayList<String>();

	public AttributeValue(ArrayList<String> values) {
		this.values.addAll(values);
	}
	
	public AttributeValue(AttributeValue av) {
		this.values.addAll(av.getValues());
	}

	public ArrayList<String> getValues(){
		return values;
	}

	public void addVal(String val){
		getValues().add(val);
	}

	public void addValues(AttributeValue av){
		int j = 0;
		String val;
		while(j < av.getValues().size()){
			val = av.getValues().get(j);
			if(!getValues().contains(val))
				getValues().add(val);
			j++;
		}
	}

	public boolean isSameVal(AttributeValue av) {
		for(String val : av.getValues()){
			for(String val1 : getValues()){
				if(val1.equals(val))
					return true;
			}
		}
		return false;
	}

	public String toString(){
		String str = "";
		for(String val : getValues()){
			str += "_" + val.replaceAll(" ", "#");
		}
		return str;
	}
}
