package l2f.evaluator.vsm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import l2f.corpus.Corpus;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.Utterance;
import l2f.corpus.processor.UtteranceProcessor;
import l2f.evaluator.QCEAnswer;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.evaluator.distance.algorithms.DistanceUtterance;
import l2f.evaluator.distance.algorithms.NgramGenerator;
import l2f.nlp.Tokenizer;
import l2f.nlp.word.frequency.SimpleWordFrequencyCounter;
import l2f.nlp.word.frequency.TFIDFWordFrequencyCounter;
import l2f.nlp.word.frequency.WordFrequencyCounter;
import l2f.tests.QCEBaseTester;
import l2f.tests.TesterInterface;

public class QuestionClassifierEvaluatorVectorSpaceModel implements QuestionClassifierEvaluator, Serializable{

	private CorpusClassifier corpus;
	protected TesterInterface tester;
	protected String desc= "";
	protected UtteranceProcessor utteranceProcessor;
	private int maxPredictions;
	private int k_neighbours;
	private Tokenizer tokenizer;
	private int ngramOrder;
	private double uttScoreWeight;
	private Set<String> termUtteranceSet = new LinkedHashSet<String>();
	private Set<String> termAnswerSet = new LinkedHashSet<String>();
	private List<List<String>> tokenizedUtterances;
	private List<List<String>> tokenizedAnswers;
	private List<VectorUtterance> vectorUtterances = new ArrayList<VectorUtterance>();
	private WordFrequencyCounter wordFreqCounterUtt;
	private WordFrequencyCounter wordFreqCounterAnswers;
	String wordFrequencyCounterType;

	public QuestionClassifierEvaluatorVectorSpaceModel(CorpusClassifier corpus, 
			int ngramOrder,
			int maxPredictions,
			int k_neighbours,
			Tokenizer tokenizer,
			double uttScoreWeight,
			String wordFrequencyCounterType) {
		
		this.corpus = corpus;
		this.utteranceProcessor = corpus.getUtteranceProcessor();
		this.maxPredictions = maxPredictions;
		this.k_neighbours = k_neighbours;
		this.tokenizer = tokenizer;
		this.ngramOrder = ngramOrder;
		this.uttScoreWeight = uttScoreWeight;
		this.desc = "VSM " + getNgramName() + " FrequencyCounter " + wordFrequencyCounterType + " uttScoreWeight " +  uttScoreWeight+ " K-Neighbor " + k_neighbours + " "+
				utteranceProcessor.getDescription();
		this.wordFrequencyCounterType = wordFrequencyCounterType;

		tester = new QCEBaseTester(this);
	}

	public QuestionClassifierEvaluatorVectorSpaceModel(UtteranceProcessor up, 
			int ngramOrder, 
			int maxPredictions, 
			int k_neighbours,
			Tokenizer tokenizer,
			double uttScoreWeight,
			String wordFrequencyCounterType) {
		this.utteranceProcessor = up;
		this.corpus = new CorpusClassifier();
		this.maxPredictions = maxPredictions;
		this.k_neighbours = k_neighbours;
		this.tokenizer = tokenizer;
		this.uttScoreWeight = uttScoreWeight;
		this.ngramOrder = ngramOrder;
		this.desc = "VSM " + getNgramName() + " FrequencyCounter " + wordFrequencyCounterType + " uttScoreWeight " +  uttScoreWeight+ " K-Neighbor " + k_neighbours + " "+
				utteranceProcessor.getDescription();
		this.wordFrequencyCounterType = wordFrequencyCounterType;

		tester = new QCEBaseTester(this);
	}

	@Override
	public QuestionClassifierEvaluator clone() {
		return new QuestionClassifierEvaluatorVectorSpaceModel(this.utteranceProcessor, 
				this.ngramOrder, 
				this.maxPredictions, 
				this.k_neighbours,
				this.tokenizer,
				this.uttScoreWeight,
				this.wordFrequencyCounterType);
	}

	private WordFrequencyCounter initFreqCounter(String wordFrequencyCounterType, List<List<String>> tokenizedDocs) {
		if(wordFrequencyCounterType.equals("SimpleWordFrequencyCounter"))
			return new SimpleWordFrequencyCounter();
		else if(wordFrequencyCounterType.equals("TFIDFWordFrequencyCounter"))
			return new TFIDFWordFrequencyCounter(tokenizedDocs);
		System.err.println("Unknown Frequency Counter " + wordFrequencyCounterType);
		System.exit(1);
		return null;
	}

