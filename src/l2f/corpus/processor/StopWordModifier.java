package l2f.corpus.processor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StopWordModifier extends StringModifier{

	private List<String> stopWords = new ArrayList<String>();
	
	public StopWordModifier(String swFilePath){
		loadStopwords(swFilePath);
	}
	
	private void loadStopwords(String swFilePath) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(swFilePath), "UTF-8"));
			String line;
			char[] stringArray;
			while((line = in.readLine()) != null){
				if(line.trim().isEmpty())
					continue;
				stopWords.add(line);
				stringArray = line.toCharArray();
				stringArray[0] = Character.toUpperCase(stringArray[0]);
				stopWords.add(new String(stringArray));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String modify(String str) {
		for(String stopword : stopWords)
			str = str.replaceAll("\\b" + stopword + "\\b *", "");
		return str;
	}

	@Override
	public String getDescription() {
		return "StopWords";
	}

}
