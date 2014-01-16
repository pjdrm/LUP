package l2f.evaluator.distance.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import l2f.config.Config;
import l2f.config.ConfigDistance;
import l2f.corpus.Corpus;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.factory.CorpusClassifierFactory;
import l2f.corpus.parser.CorpusParser;
import l2f.corpus.parser.other.DefaultParser;
import l2f.corpus.parser.qa.QAParser;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.distance.algorithms.dice.QuestionClassifierEvaluatorDice;
import l2f.evaluator.distance.algorithms.jaccard.QuestionClassifierEvaluatorJOTfidf;
import l2f.evaluator.distance.algorithms.jaccard.QuestionClassifierEvaluatorJaccard;
import l2f.evaluator.distance.algorithms.jaccard.QuestionClassifierEvaluatorJaccardOverlap;
import l2f.evaluator.distance.algorithms.overlap.QuestionClassifierEvaluatorOverlap;
import l2f.evaluator.distance.algorithms.set.intersection.SetIntersection;
import l2f.evaluator.distance.algorithms.set.intersection.SetIntersectionFactory;
import l2f.interpretation.classification.features.FeatureSet;
import l2f.nlp.SimpleTokenizer;

public class DistanceFactory {

	private ArrayList<CorpusParser> distanceParsers = new ArrayList<CorpusParser>();
	private CorpusClassifierFactory ccFact;
	private CorpusClassifier cc;
	private ArrayList<QuestionClassifierEvaluator> qceArray;
	private ArrayList<FeatureSet> features;
	private List<Integer> k_neighboursList;
	private boolean alreadyPrepared = false;
	private String corpusPropertiesPath;
	private int maxPredictions = 1;
	private String[] setIntersectionArray;

	public DistanceFactory(String featuresStr, String k_neighbours, String setIntersections, String corpusPropertiesPath){
		initializeDistanceParsers();
		this.features = getFeatures(featuresStr);
		this.k_neighboursList = getKNeighboursList(k_neighbours);
		this.setIntersectionArray = setIntersections.split(",");
		this.corpusPropertiesPath = corpusPropertiesPath;
	}

	public DistanceFactory(String featuresStr, String k_neighbours, String setIntersections, String corpusPropertiesPath, int maxPredictions){
		this(featuresStr, k_neighbours, setIntersections, corpusPropertiesPath);
		this.maxPredictions = maxPredictions;
	}

	public void initializeDistanceParsers(){
		distanceParsers.add(new QAParser());
		distanceParsers.add(new DefaultParser());
	}

	private void prepareFactory(String corpusDomain){
		qceArray = new ArrayList<QuestionClassifierEvaluator>();
		if(!alreadyPrepared){
			ConfigDistance.parseConfig();
			ccFact = new CorpusClassifierFactory(distanceParsers);
			cc = ccFact.parseCorpus(Config.corpusDir + "/" + corpusDomain);
			alreadyPrepared = true;
			//			features = getFeatures(ConfigDistance.features);
		}
	}

	private ArrayList<FeatureSet> getFeatures(String features) {
		StringTokenizer strTokenizer = new StringTokenizer(features, ",");
		String feature;
		ArrayList<FeatureSet> fsArray = new ArrayList<FeatureSet>();
		while(strTokenizer.hasMoreTokens()){
			feature = strTokenizer.nextToken();
			feature = feature.replaceAll("-", "");
			if(feature.equals(FeatureSet.BIGRAM.getShortName()))
				fsArray.add(FeatureSet.BIGRAM);
			else if(feature.equals(FeatureSet.TRIGRAM.getShortName()))
				fsArray.add(FeatureSet.TRIGRAM);
			else if(feature.equals(FeatureSet.UNIGRAM.getShortName()))
				fsArray.add(FeatureSet.UNIGRAM);
			else if(feature.equals(FeatureSet.FOURGRAM.getShortName()))
				fsArray.add(FeatureSet.FOURGRAM);
			else if(feature.equals(FeatureSet.FIVEGRAM.getShortName()))
				fsArray.add(FeatureSet.FIVEGRAM);
			else if(feature.equals(FeatureSet.SIXGRAM.getShortName()))
				fsArray.add(FeatureSet.SIXGRAM);
			else if(feature.equals(FeatureSet.SEVENGRAM.getShortName()))
				fsArray.add(FeatureSet.SEVENGRAM);
			else{
				System.err.println("ERROR:\nInvalid feature " + feature);
				System.exit(1);
			}
		}
		return fsArray;
	}

	private List<Integer> getKNeighboursList(String k_neighbours) {
		List<Integer> knList = new ArrayList<Integer>();
		k_neighbours = k_neighbours.replaceAll(" +", "");
		String[] knArray = k_neighbours.split(",");
		for(int i = 0; i < knArray.length ; i++){
			knList.add(Integer.valueOf(knArray[i]));
		}
		return knList;
	}

	private void runClassifiers(ArrayList<QuestionClassifierEvaluator> qceArray){
		for(QuestionClassifierEvaluator qce : qceArray){
			qce.setCorpus(cc);
			qce.runClassification();
		}
	}

	public ArrayList<QuestionClassifierEvaluator> getDiceQCE(String corpusDomain) {
		prepareFactory(corpusDomain);
		ArrayList<QuestionClassifierEvaluator> qceDiceArray = getRawDiceQCE();
		runClassifiers(qceDiceArray);
		return qceDiceArray;
	}

