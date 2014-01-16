package l2f.corpus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import l2f.config.Config;
import l2f.corpus.processor.NEModifier;
import l2f.corpus.processor.NormalizeStringModifier;
import l2f.corpus.processor.POSTaggerModifier;
import l2f.corpus.processor.RegularExpressionModifier;
import l2f.corpus.processor.StopWordModifier;
import l2f.corpus.processor.UtteranceProcessor;

public class Corpus {

	protected List<Utterance> testUtterances = new ArrayList<Utterance>();
	protected List<Utterance> trainUtterances = new ArrayList<Utterance>();
	protected UtteranceProcessor utProcessor = new UtteranceProcessor(); 

	public Corpus(){
		
	}
	
	public Corpus(String corpusProperties){
		addModifiers(corpusProperties);
	}

	public List<Utterance> getTestUtterances() {
		return testUtterances;
	}

	public void setTestUtterances(List<Utterance> testUtterances) {
		this.testUtterances = testUtterances;
	}

	public List<Utterance> getTrainUtterances() {
		return trainUtterances;
	}

	public void setTrainUtterances(List<Utterance> trainUtterances) {
		this.trainUtterances = trainUtterances;
	}

	public void processCorpus(){

		System.out.println("Going to apply string modifiers to corpus.");
		/*for(Utterance ut : testUtterances){
			processUtterance(ut);
		}*/

		for(Utterance ut : trainUtterances){
			processUtterance(ut);
		}

		// MAJOR HACK DEPLOY WONT WORK!
		//		utProcessor = new UtteranceProcessor();
	}

	protected void processUtterance(Utterance ut) {
		utProcessor.processUtterance(ut);		
	}

	private void addModifiers(String corpusProperties){
		if(Config.posTaggerFlag)
			utProcessor.addModifier(new POSTaggerModifier(Config.posTagFile, Config.posTagFile));
		
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(new File(corpusProperties).getCanonicalPath()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String processNE = props.getProperty("processNE");
		if(processNE != null && Boolean.parseBoolean(processNE)){
			utProcessor.addModifier(new NEModifier(props.getProperty("ne")));
		}
		
		if(Config.stopwordsFlag)
			utProcessor.addModifier(new StopWordModifier(Config.stopwordsFile));

		if(Config.normalizeStringFlag)
			utProcessor.addModifier(new NormalizeStringModifier());
		
		String processRE = props.getProperty("processRE");
		if(processRE != null && Boolean.parseBoolean(processRE)){
			utProcessor.addModifier(new RegularExpressionModifier(props.getProperty("re")));
		}
	}

	public UtteranceProcessor getUtteranceProcessor(){
		return utProcessor;
	}

}
