package l2f.evaluator.svm;

import java.util.ArrayList;
import java.util.StringTokenizer;

import l2f.ClassifierApp;
import l2f.config.Config;
import l2f.config.ConfigSVM;
import l2f.corpus.Corpus;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.factory.CorpusClassifierFactory;
import l2f.corpus.parser.CorpusParser;
import l2f.corpus.parser.other.DefaultParser;
import l2f.corpus.parser.qa.QAParser;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.SVMArguments;
import l2f.interpretation.classification.features.FeatureSet;

public class SVMFactory {
	
	private ArrayList<CorpusParser> svmParsers = new ArrayList<CorpusParser>();
	private String corpusPropertiesPath;
	
	public SVMFactory(String corpusPropertiesPath){
		this.corpusPropertiesPath = corpusPropertiesPath;
		initializeSVMParsers();
	}
	
	public void initializeSVMParsers(){
		svmParsers.add(new QAParser());
		svmParsers.add(new DefaultParser());
	}
	
	public static ArrayList<ArrayList<FeatureSet>> processSVMFeatures(String featuresStr) {
		ArrayList<ArrayList<FeatureSet>> featuresResult = new ArrayList<ArrayList<FeatureSet>>();
		if(featuresStr.contains(",")){
			StringTokenizer strTokenizer = new StringTokenizer(featuresStr, ",");
			String token;
			while(strTokenizer.hasMoreTokens()){
				token = strTokenizer.nextToken();
				featuresResult.add(getFeaturesComb(token));
			}
		} 
		else
			featuresResult.add(getFeaturesComb(featuresStr));

		return featuresResult;
	}
	
	public static ArrayList<FeatureSet> getFeaturesComb(String featuresStr) {
		ArrayList<FeatureSet> featuresArray = new ArrayList<FeatureSet>();
		StringTokenizer strToken = new StringTokenizer(featuresStr, "-");
		String featuresToken = "";
		while(strToken.hasMoreTokens()){
			featuresToken = strToken.nextToken();

			if(!featureExists(featuresToken)){
				String availableFeatures = "";
				for(FeatureSet fs : FeatureSet.values()){
					if(fs.equals(FeatureSet.DUMMY))
						continue;

					availableFeatures += fs.getShortName() + "\n";
				}
				System.err.println("ERROR:\nInvalid feature " + featuresToken+ "\nAvailable features:\n" + availableFeatures);
				System.exit(1);
			}

			if(featuresToken.equals("u")) {
				featuresArray.add(FeatureSet.UNIGRAM);
			}
			if(featuresToken.equals("bu")) {
				featuresArray.add(FeatureSet.BINARY_UNIGRAM);
			}
			if(featuresToken.equals("b")) {
				featuresArray.add(FeatureSet.BIGRAM);
			}
			if (featuresToken.equals("bb")) {
				featuresArray.add(FeatureSet.BINARY_BIGRAM);
			}
			if (featuresToken.equals("t")) {
				featuresArray.add(FeatureSet.TRIGRAM);
			}
			if (featuresToken.equals("bt")) {
				featuresArray.add(FeatureSet.BINARY_TRIGRAM);
			}
			if (featuresToken.equals("c")) {
				featuresArray.add(FeatureSet.CATEGORY);
			}
			if (featuresToken.equals("h")) {
				featuresArray.add(FeatureSet.HEADWORD);
			}
			if (featuresToken.equals("x")) {
				featuresArray.add(FeatureSet.WORD_SHAPE);
			}
			if (featuresToken.equals("bx")) {
				featuresArray.add(FeatureSet.BINARY_WORD_SHAPE);
			}
			if (featuresToken.equals("p")) {
				featuresArray.add(FeatureSet.POS);
			}
			if (featuresToken.equals("l")) {
				featuresArray.add(FeatureSet.LENGTH);
			}
			if (featuresToken.equals("ni")) {
				featuresArray.add(FeatureSet.NER_INCR);
			}
			if (featuresToken.equals("nr")) {
				featuresArray.add(FeatureSet.NER_REPL);
			}
			if (featuresToken.equals("iwlp")) {
				featuresArray.add(FeatureSet.IMPORTANT_WORDS_LIST_PREFFIX);
			}
			if (featuresToken.equals("iwle")) {
				featuresArray.add(FeatureSet.IMPORTANT_WORDS_LIST_EXPR);
			}
			if (featuresToken.equals("iwlo")) {
				featuresArray.add(FeatureSet.IMPORTANT_WORDS_LIST_OTHER);
			}
			if (featuresToken.equals("sw")) {
				featuresArray.add(FeatureSet.STOPWORDS);
			}
		}

		return featuresArray;
	}
	
	public static boolean featureExists(String feature){
		for(FeatureSet fs : FeatureSet.values()){
			if(fs.getShortName().equals(feature))
				return true;
		}
		return false;
	}

	public ArrayList<QuestionClassifierEvaluator> getSVMQCE(String corpusDomain, int nPredictions) {
		CorpusClassifierFactory ccFact = new CorpusClassifierFactory(svmParsers);
		CorpusClassifier cc = ccFact.parseCorpus(Config.corpusDir + "/" + corpusDomain);
		ArrayList<QuestionClassifierEvaluator> qceSVMArray = getRawSVMQCE(corpusDomain, nPredictions);
		for(QuestionClassifierEvaluator qce : qceSVMArray){
			qce.setCorpus(cc);
			qce.runClassification();
		}
		
		return qceSVMArray;
	}
	
	public ArrayList<QuestionClassifierEvaluator> getRawSVMQCE(String corpusDomain, int nPredictions){
		ConfigSVM.parseConfig();
		ArrayList<QuestionClassifierEvaluator> qceSVMArray = new ArrayList<QuestionClassifierEvaluator>();
		ArrayList<ArrayList<FeatureSet>> features = processSVMFeatures(ConfigSVM.features);
		ArrayList<ArrayList<FeatureSet>> featuresDummy;
		
		Corpus corpus = new Corpus(corpusPropertiesPath);
		for(ArrayList<FeatureSet> fs : features){
			featuresDummy = new ArrayList<ArrayList<FeatureSet>>();
			featuresDummy.add(fs);
			ClassifierApp.prepareDirs();
			QuestionClassifierEvaluatorSVM qceSVM = new QuestionClassifierEvaluatorSVM(corpus.getUtteranceProcessor(), new SVMArguments(corpusDomain, featuresDummy), 0.0, nPredictions);
			qceSVMArray.add(qceSVM);
		}
		return qceSVMArray;
	}
}
