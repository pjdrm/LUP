package l2f.evaluator.entropy;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import l2f.config.Config;
import l2f.config.ConfigCrossEntropy;
import l2f.corpus.Corpus;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.factory.CorpusClassifierFactory;
import l2f.corpus.parser.CorpusParser;
import l2f.corpus.parser.other.DefaultParser;
import l2f.corpus.parser.qa.QAParser;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.interpretation.classification.features.FeatureSet;

public class CrossEntropyFactory {

	private ArrayList<CorpusParser> corpusParsers = new ArrayList<CorpusParser>();
	private ArrayList<QuestionClassifierEvaluator> qceArray;
	private boolean alreadyPrepared = false;
	private CorpusClassifierFactory ccFact;
	private CorpusClassifier cc;
	private String corpusPropertiesPath;
	private List<Integer> nGrams = new ArrayList<Integer>();
	private int maxPredictions;
	
	public CrossEntropyFactory(String featuresStr, String corpusPropertiesPath, int maxPredictions){
		initializeCorpusParsers();
		getFeatures(featuresStr);
		this.corpusPropertiesPath = corpusPropertiesPath;
		this.maxPredictions = maxPredictions;
	}
	
	public void initializeCorpusParsers(){
		corpusParsers.add(new QAParser());
		corpusParsers.add(new DefaultParser());
	}
	
	private void prepareFactory(String corpusDomain){
		qceArray = new ArrayList<QuestionClassifierEvaluator>();
		if(!alreadyPrepared){
			ccFact = new CorpusClassifierFactory(corpusParsers);
			cc = ccFact.parseCorpus(Config.corpusDir + "/" + corpusDomain);
			alreadyPrepared = true;
		}
	}
	
	private void getFeatures(String features) {
		StringTokenizer strTokenizer = new StringTokenizer(features, ",");
		String feature;
		while(strTokenizer.hasMoreTokens()){
			feature = strTokenizer.nextToken();
			feature = feature.replaceAll("-", "");
			if(feature.equals(FeatureSet.BIGRAM.getShortName()))
				nGrams.add(2);
			else if(feature.equals(FeatureSet.TRIGRAM.getShortName()))
				nGrams.add(3);
			else if(feature.equals(FeatureSet.UNIGRAM.getShortName()))
				nGrams.add(1);
			else if(feature.equals(FeatureSet.FOURGRAM.getShortName()))
				nGrams.add(4);
			else if(feature.equals(FeatureSet.FIVEGRAM.getShortName()))
				nGrams.add(5);
			else if(feature.equals(FeatureSet.SIXGRAM.getShortName()))
				nGrams.add(6);
			else if(feature.equals(FeatureSet.SEVENGRAM.getShortName()))
				nGrams.add(7);
			else{
				System.err.println("ERROR:\nInvalid feature " + feature);
				System.exit(1);
			}
		}
	}
	
	private void runClassifiers(ArrayList<QuestionClassifierEvaluator> qceArray){
		for(QuestionClassifierEvaluator qce : qceArray){
			qce.setCorpus(cc);
			qce.runClassification();
		}
	}
	
	public ArrayList<QuestionClassifierEvaluator> getCrossEntropyQCE(String corpusDomain) {
		prepareFactory(corpusDomain);
		ArrayList<QuestionClassifierEvaluator> qceCEArray = getRawCrossEntropyQCE();
		runClassifiers(qceCEArray);
		return qceCEArray;
	}

	public ArrayList<QuestionClassifierEvaluator> getRawCrossEntropyQCE() {
		qceArray = new ArrayList<QuestionClassifierEvaluator>();
		Corpus corpus = new Corpus(corpusPropertiesPath);
		for(Integer nGramOrder : nGrams){
			QuestionClassifierEvaluatorCrossEntropy qceCE = new QuestionClassifierEvaluatorCrossEntropy(corpus.getUtteranceProcessor(), nGramOrder, maxPredictions);
			qceArray.add(qceCE);
		}
		return qceArray;
	}
	
}
