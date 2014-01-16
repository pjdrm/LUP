package l2f.evaluator.vsm;

import java.util.ArrayList;
import java.util.List;

import l2f.config.Config;
import l2f.corpus.Corpus;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.factory.CorpusClassifierFactory;
import l2f.corpus.parser.CorpusParser;
import l2f.corpus.parser.other.DefaultParser;
import l2f.corpus.parser.qa.QAParser;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.distance.algorithms.NgramGenerator;
import l2f.nlp.SimpleTokenizer;

public class VSMFactory {

	private ArrayList<CorpusParser> corpusParsers = new ArrayList<CorpusParser>();
	private ArrayList<QuestionClassifierEvaluator> qceArray;
	private boolean alreadyPrepared = false;
	private CorpusClassifierFactory ccFact;
	private CorpusClassifier cc;
	private String corpusPropertiesPath;
	private int maxPredictions;
	private List<Integer> nGrams;
	private List<Double> weights;
	private String[] freqCounters;
	private String[] k_neighbours;

	public VSMFactory(String corpusPropertiesPath, int maxPredictions, String  k_neighboursStr, String features, String utteranceWeights, String frequencyCounters){
		initializeCorpusParsers();
		this.corpusPropertiesPath = corpusPropertiesPath;
		this.maxPredictions = maxPredictions;
		nGrams = NgramGenerator.getNgramOrder(features);
		weights = getWeights(utteranceWeights);
		freqCounters = getFreqCounters(frequencyCounters);
		k_neighbours = getKNeighbours(k_neighboursStr);
	}

	private String[] getFreqCounters(String frequencyCounters) {
		frequencyCounters = frequencyCounters.replaceAll(" +", "");
		return frequencyCounters.split(",");
	}

	private String[] getKNeighbours(String k_neighboursStr) {
		k_neighboursStr = k_neighboursStr.replaceAll(" +", "");
		return k_neighboursStr.split(",");
	}

	private List<Double> getWeights(String utteranceWeights) {
		List<Double> weightsList = new ArrayList<Double>();
		String[] weights = utteranceWeights.split(" ");
		for(String weight : weights){
			weightsList.add(Double.parseDouble(weight));
		}
		return weightsList;
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

	public ArrayList<QuestionClassifierEvaluator> getVSMQCE(String corpusDomain) {
		prepareFactory(corpusDomain);
		ArrayList<QuestionClassifierEvaluator> qceCEArray = getRawVSMQCE();
		runClassifiers(qceCEArray);
		return qceCEArray;
	}

	public ArrayList<QuestionClassifierEvaluator> getRawVSMQCE() {
		qceArray = new ArrayList<QuestionClassifierEvaluator>();
		Corpus corpus = new Corpus(corpusPropertiesPath);
		for(Integer nGramOrder : nGrams){
			for(Double weight : weights){
				for(String freqCount : freqCounters){
					for(String k_neighbour : k_neighbours){
						QuestionClassifierEvaluatorVectorSpaceModel qceCE = new QuestionClassifierEvaluatorVectorSpaceModel(corpus.getUtteranceProcessor(), nGramOrder.intValue(), maxPredictions, Integer.valueOf(k_neighbour), new SimpleTokenizer(), weight.doubleValue(), freqCount);
						qceArray.add(qceCE);
					}
				}
			}
		}

		return qceArray;
	}
}
