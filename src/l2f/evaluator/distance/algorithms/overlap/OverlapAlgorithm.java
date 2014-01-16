package l2f.evaluator.distance.algorithms.overlap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import l2f.evaluator.distance.algorithms.DistanceAlgorithm;
import l2f.evaluator.distance.algorithms.set.intersection.SetIntersection;

/**
 * 
 * @author Sergio Curto
 *
 */
public class OverlapAlgorithm implements DistanceAlgorithm{
	private SetIntersection setInterstion;

	public OverlapAlgorithm(SetIntersection setInterstion){
		this.setInterstion = setInterstion;
	}

	/**
	 * Implements Overlap coefficient: Overlap(A,B) = |A and B| / min(|A|, |B|) = |C| / min(|A|,|B|)
	 * 
	 * C - number of words shared between sentence A and B
	 * 
	 * @param wordSetA Sentence A words ngrams
	 * @param wordSetB Sentence B words ngrams
	 * @return value between 0.00 (completely different) and 1.00 (completely similar)
	 */
	public double distance(List<String> wordSetA, List<String> wordSetB) {
		double aCount = wordSetA.size();
		double bCount = wordSetB.size();
		double cCount = setInterstion.intersection(wordSetA, wordSetB);
		return (cCount) / Math.min(aCount, bCount);
	}
}