	public ArrayList<QuestionClassifierEvaluator> getRawDiceQCE() {
		//		prepareFactory(corpusDomain);
		qceArray = new ArrayList<QuestionClassifierEvaluator>();
		Corpus corpus = new Corpus(corpusPropertiesPath);
		for(FeatureSet fs : features){
			for(Integer kn : k_neighboursList){
				for(String setIntersectionArgs : setIntersectionArray){
					QuestionClassifierEvaluatorDice qceDice = new QuestionClassifierEvaluatorDice(corpus.getUtteranceProcessor(), new DistanceAlgorithmArguments(fs), new SimpleTokenizer(), maxPredictions, kn, SetIntersectionFactory.getSetIntersection(setIntersectionArgs));
					qceArray.add(qceDice);
				}
			}
		}
		return qceArray;
	}

	public ArrayList<QuestionClassifierEvaluator> getJaccardQCE(String corpusDomain) {
		prepareFactory(corpusDomain);
		ArrayList<QuestionClassifierEvaluator> qceJaccardArray = getRawJaccardQCE();
		runClassifiers(qceJaccardArray);
		return qceJaccardArray;
	}

	public ArrayList<QuestionClassifierEvaluator> getRawJaccardQCE() {
		//		prepareFactory(corpusDomain);
		qceArray = new ArrayList<QuestionClassifierEvaluator>();
		Corpus corpus = new Corpus(corpusPropertiesPath);
		for(FeatureSet fs : features){
			for(Integer kn : k_neighboursList){
				for(String setIntersectionArgs : setIntersectionArray){
					QuestionClassifierEvaluatorJaccard qceJaccard = new QuestionClassifierEvaluatorJaccard(corpus.getUtteranceProcessor(), new DistanceAlgorithmArguments(fs), new SimpleTokenizer(), maxPredictions, kn, SetIntersectionFactory.getSetIntersection(setIntersectionArgs));
					qceArray.add(qceJaccard);
				}
			}
		}
		return qceArray;
	}

	public ArrayList<QuestionClassifierEvaluator> getJaccardOverlapQCE(String corpusDomain) {
		prepareFactory(corpusDomain);
		ArrayList<QuestionClassifierEvaluator> qceJaccardOverlapArray = getRawJaccardOverlapQCE();
		runClassifiers(qceJaccardOverlapArray);

		return qceJaccardOverlapArray;
	}

	public ArrayList<QuestionClassifierEvaluator> getRawJaccardOverlapQCE() {
		//		prepareFactory(corpusDomain);
		qceArray = new ArrayList<QuestionClassifierEvaluator>();
		Corpus corpus = new Corpus(corpusPropertiesPath);
		for(FeatureSet fs : features){
			for(Integer kn : k_neighboursList){
				for(String setIntersectionArgs : setIntersectionArray){
					QuestionClassifierEvaluatorJaccardOverlap qceJaccardOverlap = new QuestionClassifierEvaluatorJaccardOverlap(corpus.getUtteranceProcessor(), new DistanceAlgorithmArguments(fs), new SimpleTokenizer(), maxPredictions, kn, SetIntersectionFactory.getSetIntersection(setIntersectionArgs));
					qceArray.add(qceJaccardOverlap);
				}
			}
		}
		return qceArray;
	}

	public ArrayList<QuestionClassifierEvaluator> getJaccardOverlapTfidfQCE(String corpusDomain) {
		prepareFactory(corpusDomain);
		ArrayList<QuestionClassifierEvaluator> qceJaccardOverlapTfidfArray = getRawJaccardOverlapTfidfQCE();
		runClassifiers(qceJaccardOverlapTfidfArray);

		return qceJaccardOverlapTfidfArray;
	}

	public ArrayList<QuestionClassifierEvaluator> getRawJaccardOverlapTfidfQCE() {
		//		prepareFactory(corpusDomain);
		qceArray = new ArrayList<QuestionClassifierEvaluator>();
		Corpus corpus = new Corpus(corpusPropertiesPath);
		for(FeatureSet fs : features){
			for(Integer kn : k_neighboursList){
				QuestionClassifierEvaluatorJOTfidf qceJaccardOverTfidflap = new QuestionClassifierEvaluatorJOTfidf(corpus.getUtteranceProcessor(), new DistanceAlgorithmArguments(fs), new SimpleTokenizer(), maxPredictions, kn);
				qceArray.add(qceJaccardOverTfidflap);
			}
		}
		return qceArray;
	}

	public ArrayList<QuestionClassifierEvaluator> getOverlapQCE(String corpusDomain) {
		prepareFactory(corpusDomain);
		ArrayList<QuestionClassifierEvaluator> qceOverlapArray = getRawOverlapQCE();
		runClassifiers(qceOverlapArray);

		return qceOverlapArray;
	}

	public ArrayList<QuestionClassifierEvaluator> getRawOverlapQCE() {
		//		prepareFactory(corpusDomain);
		qceArray = new ArrayList<QuestionClassifierEvaluator>();
		Corpus corpus = new Corpus(corpusPropertiesPath);
		for(FeatureSet fs : features){
			for(Integer kn : k_neighboursList){
				for(String setIntersectionArgs : setIntersectionArray){
					QuestionClassifierEvaluatorOverlap qceOverlap = new QuestionClassifierEvaluatorOverlap(corpus.getUtteranceProcessor(), new DistanceAlgorithmArguments(fs), new SimpleTokenizer(), maxPredictions, kn, SetIntersectionFactory.getSetIntersection(setIntersectionArgs));
					qceArray.add(qceOverlap);
				}
			}
		}
		return qceArray;
	}

}
