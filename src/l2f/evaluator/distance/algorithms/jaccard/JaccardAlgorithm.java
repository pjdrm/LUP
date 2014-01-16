package l2f.evaluator.distance.algorithms.jaccard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import l2f.evaluator.distance.algorithms.DistanceAlgorithm;
import l2f.evaluator.distance.algorithms.set.intersection.SetIntersection;

/**
 * Implements Jaccard similarity measure: Jaccard(A,B) = |A and B| / |A or B| = |C| / (|A|+|B|-|C|)
 * 
 * C - number of words shared between sentence A and B
 * 
 * @author Sergio Curto
 *
 */
public class JaccardAlgorithm implements DistanceAlgorithm{

	private SetIntersection setInterstion;

	public JaccardAlgorithm(SetIntersection setInterstion){
		this.setInterstion = setInterstion;
	}

	/**
	 * Implements Jaccard similarity measure: Jaccard(A,B) = |A and B| / |A or B| = |C| / (|A|+|B|-|C|)
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
		//		List<String> firstSet = wordSetA;
		//		List<String> secondSet = wordSetB;

		// testing similar words through the smallest set
		/*if(aCount > bCount) {
			firstSet = wordSetB;
			secondSet = wordSetA;
		}*/
		double cCount = setInterstion.intersection(wordSetA, wordSetB);
		return cCount / (aCount + bCount - cCount);
	}

	/**
	 * Implements Jaccard similarity measure: Jaccard(A,B) = |A and B| / |A or B| = |C| / (|A|+|B|-|C|)
	 * Applies a scoring factor to the words that matched, giving same or less score then the base algo.
	 * C - number of words shared between sentence A and B
	 * 
	 * @param wordSetA Sentence A words
	 * @param wordSetB Sentence B words
	 * @param scoreAlgo Scoring algorithm to be applied
	 * @return value between 0.00 (completely different) and 1.00 (completely similar)
	 */
	/*
	static public double jaccardAlgo(Set<String> wordSetA, Set<String> wordSetB, SentenceScoreAlgorithm scoreAlgo) {
		double aCount = wordSetA.size();
		double bCount = wordSetB.size();
		Set<String> firstSet = wordSetA;
		Set<String> secondSet = wordSetB;

		// testing similar words through the smallest set
		if(aCount > bCount) {
			firstSet = wordSetB;
			secondSet = wordSetA;
		}

		Map<String, Double> firstSetScore = scoreAlgo.scoreSentence(firstSet);
		Map<String, Double> secondSetScore = scoreAlgo.scoreSentence(firstSet);

		double maxScoreFromScoreAlgo = SentenceScoreAlgorithm.mapScoreTotals(firstSetScore) + SentenceScoreAlgorithm.mapScoreTotals(secondSetScore);
		double wordsMatchedScore = 0.0000000;
		double cCount = 0;
		for(String word : firstSet) {
			if(secondSet.contains(word)) {
				wordsMatchedScore += firstSetScore.get(word) + secondSetScore.get(word);
				cCount++;
			}
		}

		return (cCount * wordsMatchedScore) / ((aCount + bCount - cCount) * maxScoreFromScoreAlgo);
	}
	 */
}
