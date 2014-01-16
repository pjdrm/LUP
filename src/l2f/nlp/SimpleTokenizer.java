package l2f.nlp;

import java.util.ArrayList;
import java.util.List;

public class SimpleTokenizer implements Tokenizer{

	@Override
	public List<String> tokenize(String line) {
		List<String> wordSet = new ArrayList<String>();
		for(String word : line.split("\\s+")){
			wordSet.add(word);
		}
		return wordSet;
	}

}
