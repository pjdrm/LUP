package l2f.evaluator.logistic.regression;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import l2f.config.Config;
import l2f.corpus.Corpus;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.factory.CorpusClassifierFactory;
import l2f.corpus.parser.CorpusParser;
import l2f.corpus.parser.other.DefaultParser;
import l2f.corpus.parser.qa.QAParser;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.entropy.QuestionClassifierEvaluatorCrossEntropy;
import l2f.interpretation.classification.features.FeatureSet;

public class LogisticRegreessionFactory {
	private ArrayList<CorpusParser> corpusParsers = new ArrayList<CorpusParser>();
	private ArrayList<QuestionClassifierEvaluator> qceArray;
	private boolean alreadyPrepared = false;
	private CorpusClassifierFactory ccFact;
	private CorpusClassifier cc;
	private String corpusPropertiesPath;
	private int maxPredictions;
	
	public LogisticRegreessionFactory(String corpusPropertiesPath, int maxPredictions){
		initializeCorpusParsers();
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
	
	private void runClassifiers(ArrayList<QuestionClassifierEvaluator> qceArray){
		for(QuestionClassifierEvaluator qce : qceArray){
			qce.setCorpus(cc);
			qce.runClassification();
		}
	}
	
	public ArrayList<QuestionClassifierEvaluator> getLogisticRegressionQCE(String corpusDomain) {
		prepareFactory(corpusDomain);
		ArrayList<QuestionClassifierEvaluator> qceCEArray = getRawLogisticRegressionQCE();
		runClassifiers(qceCEArray);
		return qceCEArray;
	}

	public ArrayList<QuestionClassifierEvaluator> getRawLogisticRegressionQCE() {
		qceArray = new ArrayList<QuestionClassifierEvaluator>();
		Corpus corpus = new Corpus(corpusPropertiesPath);
		QuestionClassifierEvaluatorLogisticRegression qceCE = new QuestionClassifierEvaluatorLogisticRegression(corpus.getUtteranceProcessor(), maxPredictions);
		qceArray.add(qceCE);
		return qceArray;
	}
}
