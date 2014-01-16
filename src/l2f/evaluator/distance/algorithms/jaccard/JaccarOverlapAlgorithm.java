package l2f.evaluator.distance.algorithms.jaccard;

import java.util.List;

import l2f.config.ConfigDistance;
import l2f.evaluator.distance.algorithms.DistanceAlgorithm;
import l2f.evaluator.distance.algorithms.overlap.OverlapAlgorithm;
import l2f.evaluator.distance.algorithms.set.intersection.SetIntersection;

public class JaccarOverlapAlgorithm implements DistanceAlgorithm{

	private JaccardAlgorithm jaccarAlg;
	private OverlapAlgorithm overlapAlg;

	public JaccarOverlapAlgorithm(SetIntersection setInterstion){
		this.jaccarAlg = new JaccardAlgorithm(setInterstion);
		this.overlapAlg = new OverlapAlgorithm(setInterstion);
	}

	@Override
	public double distance(List<String> wordSetA, List<String> wordSetB) {
		
		double score = ConfigDistance.jaccardOverlapWeight * jaccarAlg.distance(wordSetA, wordSetB) + (1.00 - ConfigDistance.jaccardOverlapWeight) * overlapAlg.distance(wordSetA, wordSetB);
		return score;
	}

}
