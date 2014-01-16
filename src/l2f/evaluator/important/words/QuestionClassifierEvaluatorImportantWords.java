package l2f.evaluator.important.words;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import l2f.evaluator.distance.algorithms.tfidf.TfidfScore;
import l2f.interpretation.classification.features.FeatureSet;
import l2f.nlp.SimpleTokenizer;
import l2f.nlp.Tokenizer;
import l2f.tests.QCEBaseTester;
import l2f.tests.TesterInterface;

public class QuestionClassifierEvaluatorImportantWords implements QuestionClassifierEvaluator, Serializable{

	private CorpusClassifier corpus;
	private TesterInterface tester;
	private String desc= "";
	private UtteranceProcessor utteranceProcessor;
	private int nGramOrder;
	private int maxPredictions;
	private int maxImportantWords;
	private Tokenizer tokenizer = new SimpleTokenizer();
	private Map<String, Set<String>> catImportantWords = new HashMap<String, Set<String>>();
	public Map<String, List<DistanceUtterance>> catImportantWordsScores = new HashMap<String, List<DistanceUtterance>>();

	public QuestionClassifierEvaluatorImportantWords(CorpusClassifier corpus, int nGramOrder, int maxPredictions, int maxImportantWords) {
		this.corpus = corpus;
		this.utteranceProcessor = corpus.getUtteranceProcessor();
		this.nGramOrder = nGramOrder;
		this.desc = "ImportantWords " + getNgramType() + " Max iw- " + maxImportantWords + utteranceProcessor.getDescription();
		this.maxPredictions = maxPredictions;
		this.maxImportantWords = maxImportantWords;

		tester = new QCEBaseTester(this);
	}

	public QuestionClassifierEvaluatorImportantWords(UtteranceProcessor up, int nGramOrder, int maxPredictions, int maxImportantWords) {
		this.utteranceProcessor = up;
		this.corpus = new CorpusClassifier();
		this.nGramOrder = nGramOrder;
		this.desc = "ImportantWords " + getNgramType() + " Max iw- " + maxImportantWords + utteranceProcessor.getDescription();
		this.maxPredictions = maxPredictions;
		this.maxImportantWords = maxImportantWords;

		tester = new QCEBaseTester(this);
	}

	@Override
	public QuestionClassifierEvaluator clone() {
		return new QuestionClassifierEvaluatorImportantWords(this.utteranceProcessor, this.nGramOrder, this.maxPredictions, this.maxImportantWords);
	}

