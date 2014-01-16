package l2f.nlp;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

public class SimpleSentenceSpliter{

	public List<String> split(String str) {
		List<String> tokens = new ArrayList<String>();
		BreakIterator bi = BreakIterator.getSentenceInstance();
		bi.setText(str);
		int begin = bi.first();
		int end;
		for (end = bi.next(); end != BreakIterator.DONE; end = bi.next()) {
			String t = str.substring(begin, end);
			if (t.trim().length() > 0) {
				tokens.add(str.substring(begin, end));
			}
			begin = end;
		}
		if (end != -1) {
			tokens.add(str.substring(end));
		}

		return tokens;
	}
}

