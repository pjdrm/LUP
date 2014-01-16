package l2f.out.of.domain;

public enum OutOfDomainSet {
	
	CLM("clm"),
	WORSTBEST("WorstBest"),
	AVERAGE("Average"),
	AVERAGEGENERAL("AverageGeneral"),
	WORSTWORST("WorstWorst"),
	FIXEDTHRESHOLD("FixedThreshold");
	
	private String type;
	OutOfDomainSet(String type){
		this.type = type;
	}
	
	public String type(){return type;}
}