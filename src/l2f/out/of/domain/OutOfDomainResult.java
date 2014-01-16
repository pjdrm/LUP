package l2f.out.of.domain;

public class OutOfDomainResult {
	private boolean isOutOfDomain;
	private double score = 1.0;
	private double threshold = 0.0;
	private String debug = "";
	
	public OutOfDomainResult(boolean isOutOfDomain){
		this.isOutOfDomain = isOutOfDomain;
	}
	
	public OutOfDomainResult(boolean isOutOfDomain, double score, double threshold){
		this(isOutOfDomain);
		this.score = score;
		this.threshold = threshold;
	}
	
	public OutOfDomainResult(boolean isOutOfDomain, double score, double threshold, String debug){
		this(isOutOfDomain, score, threshold);
		this.debug = debug;
	}
	
	public String getDebug(){
		return debug;
	}
	
	public boolean getIsOutOfDomain(){
		return isOutOfDomain;
	}
	
	public double getScore(){
		return score;
	}
	
	public double getThreshold(){
		return threshold;
	}

		
}
