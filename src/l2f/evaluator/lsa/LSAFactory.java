package l2f.evaluator.lsa;

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
import l2f.evaluator.distance.algorithms.NgramGenerator;
import l2f.interpretation.classification.features.FeatureSet;
import l2f.nlp.SimpleTokenizer;

public class LSAFactory {

	private ArrayList<CorpusParser> corpusParsers = new ArrayList<CorpusParser>();
	private ArrayList<QuestionClassifierEvaluator> qceArray;
	private boolean alreadyPrepared = false;
	private CorpusClassifierFactory ccFact;
	private CorpusClassifier cc;
	private String corpusPropertiesPath;
	private List<Integer> nGrams;
	private List<String> vectorCalculus = new ArrayList<String>();
	private int maxPredictions;
	private List<String> maxFactorsList;
	private List<String> featureInitList;
	private List<String> initialLearningRateList;
	private List<String> annealingRateList;
	private List<String> regularizationList;
	private List<String> minImprovementList;
	private List<String> minEpochsList;
	private List<String> maxEpochsList;

	public LSAFactory(String featuresStr, 
			String vectorCalc, 
			String corpusPropertiesPath, 
			int maxPredictions,
			String maxFactors,
			String featureInit,
			String initialLearningRate,
			String annealingRate,
			String regularization,
			String minImprovement,
			String minEpochs,
			String maxEpochs){
		initializeCorpusParsers();
		nGrams = NgramGenerator.getNgramOrder(featuresStr);
		getVectorCalc(vectorCalc);
		this.maxFactorsList = getParameters(maxFactors);
		this.featureInitList = getParameters(featureInit);
		this.initialLearningRateList = getParameters(initialLearningRate);
		this.annealingRateList = getParameters(annealingRate);
		this.regularizationList = getParameters(regularization);
		this.minImprovementList = getParameters(minImprovement);
		this.minEpochsList = getParameters(minEpochs);
		this.maxEpochsList = getParameters(maxEpochs);
		this.corpusPropertiesPath = corpusPropertiesPath;
		this.maxPredictions = maxPredictions;
	}

	private List<String> getParameters(String str) {
		str = str.replaceAll(" ", "");
		StringTokenizer strTokenizer = new StringTokenizer(str, ",");
		List<String> params = new ArrayList<String>(); 
		String vc;
		while(strTokenizer.hasMoreTokens()){
			params.add(strTokenizer.nextToken());
		}
		return params;
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

	private void getVectorCalc(String vectorCalc) {
		vectorCalc = vectorCalc.replaceAll(" ", "");
		StringTokenizer strTokenizer = new StringTokenizer(vectorCalc, ",");
		String vc;
		while(strTokenizer.hasMoreTokens()){
			vc = strTokenizer.nextToken();
			if(vc.equals(VectorCalculusSet.DP.getShortName()))
				vectorCalculus.add(vc);
			else if(vc.equals(VectorCalculusSet.CS.getShortName()))
				vectorCalculus.add(vc);
			else{
				System.err.println("ERROR:\nInvalid vector operation " + vc);
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

	public ArrayList<QuestionClassifierEvaluator> getLSAQCE(String corpusDomain) {
		prepareFactory(corpusDomain);
		ArrayList<QuestionClassifierEvaluator> qceCEArray = getRawLSAQCE();
		runClassifiers(qceCEArray);
		return qceCEArray;
	}

	public ArrayList<QuestionClassifierEvaluator> getRawLSAQCE() {
		qceArray = new ArrayList<QuestionClassifierEvaluator>();
		Corpus corpus = new Corpus(corpusPropertiesPath);
		for(String vc : vectorCalculus){
			for(Integer nGramOrder : nGrams){
				for(String maxFactors : maxFactorsList){
					for(String featureInit : featureInitList){
						for(String initialLearningRate : initialLearningRateList){
							for(String annealingRate : annealingRateList){
								for(String regularization : regularizationList){
									for(String minImprovement : minImprovementList){
										for(String minEpochs : minEpochsList){
											for(String maxEpochs : maxEpochsList){
												if(vc.equals(VectorCalculusSet.DP.getShortName()))
													qceArray.add(new QuestionClassifierEvaluatorLSADP(corpus.getUtteranceProcessor(), 
															nGramOrder, maxPredictions, 
															new SimpleTokenizer(), 
															Integer.parseInt(maxFactors), 
															Double.parseDouble(featureInit), 
															Double.parseDouble(initialLearningRate), 
															Integer.parseInt(annealingRate), 
															Double.parseDouble(regularization), 
															Double.parseDouble(minImprovement), 
															Integer.parseInt(minEpochs), 
															Integer.parseInt(maxEpochs)));
												else if(vc.equals(VectorCalculusSet.CS.getShortName()))
													qceArray.add(new QuestionClassifierEvaluatorLSACS(corpus.getUtteranceProcessor(), 
															nGramOrder,
															maxPredictions,
															new SimpleTokenizer(),
															Integer.parseInt(maxFactors), 
															Double.parseDouble(featureInit), 
															Double.parseDouble(initialLearningRate), 
															Integer.parseInt(annealingRate), 
															Double.parseDouble(regularization), 
															Double.parseDouble(minImprovement), 
															Integer.parseInt(minEpochs), 
															Integer.parseInt(maxEpochs)));
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return qceArray;
	}

}
