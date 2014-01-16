package l2f.evaluator.frames;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import l2f.corpus.Corpus;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.CorpusFrameClassifier;
import l2f.corpus.Utterance;
import l2f.evaluator.QCEAnswer;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.evaluator.arguments.SVMArguments;
import l2f.evaluator.svm.QuestionClassifierEvaluatorSVM;
import l2f.tests.TesterInterface;
import l2f.tests.frames.FrameTester;

public class QuestionClassifierEvaluatorAF implements QuestionClassifierEvaluator, Serializable{

	private HashMap<String, QuestionClassifierEvaluator> attributeClassifiers = new HashMap<String, QuestionClassifierEvaluator>();
	private final Double scoreThreshold = 0.1; 

	private CorpusFrameClassifier frameCorpus;
	private SVMArguments args;
	private QuestionClassifierEvaluator qceFrames = null;
	private TesterInterface tester;
	

	public QuestionClassifierEvaluatorAF(CorpusFrameClassifier frameCorpus, QuestionClassifierEvaluator qceFrames, HashMap<String, QuestionClassifierEvaluator> attributeClassifiers) {
		this.frameCorpus = frameCorpus;
		this.qceFrames = qceFrames;
		this.attributeClassifiers = attributeClassifiers;
		this.tester = new FrameTester(this);
	}
	
	@Override
	public QuestionClassifierEvaluator clone(){
		return null;
	}

	public Double getScoreTreshold(){
		return scoreThreshold;
	}


	public CorpusFrameClassifier getFrameCorpus(){
		return frameCorpus;
	}

	public HashMap<String, QuestionClassifierEvaluator> getAttrClassifiers() {
		return attributeClassifiers;
	}

	public void runClassification(){

		//		long startTime = System.currentTimeMillis();

		CorpusClassifier corpusClassifier = toCorpusClassifier(frameCorpus.getTrainFrameQuestions());
		qceFrames.setCorpus(corpusClassifier);
		qceFrames.runClassification();

		for(String attrKey :  frameCorpus.getAttributesMap().keySet()){
			FrameAttribute fa = frameCorpus.getAttributesMap().get(attrKey);
			corpusClassifier = categorizeQuestions(frameCorpus, fa);
			QuestionClassifierEvaluator qceAttr = attributeClassifiers.get(fa.getName());
			qceAttr.setCorpus(corpusClassifier);
			qceAttr.runClassification();

		}

		//		long estimatedTime = System.currentTimeMillis() - startTime;
	}

	public QCEAnswer answerWithQCEAnswer(String question){
		if(question.equals("Liga a aparelhagem da sala.")){
			System.out.println("Ver aqui!");
		}
		Double totalScore = 0.0;
		Double score = 0.0;
		ArrayList<String> recCats = new ArrayList<String>();
		ArrayList<FrameClassificationScore> fqScores = new ArrayList<FrameClassificationScore>();
		String cat = null;

		for(Frame frame : frameCorpus.getFrames()){
			for(FrameAttribute fa : frame.getFramesAttributes()){

				QCEAnswer qceAnswer = getAttrClassifiers().get(fa.getName()).answerWithQCEAnswer(question);
				if(qceAnswer.getPossibleAnswers().size() >= 1)
					cat = qceAnswer.getPossibleAnswers().get(0).getCat();
				else{
					System.err.println("QCE_AF: Classifier did not return an answer");
					System.exit(1);
				};

				if(!cat.contains("CAT_UNKNOWN_")){
					score = qceAnswer.getScore();
					totalScore += score;
					recCats.add(cat);
				}
			}
			fqScores.add(applyScoreFunction(frame, totalScore, frame.getFramesAttributes().size(), recCats));
			recCats = new ArrayList<String>();
			totalScore = 0.0;
		}
		return createQCEAnswer(chooseFrame(fqScores), question);
	}

	private FrameClassificationScore chooseFrame(ArrayList<FrameClassificationScore> frameScores) {
		FrameClassificationScore bestFrameScore = frameScores.get(0);
		frameScores.remove(0);
		for(FrameClassificationScore fqs : frameScores){
			if(fqs.getScore() == 0){
				continue;
			}
			else if(Math.abs(fqs.getScore() - bestFrameScore.getScore()) <= getScoreTreshold()){
				if(fqs.getRecognizedCats() > bestFrameScore.getRecognizedCats()){
					bestFrameScore = fqs;
				}
				else if(fqs.getRecognizedCats() == bestFrameScore.getRecognizedCats() && fqs.getScore() > bestFrameScore.getScore()){
					bestFrameScore = fqs;
				}
			}
			else if(fqs.getScore() > bestFrameScore.getScore()){
				bestFrameScore = fqs;
			}
		}

		return bestFrameScore;
	}

