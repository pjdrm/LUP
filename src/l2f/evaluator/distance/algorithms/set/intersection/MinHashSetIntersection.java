package l2f.evaluator.distance.algorithms.set.intersection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.mahout.clustering.minhash.HashFactory.HashType;
import org.apache.mahout.clustering.minhash.HashFunction;

public class MinHashSetIntersection implements SetIntersection{

	private int numHashFunctions;
	private HashType hashType = HashType.POLYNOMIAL;
	private Map<String, Integer[]> minHashCache = new HashMap<String, Integer[]>();
	
	public MinHashSetIntersection(int numHashFunctions, HashType hashType){
		this.numHashFunctions = numHashFunctions;
		this.hashType = hashType;
	}
	
	@Override
	public double intersection(List<String> wordSetA, List<String> wordSetB) {
		Integer[] minHashWordSetA = minHashCache.get(wordSetA.toString());
		if(minHashWordSetA == null){
			minHashWordSetA = minHashFromDcument(wordSetA);
			minHashCache.put(wordSetA.toString(), minHashWordSetA);
		}
		
		Integer[] minHashWordSetB = minHashCache.get(wordSetB.toString());
		if(minHashWordSetB == null){
			minHashWordSetB = minHashFromDcument(wordSetB);
			minHashCache.put(wordSetB.toString(), minHashWordSetB);
		}

		//muito restrito, tem de ser o mesmo hash na mesma posicao. Experimentar uma versao com contains e remove
		double numberOfEqualHashes = 0.0;
		for(int i = 0; i < minHashWordSetA.length; i++){
			if(minHashWordSetA[i].equals(minHashWordSetB[i])){
				numberOfEqualHashes++;
			}
		}
		return numberOfEqualHashes/new Double(numHashFunctions);
	}
	
	@Override
	public String toString(){
		return "MinHash NumHashFunctions " + numHashFunctions + " HashType " + hashType.toString();
	}
	
	public Integer[] minHashFromDcument(List<String> documentTokens){

		int[] minHashValues = new int[numHashFunctions];
		HashFunction[] hashFunction = org.apache.mahout.clustering.minhash.HashFactory.createHashFunctions(hashType, numHashFunctions);

		// Initialize the minhash values to highest
		for (int i = 0; i < numHashFunctions; i++) {
			minHashValues[i] = Integer.MAX_VALUE;
		}

		for (int i = 0; i < numHashFunctions; i++) {
			for(String token : documentTokens){
				byte[] bytesToHash = token.getBytes();
				int hashIndex = hashFunction[i].hash(bytesToHash);

				if (minHashValues[i] > hashIndex) {
					minHashValues[i] = hashIndex;
				}
			}
		}

		return ArrayUtils.toObject(minHashValues);
	}

}
