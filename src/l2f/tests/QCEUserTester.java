package l2f.tests;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import l2f.corpus.CorpusClassifier;
import l2f.corpus.Utterance;
import l2f.corpus.parser.CorpusParser;
import l2f.corpus.parser.test.UserTestParser;
import l2f.evaluator.QCEAnswer;
import l2f.evaluator.QuestionClassifierEvaluator;

public class QCEUserTester extends QCEBaseTester{

	private ArrayList<CorpusParser> testParsers = new ArrayList<CorpusParser>();
	private ArrayList<QuestionClassifierEvaluator> qceArray;
	private String testFilePath;
	private String corpusPropertiesPath;

	public QCEUserTester(String resultsDirPath, ArrayList<QuestionClassifierEvaluator> qceArray, String domain, String testFilePath, String corpusPropertiesPath) {
		this.resultsDirPath = resultsDirPath;
		this.qceArray = qceArray;
		this.corpusDomain = domain;
		this.testFilePath = testFilePath;
		this.corpusPropertiesPath = corpusPropertiesPath;
		initializeParser();
	}

	private void initializeParser() {
		testParsers.add(new UserTestParser());

	}

	public void publishAllResults() {
		CorpusClassifier cc = null;
		boolean parsedFile = false;
		for(CorpusParser parser : testParsers){
			if(parser.canProcessCorpus(testFilePath)){
				cc = parser.parseCorpus(testFilePath, corpusPropertiesPath);
				parsedFile = true;
				break;
			}
		}

		if(!parsedFile){
			System.out.println("ERROR: File " + testFilePath + " could not be parsed.");
			System.exit(1);
		}

		QCEAnswer qceAnswer;
		int tIndex = 1;
		generateResultsBaseDir();
		try {
			BufferedWriter bw;
			for(QuestionClassifierEvaluator qce : qceArray){
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultsDirPath + "/Test - " + qce.getDescription() + ".txt"), "UTF-8"));
				for(Utterance testUt : cc.getTestUtterances()){
					if(testUt.getCat().contains("BT_")){
						testUt.setCat(testUt.getCat().replaceAll("BT_", ""));
						bw.write("Test " + tIndex + "\n");
						tIndex++;
					}
					bw.write(testUt.getCat() + " " + testUt.getUtterance() + "\nA: ");
					qceAnswer = qce.answerWithQCEAnswer(testUt.getUtterance() + "\n");
					bw.write(qceAnswer.getPossibleAnswers().get(0).getUtterance() + "\n");
				}
				bw.close();
				tIndex = 1;
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultsDirPath + "/CorpusStats.txt"), "UTF-8"));
				cc.generateCorpusStats();
				bw.write(cc.getCorpusStats().replaceAll("Categories:", "Number of questions:"));
				bw.close();
			}
		}catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private static final long serialVersionUID = 7858413662933486936L;

}
