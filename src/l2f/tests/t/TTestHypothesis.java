package l2f.tests.t;

public class TTestHypothesis {
	private String sampleDesc;
	private double[] sample;
	
	public TTestHypothesis(String sample1Desc, double[] sample){
		this.sampleDesc = sample1Desc;
		this.sample = sample;
	}
	
	public String getSampleDesc(){
		return sampleDesc;
	}
	
	public double[] getSample(){
		return sample;
	}
	
	
	
	public String toString(){
		String str = "";
		for(int i = 0; i < 5 ; i++)
			str += sample[i] + " ";
		return sampleDesc + " " + "\n" + str;
	}
}
