/**
 * 
 */
package l2f.utils.string;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

//import l2f.Language;

/**
 * @author Sergio Curto
 *
 */
public class StopwordsDetector {
	Set<String> stopwordsSet = new HashSet<String>(); 
	
	public StopwordsDetector(InputStream stopwordsList) throws IOException {
		InputStreamReader converter = new InputStreamReader(stopwordsList);
		BufferedReader is = new BufferedReader(converter);
		
		String line;
		while((line = is.readLine()) != null) {
			line = line.trim();
			if(!line.isEmpty()){
				stopwordsSet.add(line);
			}
		}
	}
	
	/*public boolean isStopword(String word) {
		return stopwordsSet.contains(word);
	}

	private static EnumMap<Language, StopwordsDetector> defaultInstances = new EnumMap<Language, StopwordsDetector>(Language.class);
	
	public static StopwordsDetector getDefault(Language language) {
		return defaultInstances.get(language);
	}
	
	public static void loadDefault(Language lang, StopwordsDetector instance) {
		if(defaultInstances.get(lang) == null) {
			defaultInstances.put(lang, instance);
		}
	}*/
}
