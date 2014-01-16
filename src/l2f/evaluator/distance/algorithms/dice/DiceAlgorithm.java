/**
 * 
 */
package l2f.evaluator.distance.algorithms.dice;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import l2f.evaluator.distance.algorithms.DistanceAlgorithm;
import l2f.evaluator.distance.algorithms.set.intersection.SetIntersection;

/**
 * @author Sergio Curto
 *
 */
public class DiceAlgorithm implements DistanceAlgorithm{

	private SetIntersection setInterstion;
	
	public DiceAlgorithm(SetIntersection setInterstion){
		this.setInterstion = setInterstion;
	}
	
	/**
	 * Implements Dice similarity measure: Dice(A,B) = 2 * |A and B| / (|A| + |B|) = 2 * |C| / (|A|+|B|)
	 * 
	 * C - number of words shared between sentence A and B
	 * 
	 * @param wordSetA Sentence A words
	 * @param wordSetB Sentence B words
	 * @return value between 0.00 (completely different) and 1.00 (completely similar)
	 */
	public double distance(List<String> wordSetA, List<String> wordSetB) {
		double aCount = wordSetA.size();
		double bCount = wordSetB.size();
		
		return (2*setInterstion.intersection(wordSetA, wordSetB)) / (aCount + bCount);
	}
}
