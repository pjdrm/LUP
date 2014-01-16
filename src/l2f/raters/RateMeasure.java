package l2f.raters;

import l2f.raters.sets.RaterModeSet;

public abstract class RateMeasure {
	protected String mode;
	protected String testFile;
	protected String testFilePath;
	
	public RateMeasure(String mode, String testFile, String testFilePath){
		this.mode = mode;
		this.testFile = testFile;
		this.testFilePath = testFilePath;
	}
	
	public void execute(){
		if(mode.equals(RaterModeSet.GEN.type()))
			generateTest();
		else if(mode.equals(RaterModeSet.MEASURE.type()))
			rate();
	}
	
	protected abstract void rate();

	protected void generateTest(){
		
	}
	

}
