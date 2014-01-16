package l2f.nlp;

import java.text.Normalizer;
import java.text.Normalizer.Form;

public class NormalizerSimple {

	/**
	 * contains:
	 * 	-remove all '!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~'  = \p{Punct}+
	 *  -lowercase
	 *  -trim
	 *  
	 * @param words: single word or sentence
	 * @return single word or sentence normalized
	 */
	public static String normPunctLCase(String words){
		return words.replaceAll("\\p{Punct}+", "").toLowerCase().trim();
	}
	
	/**
	 * contains:
	 * 	-remove all !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~  = \p{Punct}+
	 *  -lowercase
	 *  -trim
	 *  -remove all diacritical marks (´`~^, etc)
	 * 
	 * @param words
	 * @return
	 */
	public static String normPunctLCaseDMarks(String words){
		return Normalizer.normalize(NormalizerSimple.normPunctLCase(words), Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
	
	/**
	 * remove all diacritical marks (´`~^, etc)
	 * @param words
	 * @return
	 */
	public static String normDMarks(String words){
		return Normalizer.normalize(words, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
}