	@Override
	public void runClassification() {
		System.out.println("Training " + getDescription());
		Map<String, String> mergedDocsMap = new HashMap<String, String>();
		String mergedDoc;

		System.out.println("Merging Documents");
		for(Utterance ut : corpus.getTrainUtterances()){
			mergedDoc = mergedDocsMap.get(ut.getCat());
			if(mergedDoc == null){
				//we will add only one of the answer to the documents since the others are suposed to be similar (at least in the email domain)
				mergedDocsMap.put(ut.getCat(), ut.getUtterance());// + " " + corpus.getAnswer(ut.getCat()).get(0).getUtterance());
			}
			else{
				mergedDoc += " " + ut.getUtterance();
				mergedDocsMap.put(ut.getCat(), mergedDoc);
			}
		}

		Tokenizer tokenizer = new SimpleTokenizer();
		ArrayList<DistanceUtterance> documents = new ArrayList<DistanceUtterance>();
		System.out.println("Calculating important words candidates");
		for(String cat : mergedDocsMap.keySet()){
			mergedDoc = mergedDocsMap.get(cat);
			List<String> docList = NgramGenerator.getNGrams(nGramOrder, tokenizer.tokenize(mergedDoc));
			documents.add(new DistanceUtterance(docList, cat));
		}

//		TfidfScore tfidfScore = new TfidfScore(documents);
		TfidfScore tfidfScore;
		System.out.println("Calculating important words tfidf scores");
		double score;
		int index = 0;
		for(DistanceUtterance doc : documents){
			ArrayList<DistanceUtterance> otherDocs = filterDocuments(documents, index);
			index++;
			tfidfScore = new TfidfScore(otherDocs);
			System.out.println("tfidf " + index + "/" + documents.size());
			Map<String, Double> scores = tfidfScore.scoreSentence(doc.getNGramUtterance());
			List<DistanceUtterance> iwScores = new ArrayList<DistanceUtterance>();
			Set<String> iwSet = catImportantWords.get(doc.getCat());
			for(String iw : scores.keySet()){
				List<String> iwSingleList = new ArrayList<String>();
				iwSingleList.add(iw);
				score = scores.get(iw);
				if(score > 0)
					iwScores.add(new DistanceUtterance(iwSingleList, doc.getCat(), score));
			}
			Collections.sort(iwScores);
			if(iwScores.size() >= maxImportantWords)
				catImportantWordsScores.put(doc.getCat(), iwScores.subList(0, maxImportantWords));
			else
				catImportantWordsScores.put(doc.getCat(), iwScores);
		}

		String str = "";
		double totalSCore;
		for(String cat : catImportantWordsScores.keySet()){
			totalSCore = 0.0;
			str += cat + "\n";
			for(DistanceUtterance iw : catImportantWordsScores.get(cat)){
				str += iw.getNGramUtterance() + " " + iw.getScore() + "\n";
				totalSCore += iw.getScore();
			}
			str += "Total score: " + totalSCore + "\n\n";
		}
		try {
			BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("IWScores.txt"), "UTF-8"));
			br.write(str);
			br.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}

	private ArrayList<DistanceUtterance> filterDocuments(ArrayList<DistanceUtterance> documents, int indexTofilter) {
		int i = 0;
		ArrayList<DistanceUtterance> filteredDocs = new ArrayList<DistanceUtterance>();
		while(i < documents.size()){
			if(i == indexTofilter){
				i++;
				continue;
			}
			filteredDocs.add(new DistanceUtterance(documents.get(i).getNGramUtterance(), documents.get(i).getCat()));
			i++;
		}
		return filteredDocs;
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
		List<String> nGramQuestion = NgramGenerator.getNGrams(nGramOrder, tokenizer.tokenize(question));
		String modifications = utteranceProcessor.getModifications();
		double bestScore = 0.0;
		double currentScore = 0.0;
		int nPredictions = 0;
		List<String> predCats = new ArrayList<String>();
		List<DistanceUtterance> candidates = new ArrayList<DistanceUtterance>();
		double nIW;
		int iwDepth;
		List<String> foundIW;
		for(String cat : catImportantWordsScores.keySet()){
			nIW = 0.0;
			foundIW = new ArrayList<String>();
			List<DistanceUtterance> iwList = catImportantWordsScores.get(cat);
			iwDepth = iwList.size();
			for(DistanceUtterance iw : iwList){
				if(nGramQuestion.contains(iw.getNGramUtterance().get(0))){
					//					currentScore += iw.getScore();	
					foundIW.add(iw.getNGramUtterance().get(0));
					currentScore += iwDepth;
					nIW++;
				}
				iwDepth--;
			}

//			currentScore = currentScore / nIW;

			if(nPredictions < maxPredictions && !predCats.contains(cat)){
				candidates.add(new DistanceUtterance(foundIW, cat, currentScore));
				nPredictions++;
				predCats.add(cat);
				Collections.sort(candidates);
			}
			else{
				if(candidates.get(maxPredictions-1).getScore() < currentScore){
					predCats.remove(candidates.get(maxPredictions-1).getCat());
					predCats.add(cat);
					candidates.remove(maxPredictions-1);
					candidates.add(new DistanceUtterance(foundIW, cat, currentScore));
					Collections.sort(candidates);
				}
			}

			currentScore = 0.0;
		}
		ArrayList<Utterance> answers = new ArrayList<Utterance>();
		for(DistanceUtterance candidate : candidates){
			for(Utterance ut : corpus.getAnswer(candidate.getCat())){
				ut.setUtterance(ut.getUtterance());// + "\nMost similar: " + answer.getNGramUtterance() + "\nProcessed question: " + nGramQuestion);
				answers.add(ut);
			}
		}	
		return new QCEAnswer(answers, candidates.get(0).getNGramUtterance().toString(), getDescription(), bestScore);
	}

	@Override
	public QuestionEvaluatorSet getType() {
		return QuestionEvaluatorSet.IW;
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

	private String getNgramType() {
		if(nGramOrder == 1)
			return FeatureSet.UNIGRAM.getShortName();
		else if(nGramOrder == 2)
			return FeatureSet.BIGRAM.getShortName();
		else if(nGramOrder == 3)
			return FeatureSet.TRIGRAM.getShortName();
		return null;
	}

	private static final long serialVersionUID = -557660877550996804L;
}
