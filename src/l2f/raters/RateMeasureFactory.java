package l2f.raters;

import l2f.config.ConfigRaters;
import l2f.raters.measures.CronbachMeasure;
import l2f.raters.sets.RatingMeasuresSet;

public class RateMeasureFactory {
	public static RateMeasure getRateMeasure(String mode, String testFile, String testFilePath){
		ConfigRaters.parseConfig();
		
		if(ConfigRaters.measure.equals(RatingMeasuresSet.CB.type()))
			return new CronbachMeasure(mode, testFile, testFilePath);
		
		return null;
	}

}
