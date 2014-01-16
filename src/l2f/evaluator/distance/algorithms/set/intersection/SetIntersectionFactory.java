package l2f.evaluator.distance.algorithms.set.intersection;

import org.apache.mahout.clustering.minhash.HashFactory.HashType;

public class SetIntersectionFactory {
	public static SetIntersection getSetIntersection(String args){
		args = args.replaceAll("  +", "");
		String[] argsArray = args.split(" ");
		if(argsArray[0].equals("regular"))
			return new RegularSetIntersection();
		else if(argsArray[0].equals("MinHash")){
			HashType hashType = null;
			if(argsArray[1].equals("LINEAR"))
				hashType = HashType.LINEAR;
			else if(argsArray[1].equals("MURMUR"))
				hashType = HashType.MURMUR;
			else if(argsArray[1].equals("MURMUR3"))
				hashType = HashType.MURMUR3;
			else if(argsArray[1].equals("POLYNOMIAL"))
				hashType = HashType.POLYNOMIAL;
			return new MinHashSetIntersection(Integer.valueOf(argsArray[2]), hashType);
		}
		return null;
	}
}
