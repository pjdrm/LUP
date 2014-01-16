package l2f.evaluator.distance.algorithms.jaccard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import l2f.evaluator.distance.algorithms.DistanceAlgorithm;
import l2f.evaluator.distance.algorithms.DistanceUtterance;
import l2f.evaluator.distance.algorithms.tfidf.TfidfScore;

public class JaccardTfidfAlgorithm implements DistanceAlgorithm{

	private TfidfScore tfidfScore;
	
	public JaccardTfidfAlgorithm(ArrayList<DistanceUtterance> documents){
		this.tfidfScore = new TfidfScore(documents);
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
				wordsMatchedScore += firstSetScore.get(word) + secondSetScore.get(word);
				cCount++;
			}
		}
		
		double denominator = (aCount + bCount - cCount) * maxScoreFromScoreAlgo;
		if(denominator == 0.0)
			denominator = 1.0;
		
		return (cCount * wordsMatchedScore) / denominator;
	}

}
