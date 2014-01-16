package l2f.evaluator.distance.algorithms.jaccard;

import java.util.ArrayList;
import java.util.List;

import l2f.config.ConfigDistance;
import l2f.evaluator.distance.algorithms.DistanceAlgorithm;
import l2f.evaluator.distance.algorithms.DistanceUtterance;
import l2f.evaluator.distance.algorithms.overlap.OverlapTfidfAlgorithm;

public class JaccardOverlapTfidf implements DistanceAlgorithm{

	private ArrayList<DistanceUtterance> documents;
	JaccardTfidfAlgorithm jaccardTfidf;
	OverlapTfidfAlgorithm overlapTfidf;
	
	public JaccardOverlapTfidf(ArrayList<DistanceUtterance> documents){
		this.documents = documents;
		this.jaccardTfidf = new JaccardTfidfAlgorithm(documents);
		this.overlapTfidf = new OverlapTfidfAlgorithm(documents);
	}
	
	@Override
	public double distance(List<String> wordSetA, List<String> wordSetB) {
		double jaccardScore = jaccardTfidf.distance(wordSetA, wordSetB);
		double overlapScore = overlapTfidf.distance(wordSetA, wordSetB);
		return ConfigDistance.jaccardOverlapWeight * jaccardScore + (1.00 - ConfigDistance.jaccardOverlapWeight) * overlapScore;
	}

}
