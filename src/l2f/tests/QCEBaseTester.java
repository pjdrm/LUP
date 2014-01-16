package l2f.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import l2f.corpus.CorpusClassifier;
import l2f.corpus.Utterance;
import l2f.evaluator.QCEAnswer;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.distance.algorithms.DistanceUtterance;
import l2f.evaluator.important.words.QuestionClassifierEvaluatorImportantWords;

public class QCEBaseTester implements TesterInterface, Serializable{

//	private QuestionClassifierEvaluatorImportantWords qce;
	private QuestionClassifierEvaluator qce;
	//Currently no being used
	private String systemResults = "";
	//Currently no being used
	private String rightAnswersResults = "";
	//Currently no being used
	private String wrongAnswersResults = "";
	private String wrongAnswersFile = "";
	//Currently no being used
	private String qaResults = "";
	private CorpusClassifier corpus;
	private String qceDesc = "";
	private TestResults resultsAcc;
	private TestResults resultsMRR;
	protected String resultsDirPath = "";
	protected String corpusDomain = "";
	
	public QCEBaseTester(){
		
	}
	
	public QCEBaseTester(QuestionClassifierEvaluator qce){
//		this.qce = (QuestionClassifierEvaluatorImportantWords)qce;
		this.qce = qce;
		this.qceDesc = qce.getDescription();
	}

	public void generateResultsBaseDir(){
		File systemResultsDir = new File(resultsDirPath);
		int i = systemResultsDir.list().length;
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy");
		String dateNow = formatter.format(currentDate.getTime());
		resultsDirPath = resultsDirPath + "/" + corpusDomain  + i + "_" + dateNow;
		File newTrainDir = new File(resultsDirPath);
		newTrainDir.mkdir();
	}

	@Override
	public void testClassifier() {
		
		/*Map<String, Integer> wrongCatountMap = new HashMap<String, Integer>();
		corpus = (CorpusClassifier)qce.getCorpus();
		Double correctAnswers = new Double(corpus.getTestUtterances().size());
		int testIdex = 1;
		int testSize = corpus.getTestUtterances().size();
		String qceDes = qce.getDescription();
		boolean hasRightAnswer;
		double mrr = 0.0;
		double rank;
		for(Utterance testUtt : corpus.getTestUtterances()){
			System.out.println("Test " + testIdex + "/" + testSize + " " + qceDes);
			testIdex++;
//			qaResults += "Q: " + testUtt.getUtterance() + "\n";
			QCEAnswer qceAnswer = qce.answerWithQCEAnswer(testUtt.getUtterance());
//			qaResults += "A: " + qceAnswer.getPossibleAnswers().get(0).getUtterance() + " " + qceAnswer.getRecognisedNE() + "\n";
//			qaResults += "\n";

			Double classifierScore = qceAnswer.getScore();
			hasRightAnswer = false;
			rank = 0.0;
			for(Utterance candidateAnswer : qceAnswer.getPossibleAnswers()){
				rank++;
				if(candidateAnswer.getCat().equals(testUtt.getCat())){
					//rightAnswersResults += testUtt.getUtterance() + ": " + candidateAnswer.getCat() + " " + classifierScore + "\n";
					hasRightAnswer = true;
					mrr += 1.0 / rank;
					break;
				}
			}
			
			if(!hasRightAnswer){
				String answerCat = qceAnswer.getPossibleAnswers().get(0).getCat();
				if(wrongCatountMap.get(testUtt.getCat()) == null)
					wrongCatountMap.put(testUtt.getCat(), 1);
				else{
					wrongCatountMap.put(testUtt.getCat(), wrongCatountMap.get(testUtt.getCat()) + 1);
				}
					
				wrongAnswersFile += "Test instance ID: " + testIdex + " Question: " + testUtt.getUtterance() + "\nAnswer: " + corpus.getAnswer(testUtt.getCat()).get(0).getUtterance() + "\nPredicted Cat: " + answerCat + " Correct Cat: " + testUtt.getCat();// + "_" + str;
				wrongAnswersFile += "\nQuestions in train with same PREDICTED category\n" + getSameCatQuestions(corpus, answerCat);
				wrongAnswersFile += "\nQuestions in train with same CORRECT category\n" + getSameCatQuestions(corpus, testUtt.getCat()) + "\n--------------------------------\n";
				correctAnswers--;
				//wrongAnswersResults += testUtt.getUtterance() + ": " + answerCat + " " + classifierScore + "\n";
			}
		}
		Double numberOfQuestions = new Double(corpus.getTestUtterances().size());
		//systemResults += "Accuracy: " + correctAnswers / numberOfQuestions + "\n\n";
		wrongAnswersFile += "\nWrong cats count\n";
		for(String cat : wrongCatountMap.keySet())
			wrongAnswersFile += cat + " " + wrongCatountMap.get(cat) +"\n";
		resultsAcc = new TestResults(correctAnswers, numberOfQuestions, wrongAnswersFile, qaResults, qceDesc);
		resultsMRR = new TestResults(mrr, numberOfQuestions, wrongAnswersFile, "", qceDesc);*/
		
	}

