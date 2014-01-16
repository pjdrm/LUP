/**
 * 
 */
package l2f.utils.string.algorithms;

import java.util.Collection;
import java.util.Map;

/**
 * @author Sergio Curto
 *
 */
public abstract class SentenceScoreAlgorithm {
	/**
	 * Scores each word of the sentence with the implementing algorithm
	 * @param wordSetA List of words to be scored present on the sentence
	 * @return Map with words as key and scores of the respective words as values
	 */
	public abstract Map<String, Double> scoreSentence(Collection<String> wordSetA);
	
	public static double mapScoreTotals(Map<String, Double> scoreMap) {
		double result = 0.00000000;
		for(Double value : scoreMap.values()) {
			result += value;
		}
		
		return result;
	}

	public abstract Map<String, Double> scoreNgrams(Collection<String> ngramCollection);
}
