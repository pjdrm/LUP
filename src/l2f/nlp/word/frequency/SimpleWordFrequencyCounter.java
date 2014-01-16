package l2f.nlp.word.frequency;

import java.util.List;
import java.util.Set;

public class SimpleWordFrequencyCounter implements WordFrequencyCounter{

	@Override
	public double[] getFrequencyValues(List<String> tokenizedUtterance,	Set<String> termSet) {
		double[] termFreqMatrix = new double[termSet.size()];
		double termCount = 0;
		int termIndex = 0;
		for(String term : termSet){
			for(String uttToken : tokenizedUtterance){
				if(uttToken.equals(term))
					termCount++;
			}
			termFreqMatrix[termIndex] = termCount;
			termIndex++;
			termCount = 0.0;
		}
		return termFreqMatrix;
	}

}
