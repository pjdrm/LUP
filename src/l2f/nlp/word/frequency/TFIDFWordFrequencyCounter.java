package l2f.nlp.word.frequency;

import java.util.List;
import java.util.Map;
import java.util.Set;

import l2f.evaluator.distance.algorithms.tfidf.TfidfScore;

public class TFIDFWordFrequencyCounter implements WordFrequencyCounter{

	private TfidfScore tfidf;
	
	public TFIDFWordFrequencyCounter(List<List<String>> tokenizedDocs){
		this.tfidf = new TfidfScore(tokenizedDocs);
	}
	
	@Override
	public double[] getFrequencyValues(List<String> tokenizedUtterance,	Set<String> termSet) {
		double[] termFreqMatrix = new double[termSet.size()];
		double termCount = 0;
		int termIndex = 0;
		int numberOfWords = tokenizedUtterance.size();
		Map<String, Integer> wordFrequency = tfidf.getTFValues(tokenizedUtterance);
		
		for(String term : termSet){
			for(String uttToken : tokenizedUtterance){
				if(uttToken.equals(term)){
					termCount = tfidf.getTFIDFScore(uttToken, wordFrequency.get(uttToken), numberOfWords);
				}
			}
			termFreqMatrix[termIndex] = termCount;
			termIndex++;
			termCount = 0.0;
		}
		return termFreqMatrix;
	}

}
