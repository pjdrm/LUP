package l2f.corpus.parser.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import l2f.corpus.CorpusClassifier;
import l2f.corpus.CorpusSet;
import l2f.corpus.Utterance;
import l2f.corpus.parser.CorpusParser;

public class UserTestParser implements CorpusParser{

	@Override
	public CorpusClassifier parseCorpus(String corpusPath, String corpusProperties) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(corpusPath), "UTF-8"));
			String str;
			StringTokenizer strTokenizer;
			String token;
			ArrayList<Utterance> testUtterances = new ArrayList<Utterance>();
			int qIndex = 1;
			boolean beginTest = true;
			String cat = "";
			while((str = br.readLine()) != null){
				if(str.contains("# Test set")){
					str = br.readLine();
					str = str.replace("Questions: ", "");
					strTokenizer = new StringTokenizer(str, ";");
					while(strTokenizer.hasMoreTokens()){
						token = strTokenizer.nextToken();
						if(beginTest){
							cat = "BT_Q"+qIndex+":";
							beginTest = false;
						}
						else
							cat = "Q"+qIndex+":";
						testUtterances.add(new Utterance(cat, token));
						qIndex++;
					}
					beginTest = true;
				}
			}
			CorpusClassifier cc = new CorpusClassifier(corpusProperties);
			cc.setTestUtterances(testUtterances);
			br.close();
			return cc;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	@Override
	public boolean canProcessCorpus(String corpusPath) {
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(corpusPath), "UTF-8"));
			String type = r.readLine();
			r.close();
			if(type.equals(CorpusSet.USERTEST.type()))
				return true;
			else
				return false;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return false;
	}

}
