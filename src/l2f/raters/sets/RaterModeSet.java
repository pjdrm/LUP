package l2f.raters.sets;

public enum RaterModeSet {
	GEN("generate test"),
	MEASURE("consistency measure");
	
	private String type;
	
	RaterModeSet(String type){
		this.type = type;
	}
	
	public String type(){return type;}
}