	private QCEAnswer createQCEAnswer(FrameClassificationScore bestFrameScore, String inputUtterance) {
		
		String attrCat;
		AttributeValue av;
		QCEAnswer qceAnswer;
		ArrayList<Utterance> answers = new ArrayList<Utterance>();
		String frameInstance = "FrameID: " + bestFrameScore.getFrame().getId() + "\n";
		for(FrameAttribute fa : bestFrameScore.getFrame().getFramesAttributes()){
//			frameInstance += fa.getSlotValues(inputUtterance) + "\n";
			qceAnswer = getAttrClassifiers().get(fa.getName()).answerWithQCEAnswer(inputUtterance);
			attrCat = qceAnswer.getPossibleAnswers().get(0).getCat();
			av = getValueFromSlot(attrCat);
			frameInstance += fa.getName() + ": " + av.toString() + "\n";
		}

		answers.add(new Utterance(frameInstance));
		return new QCEAnswer(answers, "", getDescription(), bestFrameScore.getScore());
	}

	public String getDescription() {
		return "AttributeFirst " + qceFrames.getDescription();
	}

	private FrameClassificationScore applyScoreFunction(Frame frame, Double score, int nAttr, ArrayList<String> recCats) {
		return new FrameClassificationScore(frame, score / nAttr, nAttr, recCats);
	}

	public Double scoreQuestion(FrameAttribute fa, String question, QuestionClassifierEvaluatorSVM qceAttr, ArrayList<String> recCats){		

		String cat = null;
		QCEAnswer qceAnswer = qceAttr.answerWithQCEAnswer(question);
		if(qceAnswer.getPossibleAnswers().size() >= 1)
			cat =qceAnswer.getPossibleAnswers().get(0).getCat();
		else{
			System.err.println("QCE_FF: Classifier did not return an answer");
			System.exit(1);
		};

		if(!cat.contains("CAT_UNKNOWN_"))
			recCats.add(cat);
		return qceAnswer.getScore();
	}

	public static AttributeValue getValueFromSlot(String cat) {
		String values = cat.replaceAll("CAT_", "").replaceAll("UNKNOWN_", "");
		ArrayList<String> arrayValues = new ArrayList<String>();

		if(values.contains("_")){
			StringTokenizer strT = new StringTokenizer(values, "_");

			while(strT.hasMoreTokens())
				arrayValues.add(strT.nextToken().replaceAll("#", " "));
		}
		else{
			arrayValues.add(values);
		}

		return new AttributeValue(arrayValues);
	}

	private CorpusClassifier categorizeQuestions(CorpusFrameClassifier cfc, FrameAttribute attribute) {
		ArrayList<FrameQuestion> fqFilteredTrain = new ArrayList<FrameQuestion>();
		String attrCat = "";
		for(FrameQuestion fqTrain : cfc.getTrainFrameQuestions()){
			attrCat = attribute.getAttrValue(fqTrain);
			fqTrain.setQuestionCat(attrCat);
			fqFilteredTrain.add(fqTrain);
		}

		return toCorpusClassifier(fqFilteredTrain);
	}

	private CorpusClassifier toCorpusClassifier(ArrayList<FrameQuestion> fqArray) {
		ArrayList<Utterance> utTrainArray = new ArrayList<Utterance>();
		for(FrameQuestion fqTrain : fqArray){
			utTrainArray.add(new Utterance(fqTrain.getQuestionCat(), fqTrain.getQuestion()));
		}

		CorpusClassifier cc = new CorpusClassifier();
		ArrayList<Utterance> dummyTest = new ArrayList<Utterance>();
		dummyTest.add(new Utterance("DUMMY_CAT", "DUMMY_UTTERANCE"));
		cc.setTest(dummyTest);
		cc.setTrain(utTrainArray);
		return cc;
	}

	@Override
	public TesterInterface getTester() {
		return tester;
	}

	public SVMArguments getArgs() {
		return args;
	}

	@Override
	public ArrayList<String> answerQuestion(String question) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public QuestionEvaluatorSet getType() {
		return QuestionEvaluatorSet.FRAMESAF;
	}
	
	@Override
	public void setCorpus(Corpus corpus) {
		this.frameCorpus = (CorpusFrameClassifier)corpus;
	}
	
	private static final long serialVersionUID = 8513713832906947714L;

	@Override
	public Corpus getCorpus() {
		return frameCorpus;
	}
	
}
