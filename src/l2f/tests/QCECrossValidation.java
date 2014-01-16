package l2f.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import l2f.config.Config;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.CorpusFrameClassifier;
import l2f.corpus.factory.CorpusClassifierFactory;
import l2f.corpus.factory.CorpusFrameClassifierFactory;
import l2f.corpus.parser.CorpusFrameParser;
import l2f.corpus.parser.CorpusParser;
import l2f.corpus.parser.frames.FrameParser;
import l2f.corpus.parser.other.DefaultParser;
import l2f.corpus.parser.qa.QAParser;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.tests.t.QCETTest;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class QCECrossValidation {

	private ArrayList<QuestionClassifierEvaluator> qceArrayRaw;
	private int nPartitions;
	private HashMap<String, ArrayList<TestResults>> resultsAccMap = new HashMap<String, ArrayList<TestResults>>();
	private HashMap<String, ArrayList<TestResults>> resultsMRRMap = new HashMap<String, ArrayList<TestResults>>();
	private Map<String, String> timeMap = new HashMap<String, String>();
	private QCETTest tTest;
	
	public QCECrossValidation(ArrayList<QuestionClassifierEvaluator> qceRawArray, int nPartitions, double alpha) {
		this.qceArrayRaw = qceRawArray;
		this.nPartitions = nPartitions;
		this.tTest = new QCETTest(alpha);
	}
	
	public QCECrossValidation(ArrayList<QuestionClassifierEvaluator> qceRawArray, double alpha) {
		this.qceArrayRaw = qceRawArray;
		this.tTest = new QCETTest(alpha);
	}

	public void performCrossValidation(String corpusDirPath) {
		QuestionClassifierEvaluator qce = qceArrayRaw.get(0);
		TestResults resultAcc;
		TestResults resultMRR;
		int z = 1;
		String baseDir = generateResultsBaseDir(Config.resulsDir, corpusDirPath);
		String cvRuDir = baseDir + "/run";
		File cvResultsDirFile;
		
		if(qce.getType().equals(QuestionEvaluatorSet.FRAMESAF) || qce.getType().equals(QuestionEvaluatorSet.FRAMESFF)){
			ArrayList<CorpusFrameParser> frameParsers = new ArrayList<CorpusFrameParser>();
			frameParsers.add(new FrameParser());
			CorpusFrameClassifierFactory cfcFact = new CorpusFrameClassifierFactory(frameParsers);
			ArrayList<CorpusFrameClassifier> partitions = cfcFact.getCorpusFrameClassifierPartitions(Config.corpusDir + "/"+ corpusDirPath, nPartitions);
			for(QuestionClassifierEvaluator qcEval : qceArrayRaw){
				System.out.println("Cross Validating");
				for(CorpusFrameClassifier corpus : partitions){
					System.out.println("Run " + z);
					qcEval.setCorpus(corpus);
					qcEval.runClassification();
					qcEval.getTester().testClassifier();
					cvResultsDirFile = new File(cvRuDir + z);
					if(!cvResultsDirFile.isDirectory())
						cvResultsDirFile.mkdir();
					resultAcc = qcEval.getTester().getResultsAcc();
					resultMRR = qcEval.getTester().getResultsMRR();
					cvResultsDirFile = new File(cvRuDir + z + "/" + resultAcc.getQCEDescription());
					cvResultsDirFile.mkdir();
					qcEval.getTester().publishResults(cvRuDir + z + "/" + resultAcc.getQCEDescription());
					z++;
					if(resultsAccMap.get(resultAcc.getQCEDescription()) == null){
						ArrayList<TestResults> resultsAcc = new ArrayList<TestResults>();
						resultsAcc.add(resultAcc);
						resultsAccMap.put(resultAcc.getQCEDescription(), resultsAcc);
						
						/*ArrayList<TestResults> resultsMRR = new ArrayList<TestResults>();
						resultsMRR.add(resultMRR);
						resultsMRRMap.put(resultMRR.getQCEDescription(), resultsMRR);*/
					}
					else{
						resultsAccMap.get(resultAcc.getQCEDescription()).add(resultAcc);
//						resultsMRRMap.get(resultMRR.getQCEDescription()).add(resultMRR);
					}
				}
				z = 1;
			}
		}
		else{
			ArrayList<CorpusParser> parsers = new ArrayList<CorpusParser>();
			parsers.add(new QAParser());
			parsers.add(new DefaultParser());
			CorpusClassifierFactory ccFact = new CorpusClassifierFactory(parsers);
			
			//MAJOR HACK!
			Config.posTaggerFlag = false;
			Config.stopwordsFlag = false;
			Config.normalizeStringFlag = false;
			
			List<CorpusClassifier> partitions = ccFact.getCorpusClassifierPartitions(Config.corpusDir + "/"+ corpusDirPath, nPartitions);
			crossValidateCorpusClassifier(partitions, baseDir);
			/*int z = 1;
			String baseDir = generateResultsBaseDir(Config.resulsDir, corpusDirPath);
			String cvRuDir = baseDir + "/run";
			File cvResultsDirFile;*/
			
//			processResults(baseDir + "/CrossValidationResults.txt");
		}
		processResults(baseDir);
	}
	
	public void reCrossValidate(String cvDirPath, String corpusDomain){
		String[] strSplit = corpusDomain.split("/");
		String baseDir = generateResultsBaseDir(Config.resulsDir, "reCVof-" + strSplit[strSplit.length-1]+"-");
		List<CorpusClassifier> partitions = CorpusClassifierFactory.getCorpusClassifierPartitionsFromCVDir(cvDirPath, corpusDomain);
		crossValidateCorpusClassifier(partitions, baseDir);
		processResults(baseDir);
	}
	
	public void crossValidateCorpusClassifier(List<CorpusClassifier> partitions, String baseDir){
		String cvRuDir = baseDir + "/run";
		File cvResultsDirFile;
		TestResults resultAcc;
		TestResults resultMRR;
		int z = 1;
		long startTimeTraining;
		long durationTraining;
		long startTimeTesting;
		long durationTesting = 0;
		for(QuestionClassifierEvaluator qcEval : qceArrayRaw){
			System.out.println("Cross Validating");
			durationTraining = 0;
			for(CorpusClassifier corpus : partitions){
				System.out.println("Run " + z);
				System.out.println("Setting corpus");
				qcEval.setCorpus(corpus);
				System.out.println("Running classification");
				startTimeTraining = System.nanoTime();
				qcEval.runClassification();
				durationTraining += System.nanoTime() - startTimeTraining;
				startTimeTesting = System.nanoTime();
				qcEval.getTester().testClassifier();
				cvResultsDirFile = new File(cvRuDir + z);
				if(!cvResultsDirFile.isDirectory())
					cvResultsDirFile.mkdir();
				
				cvResultsDirFile = new File(cvRuDir + z + "/" + qcEval.getDescription());
				cvResultsDirFile.mkdir();
				qcEval.getTester().publishResults(cvRuDir + z + "/" + qcEval.getDescription());
				durationTesting += System.nanoTime() - startTimeTesting;
				z++;
				resultAcc = qcEval.getTester().getResultsAcc();
				resultMRR = qcEval.getTester().getResultsMRR();
				
				if(resultsAccMap.get(resultAcc.getQCEDescription()) == null){
					ArrayList<TestResults> resultsAcc = new ArrayList<TestResults>();
					resultsAcc.add(resultAcc);
					resultsAccMap.put(resultAcc.getQCEDescription(), resultsAcc);
					
					ArrayList<TestResults> resultsMRR = new ArrayList<TestResults>();
					resultsMRR.add(resultMRR);
					resultsMRRMap.put(resultMRR.getQCEDescription(), resultsMRR);
				}
				else{
					resultsAccMap.get(resultAcc.getQCEDescription()).add(resultAcc);
					resultsMRRMap.get(resultMRR.getQCEDescription()).add(resultMRR);
				}
			}
			timeMap.put(qcEval.getDescription(), "\tTraining Time (ms): " + TimeUnit.MILLISECONDS.convert(durationTraining, TimeUnit.NANOSECONDS) + "\tTest Time (ms): " + TimeUnit.MILLISECONDS.convert(durationTesting, TimeUnit.NANOSECONDS));
			z = 1;
		}
	}
	
	public String generateResultsBaseDir(String resultsDirPath, String corpusDomain){
		File systemResultsDir = new File(resultsDirPath);
		int i = systemResultsDir.list().length;
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy");
		String dateNow = formatter.format(currentDate.getTime());
		resultsDirPath = resultsDirPath + "/" + corpusDomain  + i + "_" + dateNow;
		File newTrainDir = new File(resultsDirPath);
		newTrainDir.mkdir();
		return resultsDirPath;
	}

	private void processResults(String resutsFilePath) {
		System.out.println("Processing results");
		DescriptiveStatistics stats;
		DescriptiveStatistics frameStats;
		DescriptiveStatistics attrStats;
		List<TestResults> trAcc;
		List<TestResults> trMRR;
		List<CVTestResult> cvAccResults = new ArrayList<CVTestResult>();
		List<CVTestResult> cvMRRResults = new ArrayList<CVTestResult>();
		String tTestResults = "";
		String cvAccResultStr = "";
		String cvMRRResultStr = "";
		Double accMean;
		Double mrrMean;
		
		for(String key : resultsAccMap.keySet()){
			tTestResults += ttest(key, resultsAccMap);
			trAcc = resultsAccMap.get(key);
			trMRR = resultsMRRMap.get(key);
			double[] valuesAcc = getSample(trAcc);
			stats = new DescriptiveStatistics(valuesAcc);
			accMean = stats.getMean();
			cvAccResultStr = "QCE: " + trAcc.get(0).getQCEDescription() + " Mean: " + accMean + " STD: " + stats.getStandardDeviation();
			
			double[] valuesMRR = getSample(trMRR);
			stats = new DescriptiveStatistics(valuesMRR);
			mrrMean = stats.getMean();
			cvMRRResultStr = "QCE: " + trMRR.get(0).getQCEDescription() + " Mean: " + mrrMean + " STD: " + stats.getStandardDeviation();
			
			
			if(trAcc.get(0).getFrameAccuracy() != null){
				double[] frameValues = new double[trAcc.size()];
				int vIndex = 0;
				for(TestResults res : trAcc){
					frameValues[vIndex] = res.getFrameAccuracy();
					vIndex++;
				}
				frameStats = new DescriptiveStatistics(frameValues);
				
				double[] attrValues = new double[trAcc.size()];
				vIndex = 0;
				for(TestResults res : trAcc){
					attrValues[vIndex] = res.getAttributeAccuracy();
					vIndex++;
				}
				attrStats = new DescriptiveStatistics(attrValues);
				cvAccResultStr += " Individual Frame accuracy: " + frameStats.getMean() + " +- " + frameStats.getStandardDeviation() + " Attributes accuracy: " + attrStats.getMean() + " +- " + attrStats.getStandardDeviation();
			}
			
			
			cvAccResults.add(new CVTestResult(cvAccResultStr, accMean)); //+ runs + "\n\n";
			cvMRRResults.add(new CVTestResult(cvMRRResultStr, mrrMean));
		}
		
		try {
			Collections.sort(cvAccResults);
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(resutsFilePath + "/CrossValidationResults.txt")));
			for(CVTestResult cvtr : cvAccResults){
				bw.write(cvtr.resultDesc + "\n");
			}
			bw.close();
			
			Collections.sort(cvMRRResults);
			bw = new BufferedWriter(new FileWriter(new File(resutsFilePath + "/CrossValidationResultsMRR.txt")));
			for(CVTestResult cvtr : cvMRRResults){
				bw.write(cvtr.resultDesc + "\n");
			}
			bw.close();
			
			bw = new BufferedWriter(new FileWriter(new File(resutsFilePath + "/TimeResults.txt")));
			for(String qceDesc : timeMap.keySet()){
				bw.write(qceDesc + " \t" +timeMap.get(qceDesc) + "\n");
			}
			bw.close();
			
			bw = new BufferedWriter(new FileWriter(new File(resutsFilePath + "/t-Test.txt")));
			bw.write(tTestResults);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private String ttest(String key, HashMap<String, ArrayList<TestResults>> resultsMap) {
		boolean passedCategory = false;
		double[] sample1 = getSample(resultsMap.get(key));
		double[] sample2;
		String result = "";
		boolean isStatisSignificant;
		for(String k : resultsMap.keySet()){
			if(passedCategory){
				sample2 = getSample(resultsMap.get(k));
				isStatisSignificant = tTest.tTest(sample1, sample2);
				result += key + " | " +  k + " | " + isStatisSignificant + "\n";
			}
			
			if(k.equals(key))
				passedCategory = true;
		}
		return result;
	}

	private double[] getSample(List<TestResults> tr) {
		double[] values = new double[tr.size()];
		int vIndex = 0;
		for(TestResults res : tr){
			values[vIndex] = res.getAccuracy();
			vIndex++;
		}
		return values;
	}
	
	private class CVTestResult implements Comparable<CVTestResult>{
		private double mean;
		private String resultDesc;

		private CVTestResult(String resultDesc, double mean){
			this.mean = mean;
			this.resultDesc = resultDesc;
		}
		
		@Override
		public int compareTo(CVTestResult tr) {
			return (int) (tr.mean*10000 - mean*10000);
		}
	}

}
