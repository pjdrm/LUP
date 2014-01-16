package l2f.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

import l2f.evaluator.QuestionClassifierEvaluator;

public class QCETester extends QCEBaseTester{

	private ArrayList<QuestionClassifierEvaluator> qceArray;
	
	public QCETester(String resultsDirPath, ArrayList<QuestionClassifierEvaluator> qceArray, String corpusDomain){
		this.resultsDirPath = resultsDirPath;
		this.qceArray = qceArray;
		this.corpusDomain = corpusDomain;
	}
	
	public void publishAllResults() {
		generateResultsBaseDir();
		ArrayList<TestResults> resultsAcc = new ArrayList<TestResults>();
		ArrayList<TestResults> resultsMRR = new ArrayList<TestResults>();
		TestResults resultAcc;
		TestResults resultMRR;
		
		for(QuestionClassifierEvaluator qce : qceArray){
			qce.getTester().testClassifier();
			String qceResultsDirPath = resultsDirPath + "/" + qce.getDescription();
			File qceResultsDir = new File(qceResultsDirPath);
			qceResultsDir.mkdir();
			qce.getTester().publishResults(qceResultsDirPath);
			resultAcc = qce.getTester().getResultsAcc();
			resultsAcc.add(resultAcc);
			resultMRR = qce.getTester().getResultsMRR();
			resultsMRR.add(resultMRR);
		}
		
		Collections.sort(resultsAcc);
		String orderedResultsAcc = "";
		for(TestResults tr : resultsAcc){
			orderedResultsAcc += "Description: " + tr.getQCEDescription() + " Accuracy: " + tr.getAccuracy() + "\n";
		}
		
		Collections.sort(resultsMRR);
		String orderedResultsMRR = "";
		for(TestResults tr : resultsMRR){
			orderedResultsMRR += "Description: " + tr.getQCEDescription() + " Accuracy: " + tr.getAccuracy() + "\n";
		}
		
		try {
			BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultsDirPath + "/AllResults.txt"), "UTF-8"));
			br.write(orderedResultsAcc);
			br.close();
			
			br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultsDirPath + "/AllResultsMRR.txt"), "UTF-8"));
			br.write(orderedResultsMRR);
			br.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static final long serialVersionUID = 6558387922773950213L;
}
