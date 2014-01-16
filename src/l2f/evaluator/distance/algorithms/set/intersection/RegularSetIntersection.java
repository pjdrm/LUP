package l2f.evaluator.distance.algorithms.set.intersection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegularSetIntersection implements SetIntersection{

	@Override
	public double intersection(List<String> wordSetA, List<String> wordSetB) {
		double aCount = wordSetA.size();
		double bCount = wordSetB.size();
		HashSet<String> hashSetA = new HashSet<String>(wordSetA);
		HashSet<String> hashSetB = new HashSet<String>(wordSetB);
		Set<String> firstSet = hashSetA;
		Set<String> secondSet = hashSetB;

		// testing similar words through the smallest set
		if(aCount > bCount) {
			firstSet = hashSetB;
			secondSet = hashSetA;
		}
		double cCount = 0;
		for(String word : firstSet) {
			if(secondSet.contains(word)) {
				cCount++;
			}
		}
		return cCount;
	}
	
	@Override
	public String toString(){
		return "Regular";
	}

}
