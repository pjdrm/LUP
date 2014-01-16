package l2f.tests.frames;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import l2f.corpus.CorpusClassifier;
import l2f.corpus.CorpusFrameClassifier;
import l2f.corpus.Utterance;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.frames.FrameQuestion;
import l2f.evaluator.frames.QuestionClassifierEvaluatorAF;
import l2f.evaluator.frames.QuestionClassifierEvaluatorFF;
import l2f.tests.TestResults;
import l2f.tests.TesterInterface;

public class FrameTester implements TesterInterface, Serializable{

	private QuestionClassifierEvaluator qce;
	private CorpusFrameClassifier cfc;
	private String qceDesc;
	private Double frameAccuracy = 0.0;
	private Double attributeAccuracy = 0.0;
	private String interactions = "";
	private String wrongFrames = "";
	private String wrongSlots = "";
	private int correctAnswers = 0;
	private TestResults results;
	private CorpusClassifier cc;

	@Override
	public void testClassifier() {
		Double correctFrames = 0.0;
		Double correctAttributes = 0.0;
		Double totalAttr = 0.0;
		Double correctAttr = 0.0;
		cfc = (CorpusFrameClassifier)qce.getCorpus();
		ArrayList<FrameQuestion> fq = new ArrayList<FrameQuestion>();
		fq.addAll(cfc.getTestFrameQuestions());
		fq.addAll(cfc.getTrainFrameQuestions());
		cc = toCorpusClassifier(fq);
		int totalTests = cfc.getTestFrameQuestions().size();
		int testIndex = 1;
		for(FrameQuestion fqTest : cfc.getTestFrameQuestions()){
			System.out.println("Test " + testIndex + "/" + totalTests + " " + qce.getDescription());
//			System.out.println(fqTest.getQuestion());
			testIndex++;
			String frameInstance = qce.answerWithQCEAnswer(fqTest.getQuestion()).getPossibleAnswers().get(0).getCat();
			interactions += "Utterance\n" + fqTest.getQuestion() + "\nFrame Instance\n" + frameInstance + "\n"; 

			totalAttr += fqTest.getSlotValues().size();
			correctAttr = checkCorrectAttributes(fqTest, frameInstance);
			correctAttributes += correctAttr;

			if(fqTest.getFrameCat().equals(getFrameID(frameInstance))){
				correctFrames++;
				if(correctAttr == fqTest.getSlotValues().size())
					correctAnswers++;
			}
			else{
				wrongFrames += "Utterance\n" + fqTest.getQuestion() + "\nCorrect Frame: " + fqTest.getFrameCat() + "\nReturned Frame: " + getFrameID(frameInstance) + "\n\n";
			}
		}

		frameAccuracy = correctFrames / (new Double(cfc.getTestFrameQuestions().size()));
		attributeAccuracy  = correctAttributes / totalAttr;

		results = new TestResults(correctAnswers, cfc.getTestFrameQuestions().size(), wrongFrames + "\n" + wrongSlots, interactions, qceDesc, frameAccuracy, attributeAccuracy);

	}

	private Double checkCorrectAttributes(FrameQuestion fq, String frameInstance) {
		Double correctAttr = 0.0;
		for(String sv : fq.getSlotValues()){
			if(frameInstance.contains(sv))
				correctAttr++;
		}

		if(fq.getSlotValues().size() != correctAttr){
			/*String v = "";
			for(String sv : fq.getSlotValues()){
				v += sv + " ";			
			}*/

			wrongSlots += "Utterance\n" + fq.getQuestion() + "\nCorrect Frame ID: " + fq.getFrameCat() + "\nCorrect slots:\n" + fq.getSlotValues().toString() + "\nReturned slots:\n" + frameInstance + "\n\n"; 
		}
		return correctAttr;
	}

	private String getFrameID(String frameInstance) {
		StringTokenizer strTokenizer = new StringTokenizer(frameInstance, "\n");
		String token;

		while(strTokenizer.hasMoreTokens()){
			token = strTokenizer.nextToken();
			if(token.contains("FrameID: "))
				return token.replaceAll("FrameID: ", "");
		}
		return null;
	}

	public FrameTester(QuestionClassifierEvaluatorFF qceFF){
		this.qce = qceFF;
		this.cfc = qceFF.getFrameCorpus();
		this.qceDesc = qceFF.getDescription();
	}

	public FrameTester(QuestionClassifierEvaluatorAF qceAF){
		this.qce = qceAF;
		this.cfc = qceAF.getFrameCorpus();
		this.qceDesc = qceAF.getDescription();
	}

	@Override
	public void publishResults(String dirPath) {
		try {
			BufferedWriter br;
			br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirPath + "/SystemResults.txt"), "UTF-8"));
			br.write(qceDesc + "\n\nFrame accuracy: " + frameAccuracy + "\n" + "Attribute accuracy: " + attributeAccuracy);
			br.close();

			br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirPath + "/Corpus.txt"), "UTF-8"));
			br.write(cfc.toString());
			br.close();	
			
			br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirPath + "/CorpusStats.txt"), "UTF-8"));
			br.write(cc.toString());
			br.close();	

			br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirPath + "/TestUtterances.txt"), "UTF-8"));
			br.write(interactions);
			br.close();

			br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirPath + "/WrongFrames.txt"), "UTF-8"));
			br.write(wrongFrames);
			br.close();

			br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirPath + "/WrongSlots.txt"), "UTF-8"));
			br.write(wrongSlots);
			br.close();

			// Writing corpus to disk so we can test again later
			cfc.resetToFrameCorpus();
			FileOutputStream f_out = new FileOutputStream(dirPath + "/serializedCorpus.corpus");
			ObjectOutputStream obj_out = new ObjectOutputStream (f_out);
			obj_out.writeObject(cfc);
			obj_out.close();

			resetTester();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void resetTester() {
		frameAccuracy = 0.0;
		attributeAccuracy = 0.0;
		interactions = "";
		wrongFrames = "";
		wrongSlots = "";
		correctAnswers = 0;
	}

	@Override
	public TestResults getResultsAcc() {
		return results;
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

	private static final long serialVersionUID = 5870639884258657685L;

	@Override
	public TestResults getResultsMRR() {
		// TODO Auto-generated method stub
		return null;
	}
}
