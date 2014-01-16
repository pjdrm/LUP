package l2f.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import l2f.corpus.CorpusClassifierReference;
import l2f.corpus.Utterance;
import l2f.corpus.parser.CorpusParser;
import l2f.corpus.parser.test.TestParserFactory;
import l2f.out.of.domain.OutOfDomainEvaluator;
import l2f.out.of.domain.OutOfDomainResult;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

public class OutOfDomainEvaluatorTester extends QCEBaseTester{

	private List<CorpusParser> testParsers = new ArrayList<CorpusParser>();
	private String testDirPath;
	private List<OutOfDomainEvaluator> oodEvaluatorList;
	private String corpusPropertiesPath;

	public OutOfDomainEvaluatorTester(String resultsDirPath, String domain, String testDirPath, List<OutOfDomainEvaluator> oodEvaluatorList, String corpusPropertiesPath) {
		this.resultsDirPath = resultsDirPath;
		this.corpusDomain = domain;
		this.testDirPath = testDirPath;
		this.oodEvaluatorList = oodEvaluatorList;
		this.corpusPropertiesPath = corpusPropertiesPath;
		testParsers = TestParserFactory.getTestParsers();
	}

	public void publishAllResults() {
		CorpusClassifierReference cc = null;
		CorpusClassifierReference currentCC = null;
		boolean parsedFile = false;
		File dirFile = new File(testDirPath);
		try {
			for(File file : dirFile.listFiles()){
				for(CorpusParser parser : testParsers){
					if(parser.canProcessCorpus(file.getCanonicalPath())){
						currentCC = (CorpusClassifierReference)parser.parseCorpus(file.getCanonicalPath(), corpusPropertiesPath);
//						currentCC.processCorpus();
						if(cc == null)
							cc = currentCC;
						else{
							cc.getOutDomainUtterances().addAll(currentCC.getOutDomainUtterances());
							cc.getInDomainUtterances().addAll(currentCC.getInDomainUtterances());
							cc.getContextUtterances().addAll(currentCC.getContextUtterances());
						}
						parsedFile = true;
						break;
					}
				}
				if(!parsedFile){
					System.out.println("ERROR: File " + file.getName() + " could not be parsed.");
					System.exit(1);
				}
				parsedFile = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		generateResultsBaseDir();
		String wrongAnswers = "Out of Domain Wrong Answers\n";
		double correctAnswers = 0.0;
		double oodTotal = cc.getOutDomainUtterances().size();
		double totalTruePositives = 0.0;
		double totalTrueNegatives = 0.0;
		double totalFalsePositives = 0.0;
		double totalFalseNegatives = 0.0;
		double idAccuracy;
		double odAccuracy;
		TestResult tr;
		List<TestResult> testResults = new ArrayList<TestResult>();
//		publishResult(resultsDirPath, cc.getCorpusStats(), "CorpusStats.txt");
		OutOfDomainResult oodResult;
		Map<String, List<Double>> distributions = new HashMap<String, List<Double>>();
		List<Double> currentDistribution;

		for(OutOfDomainEvaluator oodEval : oodEvaluatorList){
			System.out.println("Testing " + oodEval.getDescription());
			currentDistribution = new ArrayList<Double>();
			for(Utterance oodUt : cc.getOutDomainUtterances()){
				oodResult = oodEval.isOutOfDomain(oodUt.getUtterance());
				if(!oodResult.getIsOutOfDomain()){
					wrongAnswers += "Q: " + oodUt.getUtterance() + " SCORE " + oodResult.getScore() + " THRESHOLD " + oodResult.getThreshold() + "\n" + oodResult.getDebug() + "\n";
					totalFalseNegatives++;
					currentDistribution.add(0.0);
				}
				else{
					currentDistribution.add(1.0);
					correctAnswers++;
					totalTruePositives++;
				}
			}
			odAccuracy = correctAnswers / oodTotal;
//			allResults += "Out Of Domain-  Accuracy: " + odAccuracy + " Description: " + oodEval.getDescription() + "\n";
//			accuracyResults += "OODEval description: " + oodEval.getDescription() + " Out of Domain Accuracy: " +  correctAnswers / oodTotal;

			wrongAnswers += "\nIn Domain Wrong Answers\n";
			correctAnswers = 0.0;
//			List<Utterance> allInDomainUtterances = new ArrayList<Utterance>();
//			allInDomainUtterances.addAll(cc.getInDomainUtterances());
//			allInDomainUtterances.addAll(oodEval.getCorpus().getTestUtterances());
//			cc.addInDomainUtterances(oodEval.getCorpus().getTestUtterances());
			cc.generateCorpusStats();

			for(Utterance indUt : cc.getInDomainUtterances()){
				oodResult = oodEval.isOutOfDomain(indUt.getUtterance());
				if(oodResult.getIsOutOfDomain()){
					wrongAnswers += "Q: " + indUt.getUtterance() + " score " + oodResult.getScore() + " threshold " + oodResult.getThreshold() + "\n" + oodResult.getDebug() + "\n";
					totalFalsePositives++;
					currentDistribution.add(0.0);
				}
				else{
					currentDistribution.add(1.0);
					correctAnswers++;
					totalTrueNegatives++;
				}
			}
			idAccuracy = correctAnswers / cc.getInDomainUtterances().size();
			double precisionID = precision(totalTrueNegatives, totalFalseNegatives);
			double recallID = recall(totalTrueNegatives, totalFalsePositives);
			double f1ID = f1Score(totalTrueNegatives, totalTruePositives, totalFalseNegatives, totalFalsePositives);
			double precisionOOD = precision(totalTruePositives, totalFalsePositives);
			double recallOOD = recall(totalTruePositives, totalFalseNegatives);
			double f1OOD = f1Score(totalTruePositives, totalTrueNegatives, totalFalsePositives, totalFalseNegatives);
			
			tr = new TestResult(precisionID, recallID, f1ID, precisionOOD, recallOOD, f1OOD, oodEval.getDescription());
			distributions.put(oodEval.getDescription(), currentDistribution);
			testResults.add(tr);
//			allResults += "In Domain- Accuracy: " + idAccuracy + " Description: " + oodEval.getDescription() + "\n\n";
//			accuracyResults += "\nOODEval description: " + oodEval.getDescription() + " In Domain Accuracy: " +  correctAnswers / allInDomainUtterances.size();
			publishResult(resultsDirPath + "/" + oodEval.getDescription(), tr.toString(), "AccuracyResults.txt");
			publishResult(resultsDirPath + "/" + oodEval.getDescription(), wrongAnswers, "WrongClassificattions.txt");

			wrongAnswers = "Out of Domain Wrong Answers\n";
			correctAnswers = 0.0;
			totalTruePositives = 0.0;
			totalTrueNegatives = 0.0;
			totalFalsePositives = 0.0;
			totalFalseNegatives = 0.0;
			
		}
		Collections.sort(testResults);
		String sortedResults = "";
		for(TestResult tRes : testResults){
			sortedResults += tRes.toString() + "\n";
			
		}
		publishResult(resultsDirPath, sortedResults, "AllResults.txt");
		publishResult(resultsDirPath, cc.toString(), "Corpus.txt");
		publishResult(resultsDirPath, cc.getCorpusStats(), "CorpusStats.txt");
		
		List<String> keys = new ArrayList<String>(distributions.keySet());
		String fileTxt = "";
		List<Double> d1;
		List<Double> d2;
		int index1 = 0;
		int index2 = 0;
		WilcoxonSignedRankTest wc = new WilcoxonSignedRankTest();
		String wcTestResults = "";
		Double[] d1Array;
		Double[] d2Array;
		for(; index1 < keys.size() - 1; index1++){
			index2 = index1 + 1;
			for(;index2 < keys.size(); index2++){
				
				d1 = distributions.get(keys.get(index1));
				d2 = distributions.get(keys.get(index2));
				fileTxt += keys.get(index1) + ";" + keys.get(index2) + "\n";
				d1Array = new Double[d1.size()];
				d2Array = new Double[d1.size()];
				wcTestResults += wc.wilcoxonSignedRank(ArrayUtils.toPrimitive(d1.toArray(d1Array)), ArrayUtils.toPrimitive(d2.toArray(d2Array))) + " "+  keys.get(index1) + "#" + keys.get(index2) + "\n";
				for(int i = 0; i < d1.size(); i++){
					fileTxt += d1.get(i) + ";" + d2.get(i) + "\n";
				}
				publishResult(resultsDirPath + "/samples", fileTxt, keys.get(index1) + "#" + keys.get(index2) + ".csv");
				fileTxt = "";
				
				
			}
		}
		publishResult(resultsDirPath + "/samples", wcTestResults, "0WilcoxonSignedRankTest.txt");
		
	}

	public double f1Score(double tp, double  tn, double fp, double fn){
		double denominator = precision(tp, fp) + recall(tp, fn);
		if(denominator == 0.0)
			return 0.0;
		return 2 * precision(tp, fp) * recall(tp, fn) / denominator;
	}

	public double precision(double tp, double fp){
		if(tp + fp == 0.0)
			return 0.0;
		return tp / (tp + fp);
	}

	public double recall(double tp, double fn){
		if(tp + fn == 0.0)
			return 0.0;
		return tp / (tp + fn);
	}

	public void publishResult(String dirPath, String result, String fileName){
		File resultDir = new File(dirPath);
		if(!resultDir.exists())
			resultDir.mkdir();
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirPath + "/" + fileName), "UTF-8"));
			bw.write(result);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	private class TestResult implements Comparable<TestResult>{
		private double precisionOOD;
		private double recallOOD;
		private double f1ScoreOOD;
		private double precisionID;
		private double recallID;
		private double f1ScoreID;
		private String description;
		
		public TestResult(double precisionID, double recallID, double f1ScoreID, double precisionOOD, double recallOOD, double f1ScoreOOD, String description) {
			this.precisionOOD = precisionOOD;
			this.recallOOD = recallOOD;
			this.f1ScoreOOD = f1ScoreOOD;
			this.precisionID = precisionID;
			this.recallID = recallID;
			this.f1ScoreID = f1ScoreID;
			this.description = description;
		}
		
		@Override
		public String toString(){
			return "Description: " + description + " ID F1-Score: " + f1ScoreID + " ID Recall: " + recallID + " ID Precision: " + precisionID + " OOD F1-Score: " + f1ScoreOOD + " OOD Recall: " + recallOOD + " OOD Precision: " + precisionOOD;
		}

		@Override
		public int compareTo(TestResult tr) {
			double argScore = tr.f1ScoreOOD + + tr.precisionOOD + tr.recallOOD + tr.f1ScoreID + + tr.precisionID + tr.recallID;
			double myScore = f1ScoreOOD +  precisionOOD + recallOOD + f1ScoreID +  precisionID + recallID;
			if(argScore == myScore)
				return 0;
			else if(argScore > myScore)
				return 1;
			else 
				return -1;
		}
		
	}

	private static final long serialVersionUID = 1398869250686819093L;

}
