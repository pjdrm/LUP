package l2f.corpus.processor;

public abstract class StringModifier {
	protected String modifications = "";

	public abstract String modify(String str);
	
	public abstract String getDescription() ;
	
	public String getModifications(){
		return modifications;
	}

}
