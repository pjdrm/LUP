package l2f.tests;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import l2f.corpus.CorpusClassifierReference;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.Utterance;
import l2f.corpus.parser.CorpusParser;
import l2f.corpus.parser.test.TestParserFactory;
import l2f.evaluator.QCEAnswer;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.nlp.SimpleTokenizer;
import l2f.nlp.Tokenizer;

public class QCEReferenceTester extends QCEBaseTester{

	private List<CorpusParser> testParsers = new ArrayList<CorpusParser>();
	private ArrayList<QuestionClassifierEvaluator> qceArray;
	private String testFilePath;
	private String corpusPropertiesPath;

	public QCEReferenceTester(String resultsDirPath, ArrayList<QuestionClassifierEvaluator> qceArray, String domain, String testFilePath, String corpusPropertiesPath) {
		this.resultsDirPath = resultsDirPath;
		this.qceArray = qceArray;
		this.corpusDomain = domain;
		this.testFilePath = testFilePath;
		this.corpusPropertiesPath = corpusPropertiesPath;
		testParsers = TestParserFactory.getTestParsers();
	}

	public void publishAllResults() {
		CorpusClassifier cc = null;
		boolean parsedFile = false;
		for(CorpusParser parser : testParsers){
			if(parser.canProcessCorpus(testFilePath)){
				cc = parser.parseCorpus(testFilePath, corpusPropertiesPath);
//				cc.processCorpus();
				parsedFile = true;
				break;
			}
		}

		if(!parsedFile){
			System.out.println("ERROR: File " + testFilePath + " could not be parsed.");
			System.exit(1);
		}

		QCEAnswer qceAnswer;
		generateResultsBaseDir();
		try {
			BufferedWriter bw;
			String qaTestCorrect = "";
			String qaTestWrong = "";
			String interaction;
			String allAccuracyResults = "";
			String acc;
			int testIndex = 1;
			boolean isRightAnswer;
			ArrayList<Utterance> sameCatUtterances;
			List<TestResult> allResultList = new ArrayList<TestResult>();
			double accuracyVal;
			List<Utterance> testUtterances = cc.getTrainUtterances();
			int totalTest = testUtterances.size();
			
			double correctAnswers = 0.0;
			for(QuestionClassifierEvaluator qce : qceArray){
				System.out.println("Testing " + qce.getDescription());
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultsDirPath + "/Test - " + qce.getDescription() + ".txt"), "UTF-8"));
				for(Utterance testUt : testUtterances){
					System.out.println(testIndex + "/" + totalTest);
					testIndex++;
					qceAnswer = qce.answerWithQCEAnswer(testUt.getUtterance());
					interaction = "Q: " + testUt.getUtterance() + "\nA: " + qceAnswer.getPossibleAnswers().get(0).getUtterance() + "\n";
					isRightAnswer = checkAnswer(qceAnswer, testUt, cc.getAnswersMap());
					if(isRightAnswer){
						correctAnswers++;
						qaTestCorrect  += interaction;
					}
					else{
						qaTestWrong += interaction + "Same cat utterances:\n";
						sameCatUtterances = ((CorpusClassifier)qce.getCorpus()).getSameCatUt(qceAnswer.getPossibleAnswers().get(0).getCat());
						for(Utterance ut : sameCatUtterances){
							qaTestWrong += "- " + ut.getUtterance() + "\n";
						}
						
					}
					
				}
				accuracyVal = correctAnswers / (double)testUtterances.size();
				allResultList.add(new TestResult(qce.getDescription(), accuracyVal));
				acc = "Accuracy: " + accuracyVal;
//				allAccuracyResults += qce.getDescription() + " " + acc + "\n";
				bw.write(acc + "\n\nWrong answers\n" + qaTestWrong + "\n\nCorrect answers\n" + qaTestCorrect);
				bw.close();
				correctAnswers = 0.0;
				qaTestCorrect = "";
				qaTestWrong = "";
				testIndex = 0;
			}
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultsDirPath + "/CorpusStats.txt"), "UTF-8"));
			cc.generateCorpusStats();
			/*List<Utterance> allUtterances = new ArrayList<Utterance>();
			allUtterances.addAll(cc.getInDomainUtterances());
			allUtterances.addAll(cc.getOutDomainUtterances());
			allUtterances.addAll(cc.getContextUtterances());
			int oovCount = outVocabularyCount(qceArray.get(0).getCorpus().getTrainUtterances(), allUtterances);*/
			int oovCount = outVocabularyCount(qceArray.get(0).getCorpus().getTrainUtterances(), testUtterances);
			
			bw.write("Out of Vocabulary: " + oovCount + "\n" + cc.getCorpusStats());
			bw.close();
			
			Collections.sort(allResultList);
			for(TestResult tr : allResultList){
				allAccuracyResults += tr.desc + " Accuracy: " + tr.accuracy + "\n";
			}
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultsDirPath + "/AllResults.txt"), "UTF-8"));
			bw.write(allAccuracyResults);
			bw.close();
		}catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private boolean checkAnswer(QCEAnswer qceAnswer, Utterance testUt, HashMap<String, ArrayList<String>> answersMap) {
		for(String possibleAnswers : answersMap.get(testUt.getCat())){
			for(Utterance returnAnswer : qceAnswer.getPossibleAnswers()){
				if(returnAnswer.getUtterance().equals(possibleAnswers))
					return true;
			}
		}
		return false;
	}
	
	private int outVocabularyCount(List<Utterance> l1, List<Utterance> l2){
		Tokenizer tokenizer = new SimpleTokenizer();
		List<String> words;
		List<String> oovWords = new ArrayList<String>();
		int oov = 0;
		boolean isOOV = true;
		for(Utterance ut2 : l2){
			words = tokenizer.tokenize(ut2.getUtterance());
			for(String word : words){
				for(Utterance ut1 : l1){
					if(ut1.getUtterance().contains(word)){
						isOOV = false;
						break;
					}
				}
				if(isOOV  && !oovWords.contains(word)){
					oov++;
					oovWords.add(word);
				}
				isOOV = true;
			}
		}
		return oov;
	}
	
	private class TestResult implements Comparable<TestResult>{
		private String desc;
		private double accuracy;
		
		private TestResult(String desc, double accuracy){
			this.desc = desc;
			this.accuracy = accuracy;
		}
		
		@Override
		public int compareTo(TestResult tr) {
			return (int) (tr.accuracy*10000 - accuracy*10000);
		}
	}
	
	private static final long serialVersionUID = 1898795935266352750L;
}
