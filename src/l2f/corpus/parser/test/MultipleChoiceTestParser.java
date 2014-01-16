package l2f.corpus.parser.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import l2f.corpus.CorpusClassifier;
import l2f.corpus.CorpusClassifierReference;
import l2f.corpus.Utterance;
import l2f.corpus.parser.CorpusParser;

public class MultipleChoiceTestParser implements CorpusParser{

	@Override
	public CorpusClassifier parseCorpus(String corpusPath, String corpusProperties) {
		try {
			ArrayList<Utterance> questionsOutOfDomain = new ArrayList<Utterance>();
			Reader reader = new InputStreamReader(new FileInputStream(corpusPath), "UTF-8");
			BufferedReader br = new BufferedReader(reader);
			String line;
			while((line  = br.readLine()) != null){
				if(line.contains("?"))
					questionsOutOfDomain.add(new Utterance("CAT_OUT_DOMAIN", line));
				
			}
			return new CorpusClassifierReference(new ArrayList<Utterance>(), questionsOutOfDomain, new ArrayList<Utterance>(), new ArrayList<Utterance>());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	@Override
	public boolean canProcessCorpus(String corpusPath) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(corpusPath)));
			String line = br.readLine();
			
			if(line == null || !line.contains("-"))
				return false;
			
			line = br.readLine();
			line = br.readLine();
			if(line == null || !line.contains("?"))
				return false;
			
			line = br.readLine();
			line = br.readLine();
			if(line == null || !line.contains("A:"))
				return false;
			
			line = br.readLine();
			if(line == null || !line.contains("B:"))
				return false;
			
			line = br.readLine();
			if(line == null || !line.contains("C:"))
				return false;
			
			line = br.readLine();
			if(line == null || !line.contains("D:"))
				return false;
			
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return false;
	}
	
}
