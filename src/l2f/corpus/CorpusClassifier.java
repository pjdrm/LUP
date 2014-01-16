package l2f.corpus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import l2f.config.Config;
import l2f.nlp.SimpleSentenceSpliter;
import l2f.utils.collections.SortableMap;

public class CorpusClassifier extends Corpus implements Serializable{
	private static final long serialVersionUID = -7683084748894673805L;
	
	protected ArrayList<Utterance> answers = new ArrayList<Utterance>();
	protected ArrayList<String> iwl = new ArrayList<String>();
	protected ArrayList<String> ne = new ArrayList<String>();
	protected HashMap<String, ArrayList<String>> answersMap = new HashMap<String, ArrayList<String>>();
	protected String corpusStats = "";
	
	public CorpusClassifier(){
		super();
	}
	
	public CorpusClassifier(String domainPropertiesPath){
		super(domainPropertiesPath);
	}

	public CorpusClassifier(ArrayList<Utterance> questions, String domainPropertiesPath){
		super(domainPropertiesPath);
		List<List<Utterance>> trainTestCorpus = getTrainTestCorpus(Config.testPercentage, questions);
		setTrainUtterances(trainTestCorpus.get(0));
		setTestUtterances(trainTestCorpus.get(1));
		
		/*if(getTestUtterances().size() == 0)
			testUtterances.add(new Utterance("dummy"));*/
	}

	public CorpusClassifier(ArrayList<Utterance> questions, ArrayList<Utterance> answers, String domainPropertiesPath){
		this(questions, domainPropertiesPath);
		setAnswers(answers);
		createAnswersMap();
	}
	
	public ArrayList<Utterance> getAnswer(String cat){
		ArrayList<Utterance> possibleAnswers = new ArrayList<Utterance>();
		if(getAnswers().size() == 0){
			possibleAnswers.add(new Utterance(cat, cat));
		}
		else{
			for(String answer : answersMap.get(cat)){
				possibleAnswers.add(new Utterance(cat, answer));
			}
		}
		return possibleAnswers;
	}
	
	public ArrayList<String> getAnswersStringArray(String cat){
		ArrayList<String> possibleAnswers = new ArrayList<String>();
		if(getAnswers().size() == 0){
			possibleAnswers.add(cat);
		}
		else{
			for(String answer : answersMap.get(cat)){
				possibleAnswers.add(answer);
			}
		}
		return possibleAnswers;
	}
	
	public ArrayList<Utterance> getSameCatUt(String cat){
		ArrayList<Utterance> results = new ArrayList<Utterance>();
		for(Utterance ut : getTrainUtterances()){
			if(ut.getCat().equals(cat))
				results.add(ut);
		}
		return results;
	}
	
	public void createAnswersMap() {
		for(Utterance ut : getAnswers()){
			if(answersMap.get(ut.getCat()) == null){
				ArrayList<String> possibleAnswers = new ArrayList<String>();
				possibleAnswers.add(ut.getUtterance());
				answersMap.put(ut.getCat(), possibleAnswers);
			}
			else{
				answersMap.get(ut.getCat()).add(ut.getUtterance());
			}
		}
	}

	public CorpusClassifier(ArrayList<Utterance> trainQuestions, ArrayList<Utterance> testQuestions, ArrayList<Utterance> answers, ArrayList<String> iwl, String domainPropertiesPath){
		super(domainPropertiesPath);
		setTrainUtterances(trainQuestions);
		setTestUtterances(testQuestions);
		setAnswers(answers);
		setIWL(iwl);
		
		createAnswersMap();
	}
	
	public void setTrain(ArrayList<Utterance> train){
		this.trainUtterances = train;
	}

	public void setTest(ArrayList<Utterance> test){
		this.testUtterances = test;
	}

	public void setIWL(ArrayList<String> iwl){
		this.iwl = iwl;
	}
	
	public void setAnswers(ArrayList<Utterance> answers) {
		this.answers = answers;
	}

	public void setAnswersMap(HashMap<String, ArrayList<String>> answersMap){
		this.answersMap = answersMap;
	}
	
	public ArrayList<String> getIWL(){
		return iwl;
	}

	public ArrayList<Utterance> getAnswers() {
		return answers;
	}

	public HashMap<String, ArrayList<String>> getAnswersMap(){
		return answersMap;
	}
	
	public void setCorpusStats(String stats){
		corpusStats = stats;
	}
	
	public String getCorpusStats(){
		return corpusStats;
	}

