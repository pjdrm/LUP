package l2f.corpus.parser.other;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import l2f.corpus.CorpusClassifier;
import l2f.corpus.Utterance;
import l2f.corpus.parser.CorpusParser;

public class DefaultParser implements CorpusParser{

	@Override
	public CorpusClassifier parseCorpus(String corpusPath, String corpusProperties) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(corpusPath), "UTF-8"));
			in.readLine();
			String line;
			ArrayList<Utterance> utterances = new ArrayList<Utterance>();
			while((line = in.readLine()) != null){
				if(line.trim().isEmpty())
					continue;
				int index = line.indexOf(" ");
				String cat = line.substring(0, index);
				String question = line.substring(index+1);
				utterances.add(new Utterance(cat, question));
			}
			return new CorpusClassifier(utterances, corpusProperties);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean canProcessCorpus(String corpusPath) {
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(corpusPath), "UTF-8"));
			String line = in.readLine();
			return line.equals("@default");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

}
