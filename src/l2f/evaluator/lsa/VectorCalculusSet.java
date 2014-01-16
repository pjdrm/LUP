package l2f.evaluator.lsa;


public enum VectorCalculusSet {

    DP("dotProduct"),
    CS("cosineSimilarity");

    private final String shortName;
    
    private VectorCalculusSet(String shortName) {
    	this.shortName = shortName;
    }

	public String getShortName() {
		return shortName;
	}
}
