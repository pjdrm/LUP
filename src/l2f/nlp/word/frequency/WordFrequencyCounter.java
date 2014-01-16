package l2f.nlp.word.frequency;

import java.util.List;
import java.util.Set;

public interface WordFrequencyCounter {
	double[] getFrequencyValues(List<String> tokenizedUtterance, Set<String> termSet);
}
