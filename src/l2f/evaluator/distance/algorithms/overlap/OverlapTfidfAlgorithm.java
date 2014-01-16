package l2f.evaluator.distance.algorithms.overlap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import l2f.evaluator.distance.algorithms.DistanceAlgorithm;
import l2f.evaluator.distance.algorithms.DistanceUtterance;
import l2f.evaluator.distance.algorithms.tfidf.TfidfScore;

public class OverlapTfidfAlgorithm implements DistanceAlgorithm{

	private TfidfScore tfidfScore;
	
	public OverlapTfidfAlgorithm(ArrayList<DistanceUtterance> documents){
		tfidfScore = new TfidfScore(documents);
	}
	
	@Override
	public double distance(List<String> wordSetA, List<String> wordSetB) {
		double aCount = wordSetA.size();
		double bCount = wordSetB.size();
		List<String> firstSet = wordSetA;
		List<String> secondSet = wordSetB;
		
		// testing similar words through the smallest set
		if(aCount > bCount) {
			firstSet = wordSetB;
			secondSet = wordSetA;
		}
		Map<String, Double> firstSetScore = tfidfScore.scoreSentence(firstSet); 
		Map<String, Double> secondSetScore = tfidfScore.scoreSentence(secondSet);
		
		double maxScoreFromScoreAlgo = tfidfScore.mapScoreTotals(firstSetScore) + tfidfScore.mapScoreTotals(secondSetScore);
		double wordsMatchedScore = 0.0000000;
		double cCount = 0;
		for(String word : firstSet) {
			if(secondSet.contains(word)) {
				/*if(firstSetScore.get(word) == null){
					System.out.println("wordSetA " + wordSetA + "\nfirstSetScore" + firstSetScore + "\nsecondSetScore" + secondSetScore + "\nwordSetB " + wordSetB + "\nWORD " + word);
					firstSetScore = tfidfScore.scoreSentence(firstSet);
					secondSetScore = tfidfScore.scoreSentence(secondSet);
				}*/
				wordsMatchedScore += firstSetScore.get(word) + secondSetScore.get(word);
				cCount++;
			}
		}
		
		double denominator = Math.min(aCount, bCount) * maxScoreFromScoreAlgo;
		if(denominator == 0)
			denominator = 1.0;
		return (cCount * wordsMatchedScore) / denominator;
	}

}
