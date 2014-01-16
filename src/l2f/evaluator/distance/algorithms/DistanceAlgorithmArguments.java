package l2f.evaluator.distance.algorithms;

import l2f.interpretation.classification.features.FeatureSet;

public class DistanceAlgorithmArguments {

	private FeatureSet featureSet;

	public DistanceAlgorithmArguments(FeatureSet fs){
		this.featureSet = fs;
	}
	
	public FeatureSet getFeatureSet() {
		return featureSet;
	}
	
}
