package l2f.evaluator.entropy;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import l2f.corpus.Corpus;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.Utterance;
import l2f.corpus.processor.UtteranceProcessor;
import l2f.evaluator.QCEAnswer;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.interpretation.classification.features.FeatureSet;
import l2f.tests.QCEBaseTester;
import l2f.tests.TesterInterface;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.JointClassifier;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.util.AbstractExternalizable;

public class QuestionClassifierEvaluatorCrossEntropy implements QuestionClassifierEvaluator, Serializable{

	private CorpusClassifier corpus;
	private TesterInterface tester;
	private String desc= "";
	private UtteranceProcessor utteranceProcessor;
	private int nGramOrder;
	private JointClassifier<CharSequence> crossEntropyCllassifier;
	private int maxPredictions;

	public QuestionClassifierEvaluatorCrossEntropy(CorpusClassifier corpus, int nGramOrder, int maxPredictions) {
		this.corpus = corpus;
		this.utteranceProcessor = corpus.getUtteranceProcessor();
		this.nGramOrder = nGramOrder;
		this.desc = "CrossEntropy " + getNgramType() + utteranceProcessor.getDescription();
		this.maxPredictions = maxPredictions;

		tester = new QCEBaseTester(this);
	}

	public QuestionClassifierEvaluatorCrossEntropy(UtteranceProcessor up, int nGramOrder, int maxPredictions) {
		this.utteranceProcessor = up;
		this.corpus = new CorpusClassifier();
		this.nGramOrder = nGramOrder;
		this.desc = "CrossEntropy " + getNgramType() + utteranceProcessor.getDescription();
		this.maxPredictions = maxPredictions;
		
		tester = new QCEBaseTester(this);
	}

	@Override
	public QuestionClassifierEvaluator clone() {
		return new QuestionClassifierEvaluatorCrossEntropy(this.utteranceProcessor, this.nGramOrder, this.maxPredictions);
	}

	@Override
	public void runClassification() {
		System.out.println("Training " + getDescription());
		Set<String> categories = this.corpus.getAnswersMap().keySet();
		DynamicLMClassifier<NGramBoundaryLM> classifier = DynamicLMClassifier.createNGramBoundary(categories.toArray(new String[categories.size()]), nGramOrder);
		for(Utterance trainUtt : this.corpus.getTrainUtterances()){
			Classification classification = new Classification(trainUtt.getCat());
			Classified<CharSequence> classified = new Classified<CharSequence>(trainUtt.getUtterance(),classification);
			classifier.handle(classified);
		}
		
		try {
			crossEntropyCllassifier = (JointClassifier<CharSequence>)AbstractExternalizable.compile(classifier);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
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
		JointClassification jc = crossEntropyCllassifier.classify(question);
		List<String> answerCandidates = new ArrayList<String>();
		
		for(int i = 0; i < maxPredictions; i++){
			answerCandidates.add(jc.category(i));
		}
		
		ArrayList<Utterance> possibleAnswers = new ArrayList<Utterance>();
		for(String candidate : answerCandidates){
			possibleAnswers.addAll(getCorpus().getAnswer(candidate));
		}
		return new QCEAnswer(possibleAnswers, modifications, getDescription(), jc.score(0));
	}

	@Override
	public QuestionEvaluatorSet getType() {
		return QuestionEvaluatorSet.CE;
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

	private static final long serialVersionUID = 4368750387903213514L;

}
