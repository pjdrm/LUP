package l2f.evaluator.lsa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import l2f.tests.QCEBaseTester;
import l2f.tests.TesterInterface;

import org.apache.commons.lang.ArrayUtils;

import com.aliasi.matrix.SvdMatrix;

public class QuestionClassifierEvaluatorLSADP implements QuestionClassifierEvaluator, Serializable{

	private CorpusClassifier corpus;
	protected TesterInterface tester;
	protected String desc= "";
	protected UtteranceProcessor utteranceProcessor;
	private int maxPredictions;
	private Map<String, Double[]> termVectorsMap = new HashMap<String,Double[]>();
	private Map<String, Double[]> documentVectorsMap = new HashMap<String,Double[]>();
	private int maxFactors;
	private double featureInit;
	private double initialLearningRate;
	private int annealingRate;
	private double regularization;
	private double minImprovement;
	private int minEpochs;
	private int maxEpochs;
	private Tokenizer tokenizer;
	private int ngramOrder;
	private Set<String> termSet = new LinkedHashSet<String>();
	private Set<String> docIds = new LinkedHashSet<String>();
	private double[] scales;
	private Map<String, String> docsMy;

	public QuestionClassifierEvaluatorLSADP(CorpusClassifier corpus, 
			int ngramOrder, 
			int maxPredictions, 
			Tokenizer tokenizer,
			int maxFactors,
			double featureInit,
			double initialLearningRate,
			int annealingRate,
			double regularization,
			double minImprovement,
			int minEpochs,
			int maxEpochs) {
		this.corpus = corpus;
		this.utteranceProcessor = corpus.getUtteranceProcessor();
		this.maxPredictions = maxPredictions;
		this.maxFactors = maxFactors;
		this.featureInit = featureInit;
		this.initialLearningRate = initialLearningRate;
		this.annealingRate = annealingRate;
		this.regularization = regularization;
		this.minImprovement = minImprovement;
		this.minEpochs = minEpochs;
		this.maxEpochs = maxEpochs;
		this.tokenizer = tokenizer;
		this.ngramOrder = ngramOrder;
		this.desc = "LSA dot product " + getNgramName() + 
				" Max fact- " + maxFactors +
				" Features init- " + featureInit +
				" Initial learning rate- " + initialLearningRate +
				" Annealing- "+ annealingRate +
				" Regularization- " + regularization +
				" Min improv- " + minImprovement +
				" Min epochs- " + minEpochs +
				" Max epochs- " +  maxEpochs +
				utteranceProcessor.getDescription();
		this.scales = new double[maxFactors];

		tester = new QCEBaseTester(this);
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

	public QuestionClassifierEvaluatorLSADP(UtteranceProcessor up, 
			int ngramOrder, 
			int maxPredictions, 
			Tokenizer tokenizer,
			int maxFactors,
			double featureInit,
			double initialLearningRate,
			int annealingRate,
			double regularization,
			double minImprovement,
			int minEpochs,
			int maxEpochs) {
		this.utteranceProcessor = up;
		this.corpus = new CorpusClassifier();
		this.maxPredictions = maxPredictions;
		this.maxFactors = maxFactors;
		this.featureInit = featureInit;
		this.initialLearningRate = initialLearningRate;
		this.annealingRate = annealingRate;
		this.regularization = regularization;
		this.minImprovement = minImprovement;
		this.minEpochs = minEpochs;
		this.maxEpochs = maxEpochs;
		this.tokenizer = tokenizer;
		this.ngramOrder = ngramOrder;
		this.desc = "LSA dot product " + getNgramName() + 
				" Max fact- " + maxFactors +
				" Features init- " + featureInit +
				" Initial learning rate- " + initialLearningRate +
				" Annealing- "+ annealingRate +
				" Regularization- " + regularization +
				" Min improv- " + minImprovement +
				" Min epochs- " + minEpochs +
				" Max epochs- " +  maxEpochs + 
				utteranceProcessor.getDescription();

		tester = new QCEBaseTester(this);
	}

	@Override
	public QuestionClassifierEvaluator clone() {
		return new QuestionClassifierEvaluatorLSADP(this.utteranceProcessor, 
				this.ngramOrder, 
				this.maxPredictions, 
				this.tokenizer,
				this.maxFactors,
				this.featureInit,
				this.initialLearningRate,
				this.annealingRate,
				this.regularization,
				this.minImprovement,
				this.minEpochs,
				this.maxEpochs);
	}

	@Override
	public void runClassification() {
		System.out.println("Training " + getDescription());
		double[][] tdMatrix = getTermDocumentMatrix();
		double featureInit = 0.01;
		double initialLearningRate = 0.005;
		int annealingRate = 1000;
		double regularization = 0.00;
		double minImprovement = 0.0000;
		int minEpochs = 10;
		int maxEpochs = 50000;

		SvdMatrix svdMatrix = SvdMatrix.svd(tdMatrix,
				maxFactors,
				featureInit,
				initialLearningRate,
				annealingRate,
				regularization,
				null,
				minImprovement,
				minEpochs,
				maxEpochs);
		scales = svdMatrix.singularValues();
		
		//For some unknown reason sometimes the number os latent dimensions is smaller then the number of factors.
		maxFactors = scales.length;

		int i = 0;
		double[][] termValues =svdMatrix.leftSingularVectors();
//		String tvStr = "";
		for(String term : termSet){
			/*tvStr += "(";
			for(int j = 0; j < scales.length; j++){
				tvStr += termValues[i][j] + ", ";
			}
			tvStr += ") " + term + "\n";*/
			termVectorsMap.put(term, ArrayUtils.toObject(termValues[i]));
			i++;
		}
		/*Writer out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("TermValues.txt"), "UTF-8"));
			out.write(tvStr);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		i = 0;
		double[][] docValues = svdMatrix.rightSingularVectors();
//		String dvStr = "";
		for(String docId : docIds){
			/*dvStr += "(";
			for(int j = 0; j < scales.length; j++){
				dvStr += docValues[i][j] + ", ";
			}
			dvStr += ") " + docId + "\n";*/
			documentVectorsMap.put(docId, ArrayUtils.toObject(docValues[i]));
			i++;
		}
		/*try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("DocValues.txt"), "UTF-8"));
			out.write(dvStr);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
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
		List<String> queryTerms = NgramGenerator.getNGrams(ngramOrder, tokenizer.tokenize(question));
		double[] queryVector = new double[maxFactors];
		Arrays.fill(queryVector,0.0);
		Double[] termValues;
		for(String term : queryTerms){
			termValues = termVectorsMap.get(term);
			if(termValues != null){
				for(int j = 0; j < scales.length; j++){
					queryVector[j] += termValues[j];
				}
			}
		}
		String scoresStr = "\nQ: " + question + "\nQuery vector: (";
		for(int j = 0; j < scales.length; j++){
			scoresStr += queryVector[j] + ", ";
		}
		scoresStr += ")\n";

		int nPredictions = 0;
		DistanceUtterance dUt;
		List<String> predCats = new ArrayList<String>();
		List<DistanceUtterance> candidates = new ArrayList<DistanceUtterance>();
		List<DistanceUtterance> allCandidates = new ArrayList<DistanceUtterance>();

		for (String docId : documentVectorsMap.keySet()) {
			double score = vectorCalc(queryVector, documentVectorsMap.get(docId),scales);
			dUt = new DistanceUtterance(queryTerms, docId, score);
//			allCandidates.add(dUt);
			if(predCats.contains(dUt.getCat())){
				//skip
			}
			else{
				if(nPredictions < maxPredictions){
					candidates.add(dUt);
					nPredictions++;
					predCats.add(dUt.getCat());
					Collections.sort(candidates);
				}
				else{
					if(candidates.get(maxPredictions-1).getScore() < dUt.getScore()){
						predCats.remove(candidates.get(maxPredictions-1).getCat());
						predCats.add(dUt.getCat());
						candidates.remove(maxPredictions-1);
						candidates.add(dUt);
						Collections.sort(candidates);
					}
				}
			}
		}

		/*Collections.sort(allCandidates);
		
		int i = 0;
		for(DistanceUtterance cand : allCandidates){
			scoresStr += cand.getScore() + " " + cand.getCat() + "\n";
			if(i == 4)
				break;
			i++;

		}
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Scores.txt", true), "UTF-8"));
			out.write(scoresStr);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		ArrayList<Utterance> answers = new ArrayList<Utterance>();
		String s = docsMy.get(candidates.get(0).getCat());
		for(DistanceUtterance candidate : candidates){
			for(Utterance ut : corpus.getAnswer(candidate.getCat())){
				ut.setUtterance(ut.getUtterance());
				answers.add(ut);
			}
		}	
		return new QCEAnswer(answers, modifications, getDescription(), candidates.get(0).getScore());
	}

	@Override
	public QuestionEvaluatorSet getType() {
		return QuestionEvaluatorSet.LSA;
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

	private double[][] getTermDocumentMatrix() {
		Map<String, String> docs = new LinkedHashMap<String,String>();
		for(Utterance utt : corpus.getTrainUtterances()){
			String cat = utt.getCat();
			if(docs.get(cat) == null){
				docs.put(cat, utt.getUtterance());
				docIds.add(cat);
			}
			else{
				docs.put(cat, docs.get(cat) + " " + utt.getUtterance());
			}
		}

		docsMy = docs;
		List<List<String>> tokenizedDocs = new ArrayList<List<String>>();
		for(String docId : docs.keySet()){
			List<String> docTerms = NgramGenerator.getNGrams(ngramOrder, tokenizer.tokenize(docs.get(docId)));
			tokenizedDocs.add(docTerms);
			termSet.addAll(docTerms);
		}
		double[][] tdMatrix = new double[termSet.size()][docs.keySet().size()];
		int termIndex = 0;
		int docIndex = 0;
		int termCount = 0;
		for(String term : termSet){
			for(List<String> docTokens : tokenizedDocs){
				for(String docToken : docTokens){
					if(docToken.equals(term))
						termCount++;
				}
				tdMatrix[termIndex][docIndex] = termCount;
				termCount = 0;
				docIndex++;
			}
			docIndex = 0;
			termIndex++;
		}

		return tdMatrix;
	}

	//calculates dot product of two vectors
	double vectorCalc(double[] xs, Double[] ys, double[] scales) {
		double sum = 0.0;
		for (int k = 0; k < xs.length; ++k)
			sum += xs[k] * ys[k] * scales[k];
		return sum;
	}

	private static final long serialVersionUID = -2744791456062459042L;
}
