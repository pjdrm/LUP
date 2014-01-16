package l2f.corpus;

import java.io.Serializable;

public class Utterance implements Serializable{
	
	private static final long serialVersionUID = -8141755065295754578L;
	
	private String cat;
	private String utterance;
	
	public Utterance(String cat, String utterance){
		this.cat = cat;
		this.utterance = utterance;
	}
	
	public Utterance(String cat){
		this.cat = cat;
		this.utterance = "dummy";
	}

	public String getCat() {
		return cat;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	public String getUtterance() {
		return utterance;
	}

	public void setUtterance(String utterance) {
		this.utterance = utterance;
	}
	
	public String toString(){
		return getCat() + " " + getUtterance();
	}
	
	
}
