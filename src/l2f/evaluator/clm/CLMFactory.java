package l2f.evaluator.clm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;

import l2f.config.Config;
import l2f.config.ConfigCLM;
import l2f.corpus.Corpus;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.factory.CorpusClassifierFactory;
import l2f.corpus.parser.CorpusParser;
import l2f.corpus.parser.other.DefaultParser;
import l2f.corpus.parser.qa.QAParser;
import l2f.evaluator.QuestionClassifierEvaluator;

public class CLMFactory {
	
	private ArrayList<CorpusParser> clmParsers = new ArrayList<CorpusParser>();
	private String corpusPropertiesPath;

	public CLMFactory(String corpusPropertiesPath){
		this.corpusPropertiesPath = corpusPropertiesPath;
		initializeCLMParsers();
	}
	
	public void initializeCLMParsers(){
		clmParsers.add(new QAParser());
		clmParsers.add(new DefaultParser());
	}
	
	public ArrayList<QuestionClassifierEvaluator> getCLMQCE(String corpusDomain) {
		ConfigCLM.parseConfig();
		CorpusClassifierFactory ccFact = new CorpusClassifierFactory(clmParsers);
		CorpusClassifier cc = ccFact.parseCorpus(Config.corpusDir + "/" + corpusDomain);
		ArrayList<QuestionClassifierEvaluator> qceCLMArray = getRawCLMQCE(corpusDomain);
		for(QuestionClassifierEvaluator qce : qceCLMArray){
			qce.setCorpus(cc);
			qce.runClassification();
		}
		
		return qceCLMArray;
	}
	
	public ArrayList<QuestionClassifierEvaluator> getRawCLMQCE(String corpusDomain) {
//		ConfigCLM.parseConfig();
//		CorpusClassifierFactory ccFact = new CorpusClassifierFactory(clmParsers);
//		CorpusClassifier cc = ccFact.parseCorpus(Config.corpusDir + "/" + corpusDomain);
		Corpus corpus = new Corpus(corpusPropertiesPath);
		ArrayList<QuestionClassifierEvaluator> qceCLMArray = new ArrayList<QuestionClassifierEvaluator>();
		QuestionClassifierEvaluatorCLM clmQCE = new QuestionClassifierEvaluatorCLM(corpus.getUtteranceProcessor(), getDomainConfig(Config.corpusDir + "/" + corpusDomain));
		qceCLMArray.add(clmQCE);
		return qceCLMArray;
	}
	
	private static String getDomainConfig(String corpusDirPath) {
		String clmConfig = null;
		try {
			Properties props = new Properties();
			File corpusDir = new File(corpusDirPath);
			for(File f : corpusDir.listFiles()){
				if(f.isDirectory())
					continue;
				else if(f.getName().substring(f.getName().lastIndexOf(".")).equals(".properties")){
					props.load(new FileInputStream(f));
					clmConfig = props.getProperty(ConfigCLM.clmDomainConfigProp);
					if(clmConfig == null){
						System.out.println("ERROR: Property clmConfig required");
						System.exit(1);
					}
					break;
				}
				
			}
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return clmConfig;
	}
}