	@Override
	public void publishResults(String dirPath) {
		try {
			/*BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirPath + "/SystemResults.txt"), "UTF-8"));
			br.write(systemResults);
			br.close();*/

			/*br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirPath + "/IndividualClassifications.txt"), "UTF-8"));
			br.write("Wrong Answers\n" + wrongAnswersResults + "\nRight Answers\n" + rightAnswersResults);
			br.close();*/

			/*br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirPath + "/WrongAnswers.txt"), "UTF-8"));
			br.write(wrongAnswersFile);
			br.close();*/

			/*br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirPath + "/QA_Test.txt"), "UTF-8"));
			br.write(qaResults);
			br.close();*/

			/*br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirPath + "/Corpus.txt"), "UTF-8"));
			br.write(corpus.toString());
			br.close();*/

			// Writing corpus to disk so we can test again later
			/*FileOutputStream f_out = new FileOutputStream(dirPath + "/serializedCorpus.corpus");
			ObjectOutputStream obj_out = new ObjectOutputStream (f_out);
			obj_out.writeObject(corpus);
			obj_out.close();*/

			BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirPath + "/WrongAnswers.txt"), "UTF-8"));
			Map<String, Integer> wrongCatountMap = new HashMap<String, Integer>();
			corpus = (CorpusClassifier)qce.getCorpus();
			Double correctAnswers = new Double(corpus.getTestUtterances().size());
			int testIdex = 1;
			int testSize = corpus.getTestUtterances().size();
			String qceDes = qce.getDescription();
			boolean hasRightAnswer;
			double mrr = 0.0;
			double rank;
			for(Utterance testUtt : corpus.getTestUtterances()){
				System.out.println("Test " + testIdex + "/" + testSize + " " + qceDes);
				testIdex++;
//				qaResults += "Q: " + testUtt.getUtterance() + "\n";
				QCEAnswer qceAnswer = qce.answerWithQCEAnswer(testUtt.getUtterance());
//				qaResults += "A: " + qceAnswer.getPossibleAnswers().get(0).getUtterance() + " " + qceAnswer.getRecognisedNE() + "\n";
//				qaResults += "\n";

				Double classifierScore = qceAnswer.getScore();
				hasRightAnswer = false;
				rank = 0.0;
				for(Utterance candidateAnswer : qceAnswer.getPossibleAnswers()){
					rank++;
					if(candidateAnswer.getCat().equals(testUtt.getCat())){
						//rightAnswersResults += testUtt.getUtterance() + ": " + candidateAnswer.getCat() + " " + classifierScore + "\n";
						hasRightAnswer = true;
						mrr += 1.0 / rank;
						break;
					}
				}
				
				if(!hasRightAnswer){
					String answerCat = qceAnswer.getPossibleAnswers().get(0).getCat();
					/*String str = "";
					for(DistanceUtterance iw : qce.catImportantWordsScores.get(testUtt.getCat())){
						str += iw.getNGramUtterance();
					}*/
					if(wrongCatountMap.get(testUtt.getCat()) == null)
						wrongCatountMap.put(testUtt.getCat(), 1);
					else{
						wrongCatountMap.put(testUtt.getCat(), wrongCatountMap.get(testUtt.getCat()) + 1);
					}
						
					br.write("Test instance ID: " + testIdex + " Question: " + testUtt.getUtterance() + "\nAnswer: " + corpus.getAnswer(testUtt.getCat()).get(0).getUtterance() + "\nPredicted Cat: " + answerCat + " Correct Cat: " + testUtt.getCat());// + "_" + str;
					br.write("\nQuestions in train with same PREDICTED category\n" + getSameCatQuestions(corpus, answerCat));
					br.write("\nQuestions in train with same CORRECT category\n" + getSameCatQuestions(corpus, testUtt.getCat()) + "\n--------------------------------\n");
					correctAnswers--;
					//wrongAnswersResults += testUtt.getUtterance() + ": " + answerCat + " " + classifierScore + "\n";
				}
				
				/*String answerCat = qceAnswer.getPossibleAnswers().get(0).getCat();
				String result = testUtt.getUtterance() + ": " + answerCat + " " + classifierScore + "\n";
				if(answerCat.equals(testUtt.getCat())){
					rightAnswersResults += result;
				}
				else{
					wrongAnswersFile += "Question: " + testUtt.getUtterance() + "\nPredicted Cat: " + answerCat + " Correct Cat: " + testUtt.getCat();
					wrongAnswersFile += "\nQuestions in train with same PREDICTED category\n" + getSameCatQuestions(corpus, answerCat);
					wrongAnswersFile += "\nQuestions in train with same CORRECT category\n" + getSameCatQuestions(corpus, testUtt.getCat()) + "\n--------------------------------\n";
					correctAnswers--;
					wrongAnswersResults += result;
				}*/

			}
			Double numberOfQuestions = new Double(corpus.getTestUtterances().size());
			//systemResults += "Accuracy: " + correctAnswers / numberOfQuestions + "\n\n";
			br.write("\nWrong cats count\n");
			for(String cat : wrongCatountMap.keySet())
				br.write(cat + " " + wrongCatountMap.get(cat) +"\n");
			resultsAcc = new TestResults(correctAnswers, numberOfQuestions, wrongAnswersFile, qaResults, qceDesc);
			resultsMRR = new TestResults(mrr, numberOfQuestions, wrongAnswersFile, "", qceDesc);
			
			br.close();
			
			br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirPath + "/Corpus.txt"), "UTF-8"));
			br.write(corpus.toString());
			br.close();
			
			resetTester();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public TestResults getResultsAcc() {
		return resultsAcc;
	}
	
	@Override
	public TestResults getResultsMRR() {
		return resultsMRR;
	}
	
	private String getSameCatQuestions(CorpusClassifier corpus, String cat) {
		String result = "";
		for(Utterance ut : corpus.getSameCatUt(cat)){
			result += ut.getUtterance() + "\n";
		}
		return result;
	}
	
	private void resetTester() {
		systemResults = "";
		rightAnswersResults = "";
		wrongAnswersResults = "";
		wrongAnswersFile = "";
		qaResults = "";
	}
	
	private static final long serialVersionUID = -8289295357047024794L;
}