	protected String getNgramName() {
		if(ngramOrder == 1)
			return "-u-";
		else if(ngramOrder == 2)
			return "-b-";
		else if(ngramOrder == 3)
			return "-t-";
		else
			return "-n-";
	}

	@Override
	public void runClassification() {
		System.out.println("Training " + getDescription());
		initTokinezedDocs(corpus.getTrainUtterances());
		this.wordFreqCounterUtt = initFreqCounter(wordFrequencyCounterType, tokenizedUtterances);
		this.wordFreqCounterAnswers = initFreqCounter(wordFrequencyCounterType, tokenizedAnswers);
		vectorUtterances = getVetorRepresentations(corpus.getTrainUtterances());
	}

	private void initTokinezedDocs(List<Utterance> utterances) {
		tokenizedUtterances = new ArrayList<List<String>>();
		tokenizedAnswers = new ArrayList<List<String>>();
		int i = 1;
		int nUtt = utterances.size();
		for(Utterance utt : utterances){
			System.out.println("Vector Representation calculation progress " + i + "/" + nUtt);
			i++;
			List<String> uttTerms = NgramGenerator.getNGrams(ngramOrder, tokenizer.tokenize(utt.getUtterance()));
			tokenizedUtterances.add(uttTerms);
			termUtteranceSet.addAll(uttTerms);

			List<String> answerTerms = NgramGenerator.getNGrams(ngramOrder, tokenizer.tokenize(corpus.getAnswer(utt.getCat()).get(0).getUtterance()));
			tokenizedAnswers.add(answerTerms);
			termAnswerSet.addAll(answerTerms);
		}
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
		double currentScore;
		List<String> nGramQuestion = NgramGenerator.getNGrams(ngramOrder, tokenizer.tokenize(question));
		List<VectorUtterance> candidates = new ArrayList<VectorUtterance>();
		List<VectorUtterance> topK_neibours = new ArrayList<VectorUtterance>();
		List<String> predCats = new ArrayList<String>();
		int nPredictions = 0;
		double[] inputUttVector = wordFreqCounterUtt.getFrequencyValues(nGramQuestion, termUtteranceSet);
		double[] inputAnswerVector = wordFreqCounterAnswers.getFrequencyValues(nGramQuestion, termAnswerSet);
		int nNeighbours = 0;
		//k-neighbor version
		for(VectorUtterance vecUt :	vectorUtterances){
			currentScore = cosineSimilarity(inputUttVector, vecUt.getUtteranceVectorRepresentation())*uttScoreWeight + (1-uttScoreWeight)*cosineSimilarity(inputAnswerVector, vecUt.getAnswerVectorRepresentation());
			vecUt.setScore(currentScore);
			vecUt.setOriginalScore(currentScore);

			if(nNeighbours < k_neighbours){
				topK_neibours.add(vecUt);
				nNeighbours++;
				Collections.sort(topK_neibours);
			}
			else{
				if(topK_neibours.get(nNeighbours-1).getScore() < vecUt.getScore()){
					topK_neibours.remove(k_neighbours-1);
					topK_neibours.add(vecUt);
					Collections.sort(topK_neibours);
				}
			}
		}
		
		Map<String, VectorUtterance> neighboursCountsMap = new HashMap<String, VectorUtterance>();
		List<VectorUtterance> catCountList = new ArrayList<VectorUtterance>();
		for(VectorUtterance vu : topK_neibours){
			if(neighboursCountsMap.get(vu.getUtterance().getCat()) == null){
				//Going to lose distance score! Should use getOriginalScore method if want this value
				vu.setScore(1.0);
				catCountList.add(vu);
				neighboursCountsMap.put(vu.getUtterance().getCat(), vu);
			}
			else{
				VectorUtterance vutt = neighboursCountsMap.get(vu.getUtterance().getCat());
				vutt.setScore(vutt.getScore()+1.0);
				vutt.highestScore(vu.getOriginalScore());
			}
		}
		Collections.sort(catCountList);
		while(nPredictions < maxPredictions && nPredictions < catCountList.size()){
			candidates.add(catCountList.get(nPredictions));
			nPredictions++;
		}
		
		//old 1-neighbor version
		/*for(VectorUtterance vecUt :	vectorUtterances){
			currentScore = cosineSimilarity(inputUttVector, vecUt.getUtteranceVectorRepresentation())*uttScoreWeight + (1-uttScoreWeight)*cosineSimilarity(inputAnswerVector, vecUt.getAnswerVectorRepresentation());
			vecUt.setScore(currentScore);
			if(predCats.contains(vecUt.getUtterance().getCat())){
				//skip
			}
			else{
				if(nPredictions < maxPredictions && !predCats.contains(vecUt.getUtterance().getCat())){
					candidates.add(vecUt);
					nPredictions++;
					predCats.add(vecUt.getUtterance().getCat());
					Collections.sort(candidates);
				}
				else{
					if(!predCats.contains(vecUt.getUtterance().getCat()) && candidates.get(maxPredictions-1).getScore() < vecUt.getScore()){
						predCats.remove(candidates.get(maxPredictions-1).getUtterance().getCat());
						predCats.add(vecUt.getUtterance().getCat());
						candidates.remove(maxPredictions-1);
						candidates.add(vecUt);
						Collections.sort(candidates);
					}
				}
			}
		}*/

		ArrayList<Utterance> answers = new ArrayList<Utterance>();
		for(VectorUtterance candidate : candidates){
			for(Utterance ut : corpus.getAnswer(candidate.getUtterance().getCat())){
				ut.setUtterance(ut.getUtterance());// + "\nMost similar: " + answer.getNGramUtterance() + "\nProcessed question: " + nGramQuestion);
				answers.add(ut);
			}
		}	
		return new QCEAnswer(answers, modifications, getDescription(), candidates.get(0).getScore());
	}

