package l2f.evaluator.logistic.regression;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import l2f.corpus.Corpus;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.Utterance;
import l2f.corpus.processor.UtteranceProcessor;
import l2f.evaluator.QCEAnswer;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.tests.QCEBaseTester;
import l2f.tests.TesterInterface;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.FeatureExtractor;

public class QuestionClassifierEvaluatorLogisticRegression implements QuestionClassifierEvaluator, Serializable{

	private CorpusClassifier corpus;
	private TesterInterface tester;
	private String desc= "";
	private UtteranceProcessor utteranceProcessor;
	private LogisticRegressionClassifier<CharSequence> logisticRegressionCllassifier;
	private int maxPredictions;

	public QuestionClassifierEvaluatorLogisticRegression(CorpusClassifier corpus, int maxPredictions) {
		this.corpus = corpus;
		this.utteranceProcessor = corpus.getUtteranceProcessor();
		this.desc = "LogisticRegression " + utteranceProcessor.getDescription();
		this.maxPredictions = maxPredictions;

		tester = new QCEBaseTester(this);
	}
	
	public QuestionClassifierEvaluatorLogisticRegression(UtteranceProcessor up, int maxPredictions) {
		this.utteranceProcessor = up;
		this.corpus = new CorpusClassifier();
		this.desc = "LogisticRegression " + utteranceProcessor.getDescription();
		this.maxPredictions = maxPredictions;

		tester = new QCEBaseTester(this);
	}
	
	@Override
	public QuestionClassifierEvaluator clone() {
		return new QuestionClassifierEvaluatorLogisticRegression(this.utteranceProcessor, this.maxPredictions);
	}

	@Override
	public void runClassification() {
		System.out.println("Training " + getDescription());
				
		XValidatingObjectCorpus<Classified<CharSequence>> corpusLR
		= new XValidatingObjectCorpus<Classified<CharSequence>>(0);
		for(Utterance trainUtt : this.corpus.getTrainUtterances()){
			Classification c = new Classification(trainUtt.getCat());
			Classified<CharSequence> classified
			= new Classified<CharSequence>(trainUtt.getUtterance(),c);
			corpusLR.handle(classified);
		}

		TokenizerFactory tokenizerFactory
		// = new com.aliasi.tokenizer.NGramTokenizerFactory(3,5);
		= new RegExTokenizerFactory("\\p{L}+|\\d+"); // letter+ | digit+
		FeatureExtractor<CharSequence> featureExtractor
		= new TokenFeatureExtractor(tokenizerFactory);
		int minFeatureCount = 2;
		boolean addInterceptFeature = true;
		boolean noninformativeIntercept = true;
//		double priorVariance = 10.0;
		// RegressionPrior prior = RegressionPrior.elasticNet(0.10, 1.0, noninformativeIntercept);
		RegressionPrior prior = RegressionPrior.gaussian(1.0,noninformativeIntercept);

		// = RegressionPrior.noninformative();
		AnnealingSchedule annealingSchedule
		= AnnealingSchedule.exponential(0.00025,0.999);  // exp(0.00025,0.999) works OK
		double minImprovement = 0.000000001;
		int minEpochs = 100;
		int maxEpochs = 20000;

		int blockSize = corpusLR.size(); // reduces to conjugate gradient
		LogisticRegressionClassifier<CharSequence> hotStart = null;
		int rollingAvgSize = 10;
		ObjectHandler<LogisticRegressionClassifier<CharSequence>> classifierHandler	= null;
		PrintWriter progressWriter = new PrintWriter(System.out,true);
		Reporter reporter = Reporters.writer(progressWriter);
		
		try {
			logisticRegressionCllassifier = LogisticRegressionClassifier.<CharSequence>train(corpusLR,
			                                                   featureExtractor,
			                                                   minFeatureCount,
			                                                   addInterceptFeature,
			                                                   prior,
			                                                   blockSize,
			                                                   hotStart,
			                                                   annealingSchedule,
			                                                   minImprovement,
			                                                   rollingAvgSize,
			                                                   minEpochs,
			                                                   maxEpochs,
			                                                   classifierHandler,
			                                                   reporter);
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
		ConditionalClassification conditionalClassification = logisticRegressionCllassifier.classify(question);
		List<String> answerCandidates = new ArrayList<String>();
		
		for(int i = 0; i < maxPredictions && i < conditionalClassification.size(); i++){
			answerCandidates.add(conditionalClassification.category(i));
		}
		
		ArrayList<Utterance> possibleAnswers = new ArrayList<Utterance>();
		for(String candidate : answerCandidates){
			possibleAnswers.addAll(getCorpus().getAnswer(candidate));
		}
		return new QCEAnswer(possibleAnswers, modifications, getDescription(), conditionalClassification.score(0));
	}

	@Override
	public QuestionEvaluatorSet getType() {
		return QuestionEvaluatorSet.LR;
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
	
	private static final long serialVersionUID = 1665089406443584523L;

}
