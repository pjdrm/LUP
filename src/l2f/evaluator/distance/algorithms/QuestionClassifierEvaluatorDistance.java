package l2f.evaluator.distance.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2f.corpus.Corpus;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.Utterance;
import l2f.corpus.processor.UtteranceProcessor;
import l2f.evaluator.QCEAnswer;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.interpretation.classification.features.FeatureSet;
import l2f.nlp.Tokenizer;
import l2f.tests.QCEBaseTester;
import l2f.tests.TesterInterface;

public abstract class QuestionClassifierEvaluatorDistance implements QuestionClassifierEvaluator{

	protected DistanceAlgorithm distanceAlg = null;
	protected CorpusClassifier corpusClassifier;
	protected DistanceAlgorithmArguments daArgs;
	protected ArrayList<DistanceUtterance> nGramCorpusTrain = new ArrayList<DistanceUtterance>();
	protected ArrayList<DistanceUtterance> nGramCorpusTest = new ArrayList<DistanceUtterance>();
	protected int nGramsOrder;
	protected Tokenizer tokenizer;
	protected TesterInterface tester;
	protected UtteranceProcessor utteranceProcessor;
	protected int maxPredictions;
	protected int k_neighbours;
	protected String setIntersectionDesc;

	public QuestionClassifierEvaluatorDistance(CorpusClassifier cc, DistanceAlgorithmArguments args, Tokenizer tokenizer, int maxPredictions, int k_neighbours, String setIntersectionDesc){
		this.corpusClassifier = cc;
		this.utteranceProcessor = cc.getUtteranceProcessor();
		this.daArgs = args;
		this.tokenizer = tokenizer;
		this.maxPredictions = maxPredictions;
		this.k_neighbours = k_neighbours;
		this.setIntersectionDesc = setIntersectionDesc;
		setNGramOrder();
		generateNGramCorpus();
		this.tester = new QCEBaseTester(this);
	}

	public QuestionClassifierEvaluatorDistance(UtteranceProcessor up, DistanceAlgorithmArguments args, Tokenizer tokenizer, int maxPredictions, int k_neighbours, String setIntersectionDesc) {
		this.corpusClassifier = new CorpusClassifier();
		this.utteranceProcessor = up;
		this.daArgs = args;
		this.tokenizer = tokenizer;
		this.maxPredictions = maxPredictions;
		this.k_neighbours = k_neighbours;
		this.setIntersectionDesc = setIntersectionDesc;
		this.tester = new QCEBaseTester(this);
	}

	public DistanceAlgorithm getDistanceAlgorithm(){
		return distanceAlg;
	}

	public int getNgramOrder(){
		return nGramsOrder;
	}

	private void setNGramOrder() {
		FeatureSet fs = daArgs.getFeatureSet();
		if(fs.getShortName().equals(FeatureSet.UNIGRAM)){
			nGramsOrder = 1;
		}
		else if(fs.getShortName().equals(FeatureSet.BIGRAM)){
			nGramsOrder = 2;
		}
		else if(fs.getShortName().equals(FeatureSet.TRIGRAM)){
			nGramsOrder = 3;
		}
		else if(fs.getShortName().equals(FeatureSet.FOURGRAM.getShortName()))
			nGramsOrder = 4;
		else if(fs.equals(FeatureSet.FIVEGRAM.getShortName()))
			nGramsOrder = 5;
		else if(fs.getShortName().equals(FeatureSet.SIXGRAM.getShortName()))
			nGramsOrder = 6;
		else if(fs.getShortName().equals(FeatureSet.SEVENGRAM.getShortName()))
			nGramsOrder = 7;

	}

	public void generateNGramCorpus(){
		nGramCorpusTrain = new ArrayList<DistanceUtterance>();
		for(Utterance trainUt : corpusClassifier.getTrainUtterances()){
			ArrayList<String> words = (ArrayList<String>)tokenizer.tokenize(trainUt.getUtterance());
			nGramCorpusTrain.add(new DistanceUtterance(NgramGenerator.getNGrams(nGramsOrder, words), trainUt.getCat()));
		}

		nGramCorpusTest = new ArrayList<DistanceUtterance>();
		for(Utterance testUt : corpusClassifier.getTestUtterances()){
			ArrayList<String> words = (ArrayList<String>)tokenizer.tokenize(testUt.getUtterance());
			nGramCorpusTest.add(new DistanceUtterance(NgramGenerator.getNGrams(nGramsOrder, words), testUt.getCat()));
		}
	}

	public ArrayList<DistanceUtterance> getTrainCorpus(){
		return nGramCorpusTrain;
	}

	public ArrayList<DistanceUtterance> getTestCorpus(){
		return nGramCorpusTest;
	}

	@Override
	public void runClassification() {

	}

	@Override
	public TesterInterface getTester() {
		return tester;
	}

	@Override
	public ArrayList<String> answerQuestion(String question) {
		return answerWithQCEAnswer(question).getStringPossibleAnswers();
	}

