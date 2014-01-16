package l2f.raters.sets;

public enum RatingMeasuresSet {
	CB("Cronbach");
	
	private String type;
	RatingMeasuresSet(String type){
		this.type = type;
	}
	
	public String type(){return type;}

}
