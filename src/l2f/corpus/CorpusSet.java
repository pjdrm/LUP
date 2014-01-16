package l2f.corpus;

public enum CorpusSet {
	USERTEST("@ut_1");
	
	private String type;
	CorpusSet(String type){
		this.type = type;
	}
	
	public String type(){return type;}
}