	@Override
	public QCEAnswer answerWithQCEAnswer(String question) {
		question = utteranceProcessor.processString(question);
		String modifications = utteranceProcessor.getModifications();
		double bestScore = -1.0;
		double currentScore;
		List<String> nGramQuestion = NgramGenerator.getNGrams(nGramsOrder, tokenizer.tokenize(question));
		DistanceUtterance answer = null;
		List<DistanceUtterance> candidates = new ArrayList<DistanceUtterance>();
		List<DistanceUtterance> topK_neibours;
		List<String> predCats = new ArrayList<String>();
		int nNeighbours;

		//k-neighbours version
		for(int nPreds = 0; nPreds < maxPredictions ; nPreds++){
			topK_neibours = new ArrayList<DistanceUtterance>();
			nNeighbours = 0;
			for(DistanceUtterance dUt :	nGramCorpusTrain){
				if(containsDistanceUtterance(candidates, dUt))
					continue;
				
				currentScore = distanceAlg.distance(nGramQuestion, dUt.getNGramUtterance());
				dUt.setScore(currentScore);
				dUt.setOriginalScore(currentScore);

				if(nNeighbours < k_neighbours){
					topK_neibours.add(dUt);
					nNeighbours++;
					Collections.sort(topK_neibours);
				}
				else{
					if(topK_neibours.get(nNeighbours-1).getScore() < dUt.getScore()){
						topK_neibours.remove(k_neighbours-1);
						topK_neibours.add(dUt);
						Collections.sort(topK_neibours);
					}
				}
			}

			Map<String, DistanceUtterance> neighboursCountsMap = new HashMap<String, DistanceUtterance>();
			List<DistanceUtterance> catCountList = new ArrayList<DistanceUtterance>();
			for(DistanceUtterance du : topK_neibours){
				if(neighboursCountsMap.get(du.getCat()) == null){
					//Going to lose distance score! Should use getOriginalScore method if want this value
					du.setScore(1.0);
					catCountList.add(du);
					neighboursCountsMap.put(du.getCat(), du);
				}
				else{
					DistanceUtterance dutt = neighboursCountsMap.get(du.getCat());
					dutt.setScore(dutt.getScore()+1.0);
					dutt.highestScore(du.getOriginalScore());
				}
			}
			Collections.sort(catCountList);
			candidates.add(catCountList.get(0));
		}
		
		/*while(nPredictions < maxPredictions && nPredictions < catCountList.size()){
			candidates.add(catCountList.get(nPredictions));
			nPredictions++;
		}*/

		//1-neighbour old version
		/*for(DistanceUtterance dUt :	nGramCorpusTrain){
			currentScore = distanceAlg.distance(nGramQuestion, dUt.getNGramUtterance());
			dUt.setScore(currentScore);
			//if(currentScore > bestScore){
				//bestScore = currentScore;
				//answer = new DistanceUtterance(dUt.getNGramUtterance(), dUt.getCat());
				//Collections.sort(candidates);

				//if(bestScore > 0.9999999999999999) {
				//break; // we wont find better, no need to process the rest of the corpora
				//}
			//}

			if(predCats.contains(dUt.getCat())){
				//skip
			}
			else{
				if(nPredictions < maxPredictions && !predCats.contains(dUt.getCat())){
					candidates.add(dUt);
					nPredictions++;
					predCats.add(dUt.getCat());
					Collections.sort(candidates);
				}
				else{
					if(!predCats.contains(dUt.getCat()) && candidates.get(maxPredictions-1).getScore() < dUt.getScore()){
						predCats.remove(candidates.get(maxPredictions-1).getCat());
						predCats.add(dUt.getCat());
						candidates.remove(maxPredictions-1);
						candidates.add(dUt);
						Collections.sort(candidates);
					}
				}
			}
		}*/

		ArrayList<Utterance> answers = new ArrayList<Utterance>();
		for(DistanceUtterance candidate : candidates){
			for(Utterance ut : corpusClassifier.getAnswer(candidate.getCat())){
				ut.setUtterance(ut.getUtterance());// + "\nMost similar: " + answer.getNGramUtterance() + "\nProcessed question: " + nGramQuestion);
				answers.add(ut);
			}
		}	
		return new QCEAnswer(answers, modifications, getDescription(), candidates.get(0).getScore());
	}

	private boolean containsDistanceUtterance(List<DistanceUtterance> duList, DistanceUtterance dUt) {
		for(DistanceUtterance du : duList)
			if(du.getCat().equals(dUt.getCat()))
				return true;
		return false;
	}

	@Override
	public abstract QuestionEvaluatorSet getType();

	@Override
	public void setCorpus(Corpus corpus) {
		List<Utterance> newTestUtterances = new ArrayList<Utterance>();
		Utterance newUt;
		for(Utterance ut : corpus.getTestUtterances()){
			newUt = new Utterance(ut.getCat(), ut.getUtterance());
			//			utteranceProcessor.processUtterance(newUt);
			newTestUtterances.add(newUt);
		}
		corpusClassifier.setTestUtterances(newTestUtterances);

		List<Utterance> newTrainUtterances = new ArrayList<Utterance>();
		for(Utterance ut : corpus.getTrainUtterances()){
			newUt = new Utterance(ut.getCat(), ut.getUtterance());
			utteranceProcessor.processUtterance(newUt);
			newTrainUtterances.add(newUt);
		}
		corpusClassifier.setTrainUtterances(newTrainUtterances);
		//		utteranceProcessor.getModifications(); //just for flusing

		//		corpusClassifier = (CorpusClassifier) corpus;
		corpusClassifier.setAnswers(((CorpusClassifier) corpus).getAnswers());
		corpusClassifier.setAnswersMap(((CorpusClassifier) corpus).getAnswersMap());
		setNGramOrder();
		generateNGramCorpus();

	}

	@Override
	public String getDescription() {
		return "Abstract Distance Algorithm";
	}

	public CorpusClassifier getCorpus() {
		return corpusClassifier;
	}

	@Override
	public abstract QuestionClassifierEvaluator clone();
}