	@Override
	public QuestionEvaluatorSet getType() {
		return QuestionEvaluatorSet.VSM;
	}

	@Override
	public void setCorpus(Corpus corpus) {
		List<Utterance> newTestUtterances = new ArrayList<Utterance>();
		Utterance newUt;
		for(Utterance ut : corpus.getTestUtterances()){
			newUt = new Utterance(ut.getCat(), ut.getUtterance());
			//			utteranceProcessor.processUtterance(newUt);
			newTestUtterances.add(newUt);
		}
		this.corpus.setTestUtterances(newTestUtterances);

		List<Utterance> newTrainUtterances = new ArrayList<Utterance>();
		for(Utterance ut : corpus.getTrainUtterances()){
			newUt = new Utterance(ut.getCat(), ut.getUtterance());
			utteranceProcessor.processUtterance(newUt);
			newTrainUtterances.add(newUt);
		}
		this.corpus.setTrainUtterances(newTrainUtterances);

		//		this.corpus = (CorpusClassifier)corpus;
		this.corpus.setAnswers(((CorpusClassifier) corpus).getAnswers());
		this.corpus.setAnswersMap(((CorpusClassifier) corpus).getAnswersMap());
	}

	@Override
	public String getDescription() {
		return desc;
	}

	@Override
	public CorpusClassifier getCorpus() {
		return corpus;
	}

	private List<VectorUtterance> getVetorRepresentations(List<Utterance> utterances) {
		List<VectorUtterance> vecUtt = new ArrayList<VectorUtterance>();
		int trainUttIndex = 0;
		double[] termFreqMatrixUtt;
		double[] termFreqMatrixAnswer;
		for(Utterance utt : utterances){
			//			System.out.println(trainUttIndex + " " + tokenizedUtterances.get(trainUttIndex) + " ");
			termFreqMatrixUtt = wordFreqCounterUtt.getFrequencyValues(tokenizedUtterances.get(trainUttIndex), termUtteranceSet);
			termFreqMatrixAnswer = wordFreqCounterAnswers.getFrequencyValues(tokenizedAnswers.get(trainUttIndex), termAnswerSet);
			vecUtt.add(new VectorUtterance(utt, termFreqMatrixUtt, termFreqMatrixAnswer));
			trainUttIndex++;
		}
		return vecUtt;
	}

	double cosineSimilarity(double[] xs, double[] ys) {
		double product = 0.0;
		double xsLengthSquared = 0.0;
		double ysLengthSquared = 0.0;
		for (int k = 0; k < xs.length; ++k) {
			xsLengthSquared += xs[k] * xs[k];
			ysLengthSquared += ys[k] * ys[k];
			product += xs[k] *  ys[k];
		}
		return product / Math.sqrt(xsLengthSquared * ysLengthSquared);
	}

	private static final long serialVersionUID = -2120007989256900238L;
}
