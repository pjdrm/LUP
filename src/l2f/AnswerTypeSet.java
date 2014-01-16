package l2f;

public enum AnswerTypeSet {
	OUTDOMAIN("out-domain");
	
	private String type;
	AnswerTypeSet(String type){
		this.type = type;
	}
	
	public String type(){return type;}
}
