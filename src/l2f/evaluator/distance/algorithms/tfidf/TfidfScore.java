package l2f.evaluator.distance.algorithms.tfidf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2f.evaluator.distance.algorithms.DistanceUtterance;

public class TfidfScore {
	private HashMap<String, Integer> wordOnDocumentFrequency = new HashMap<String, Integer>();
	private Integer numberOfDocuments = 0;
	//	private SoftHashMap<Integer, Map<String, Double>> cache = new SoftHashMap<Integer, Map<String,Double>>();
	//	private HashMap<Integer, Map<String, Double>> cache = new HashMap<Integer, Map<String,Double>>();
	private HashMap<String, Map<String, Double>> cache = new HashMap<String, Map<String,Double>>();

	/**
	 * Creates a new instance of the algorithm with an empty training set
	 */
	public TfidfScore(ArrayList<DistanceUtterance> documents) {
		initDU(documents);
	}

	public TfidfScore(List<List<String>> tokenizedDocs) {
		initTokenDocs(tokenizedDocs);
	}

	private void initTokenDocs(List<List<String>> tokenizedDocs) {
		for(List<String> doc : tokenizedDocs)
			addSentenceToTrainSet(doc);
	}

	private void initDU(ArrayList<DistanceUtterance> documents) {
		for(DistanceUtterance du : documents)
			addSentenceToTrainSet(du.getNGramUtterance());
	}

	public void addSentenceToTrainSet(List<String> tokenDoc){
		for(String word : tokenDoc) {
			Integer currentCount = wordOnDocumentFrequency.get(word);

			if(currentCount == null) {
				wordOnDocumentFrequency.put(word, Integer.valueOf(1));
			} else {
				wordOnDocumentFrequency.put(word, currentCount+1);
			}
		}

		numberOfDocuments++;
	}

	public Map<String, Double> scoreSentence(Collection<String> wordsList) {
		//		System.out.println(wordsList + " " + wordsList.hashCode()); 
		//		Map<String, Double> results = cache.get(wordsList.hashCode());
		Map<String, Double> results = cache.get(wordsList.toString());
		if(results != null) {
			return results;
		}

		int numberOfWords = wordsList.size();

		Map<String, Integer> wordFrequency = getTFValues(wordsList);

		results = new HashMap<String, Double>();
		for(String word : wordFrequency.keySet()) {
			Integer tfValue = wordFrequency.get(word);
			results.put(word, getTFIDFScore(word, tfValue, numberOfWords));
		}

		//		cache.put(wordsList.hashCode(), results);
		cache.put(wordsList.toString(), results);
		//System.out.println("TfIdfAlgo: Score given to sentence components "+results);
		return results;
	}

	public Map<String, Integer> getTFValues(Collection<String> wordsList){
		Map<String, Integer> wordFrequency = new HashMap<String, Integer>();

		for(String word : wordsList) {
			Integer currentCount = wordFrequency.get(word);

			if(currentCount == null) {
				wordFrequency.put(word, Integer.valueOf(1));
			} else {
				wordFrequency.put(word, currentCount+1);
			}
		}
		
		return wordFrequency;
	}
	
	public double getTFIDFScore(String word, int tfValue, int numberOfWords){
		Integer wordOnDocumentValue = wordOnDocumentFrequency.get(word);

		if(wordOnDocumentValue == null) {
			// we don't have this word on our training set, giving score 0
			return 0.0;
		}
		else {
			return ((double)tfValue / (double)numberOfWords)*(Math.log((double)numberOfDocuments / (double) wordOnDocumentValue));
		}
	}

	public double mapScoreTotals(Map<String, Double> scoreMap) {
		double result = 0.00000000;
		for(Double value : scoreMap.values()) {
			result += value;
		}

		return result;
	}
}
