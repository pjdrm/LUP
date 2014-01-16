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
import l2f.tests.TesterInterface;
import l2f.tests.frames.FrameTester;

public class QuestionClassifierEvaluatorFF implements Serializable, QuestionClassifierEvaluator{

	private HashMap<String, QuestionClassifierEvaluator> attributeClassifiers;
	private CorpusFrameClassifier frameCorpus;
	private QuestionClassifierEvaluator qceFrames;
	private TesterInterface tester;

	public QuestionClassifierEvaluatorFF(CorpusFrameClassifier frameCorpus, QuestionClassifierEvaluator qceFrames, HashMap<String, QuestionClassifierEvaluator> attributeClassifiers) {
		this.frameCorpus = frameCorpus;
		this.qceFrames = qceFrames;
		this.attributeClassifiers = attributeClassifiers;
		this.tester = new FrameTester(this);
	}
	
	@Override
	public QuestionClassifierEvaluator clone(){
		return new QuestionClassifierEvaluatorFF(this.frameCorpus, this.qceFrames, this.attributeClassifiers);
	}

	public CorpusFrameClassifier getFrameCorpus() {
		return frameCorpus;
	}

	public HashMap<String, QuestionClassifierEvaluator> getAttrClassifiers() {
		return attributeClassifiers;
	}

	public String getDescription() {
		return "FrameFirst " + qceFrames.getDescription();
	}

	public void runClassification(){

		//		long startTime = System.currentTimeMillis();

		CorpusClassifier corpusClassifier = toCorpusClassifier(frameCorpus.getTrainFrameQuestions());
		qceFrames.setCorpus(corpusClassifier);
		qceFrames.runClassification();

		QuestionClassifierEvaluator qceAttr;
		for(String attrKey :  frameCorpus.getAttributesMap().keySet()){
			FrameAttribute fa = frameCorpus.getAttributesMap().get(attrKey);
			corpusClassifier = categorizeQuestions(frameCorpus, fa);
			qceAttr = attributeClassifiers.get(fa.getName());
			qceAttr.setCorpus(corpusClassifier);
			qceAttr.runClassification();
		}

		//		long estimatedTime = System.currentTimeMillis() - startTime;
	}

	public QCEAnswer answerWithQCEAnswer(String question){
		if(question.equals("Liga a aparelhagem da sala.")){
			System.out.println("Ver aqui!");
		}
		QCEAnswer qceAnswer = qceFrames.answerWithQCEAnswer(question);
		Double score = qceAnswer.getScore();
		Frame correctFrame = getFrameFromCat(qceAnswer.getPossibleAnswers().get(0).getCat());

		if(correctFrame == null)
			System.out.println("LOL " + correctFrame);
		String frameInstance = "FrameID: " + correctFrame.getId() + "\n";
		String attrCat;
		AttributeValue av;
		for(FrameAttribute attr : correctFrame.getFramesAttributes()){
			qceAnswer = getAttrClassifiers().get(attr.getName()).answerWithQCEAnswer(question);
			attrCat = qceAnswer.getPossibleAnswers().get(0).getCat();
			av = getValueFromSlot(attrCat);
			frameInstance += attr.getName() + ": " + av.toString() + "\n";
		}

		ArrayList<Utterance> answers = new ArrayList<Utterance>();
		answers.add(new Utterance(frameInstance));
		return new QCEAnswer(answers , "", getDescription(), score);
	}

	private CorpusClassifier categorizeQuestions(CorpusFrameClassifier cfc, FrameAttribute attribute) {
		ArrayList<FrameQuestion> fqFilteredTrain = new ArrayList<FrameQuestion>();
		String attrCat = "";
		for(FrameQuestion fqTrain : cfc.getTrainFrameQuestions()){
			attrCat = attribute.getAttrValue(fqTrain);
//			if(attrCat.contains("CAT_UNKNOWN_")){
//				continue;
//			}
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


	private Frame getFrameFromCat(String frameCat) {
		String frameID = frameCat.replaceAll("CAT_", "");
		for(Frame frame : frameCorpus.getFrames()){
			if(frameID.equals(frame.getId()))
				return frame;
		}
		return null;
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

	@Override
	public TesterInterface getTester() {
		return tester;
	}

	@Override
	public ArrayList<String> answerQuestion(String question) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QuestionEvaluatorSet getType() {
		return QuestionEvaluatorSet.FRAMESFF;
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
