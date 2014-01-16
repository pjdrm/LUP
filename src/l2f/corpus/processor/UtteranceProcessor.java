package l2f.corpus.processor;

import java.util.ArrayList;
import java.util.List;

import l2f.corpus.Utterance;

public class UtteranceProcessor {
	private List<StringModifier> modifiers = new ArrayList<StringModifier>();
	private String modifications = "";
	
	public UtteranceProcessor(){
		
	}
	
	public UtteranceProcessor(List<StringModifier> modifiers){
		this.modifiers = modifiers;
	}
	
	public void processUtterance(Utterance ut){
		for(StringModifier modifier : modifiers){
			ut.setUtterance(modifier.modify(ut.getUtterance()));
		}
	}
	
	public String processString(String str){
		for(StringModifier modifier : modifiers){
			str = modifier.modify(str);
			modifications += modifier.getModifications();
		}
		return str;
	}
	
	public void addModifier(StringModifier modifier){
		modifiers.add(modifier);
	}
	
	public String getDescription(){
		if(modifiers.size() == 0)
			return "";
		
		String desc = " Modifiers- ";
		for(StringModifier sm : modifiers){
			desc += sm.getDescription() + " ";
		}
		return " " + desc.trim();
	}
	
	public String getModifications(){
		String currentModifications = new String(modifications);
		modifications = "";
		return currentModifications;
	}
}