	public List<List<Utterance>> getTrainTestCorpus(double testPercentage, List<Utterance> utterances){
		Map<String, List<Utterance>> utteranceMap = new HashMap<String, List<Utterance>>();
		List<Utterance> utteranceList;
		for(Utterance ut : utterances){
			if(utteranceMap.get(ut.getCat()) == null){
				utteranceList = new ArrayList<Utterance>();
				utteranceList.add(ut);
				utteranceMap.put(ut.getCat(), utteranceList);
			}
			else
				utteranceMap.get(ut.getCat()).add(ut);
		}
		
		List<List<Utterance>> trainTestCorpus = new ArrayList<List<Utterance>>();
		List<Utterance> testCorpus = new ArrayList<Utterance>();
		List<Utterance> trainCorpus = new ArrayList<Utterance>();
		List<Integer> testLines;
		List<Utterance> currentUtterances;
		int i;
		SimpleSentenceSpliter sentenceSpliter = new SimpleSentenceSpliter();
		
		for(String cat : utteranceMap.keySet()){
			i = 0;
			currentUtterances = utteranceMap.get(cat);
			testLines = getRandomTestLines((int)Math.round(currentUtterances.size()*testPercentage), currentUtterances.size());
			for(Utterance ut : currentUtterances){
				if(testLines.contains(i))
					testCorpus.add(ut);
				else{
					/*List<String> sentences = sentenceSpliter.split(ut.getUtterance());
					for(String sentence : sentences){
						trainCorpus.add(new Utterance(ut.getCat(), sentence));
					}*/
					trainCorpus.add(ut);
				}
				i++;
			}
		}
		
		trainTestCorpus.add(trainCorpus);
		trainTestCorpus.add(testCorpus);
		return trainTestCorpus;
	}
	
	public ArrayList<Integer> getRandomTestLines(int numberOfTestLines, int totalOfLines) {
		ArrayList<Integer> testLines = new ArrayList<Integer>();
		int lineCounter = 0;
		Integer lineNumber = 0;
		Random randomGenerator = new Random();

		while(lineCounter < numberOfTestLines){
			lineNumber = randomGenerator.nextInt(totalOfLines);
			if(!testLines.contains(lineNumber)){
				testLines.add(lineNumber);
				lineCounter++;
			}
		}
		return testLines;
	}
	
	public void generateCorpusStats(){
		ArrayList<Utterance> corpus = new ArrayList<Utterance>();
		corpus.addAll(getTestUtterances());
		corpus.addAll(getTrainUtterances());
		HashMap<String, Integer> wordUttMap = new HashMap<String, Integer>();
		HashMap<String, Integer> wordAnswerMap = new HashMap<String, Integer>();
		Map<String, Integer> catMap = new HashMap<String, Integer>();
		String word = "";
		int totalUttWords = 0;
		int totalAnswerWords = 0;
		int utCount;
		double uttLenght[] = new double[corpus.size()];
		int i = 0;
		for(Utterance u : corpus){
			StringTokenizer strUttTokenizer = new StringTokenizer(u.getUtterance(), " ");
			int nTokens = strUttTokenizer.countTokens();
			totalUttWords += nTokens;
			uttLenght[i] = nTokens;
			i++;
			while(strUttTokenizer.hasMoreTokens()){
				word = strUttTokenizer.nextToken();
				if(wordUttMap.get(word) == null)
					wordUttMap.put(word, 1);
			}
			
			StringTokenizer strAnswerTokenizer = new StringTokenizer(getAnswer(u.getCat()).get(0).getUtterance(), " ");
			totalAnswerWords += strAnswerTokenizer.countTokens();
			while(strAnswerTokenizer.hasMoreTokens()){
				word = strAnswerTokenizer.nextToken();
				if(wordAnswerMap.get(word) == null)
					wordAnswerMap.put(word, 1);
			}
			
			if(catMap.get(u.getCat()) == null)
				catMap.put(u.getCat(), 1);
			else{
				utCount = catMap.get(u.getCat());
				utCount++;
				catMap.put(u.getCat(), utCount);
			}
		}
		
		catMap = SortableMap.sort(catMap, SortableMap.ORDER.DESCENDING);
		String utteranceCount = "Utterances per Category\n";
		for(String cat : catMap.keySet()){
			utteranceCount += cat + " " + catMap.get(cat) + "\n";
		}
		
		DescriptiveStatistics stats = new DescriptiveStatistics(uttLenght);
		setCorpusStats("Categories: " + catMap.keySet().size() + "\nTotal Utterance: " + corpus.size() + "\nUnique Utterance words: " + wordUttMap.keySet().size() + "\nTotal Utterance words: " + totalUttWords + "\nSTD Words/Utterace: "+ stats.getStandardDeviation() + " " + stats.getMean() + "\nUnique Answer words: " + wordAnswerMap.keySet().size() + "\nTotal Answer words: " + totalAnswerWords + "\n\n" + utteranceCount);
	}
	
	public String toString(){
		generateCorpusStats();
		String result = "Stats:\n";
		result += getCorpusStats() + "\n\n";
		
		result += "Test:\n";
		for(Utterance tu : getTestUtterances()){
			result += tu.getCat() + " " + tu.getUtterance() + "\n";
		}
		
		result += "\nTrain:\n";
		for(Utterance tru : getTrainUtterances()){
			result += tru.getCat() + " " + tru.getUtterance() + "\n";
		}
		
		result += "\nAnswers\n";
		for(String cat : answersMap.keySet()){
			for(String a : answersMap.get(cat)){
				result += cat + " " + a + "\n";
			}
		}
		
		result += "\nIWL:\n";
		for(String iw : getIWL()){
			result += iw + "\n";
		}
		
		/*result += "\nNE:\n";
		for(String ne : getNE()){
			result += ne + "\n";
		}*/
		
		return result;
		
	}

	public void fullTrain() {
		getTrainUtterances().addAll(getTestUtterances());
	}
}
