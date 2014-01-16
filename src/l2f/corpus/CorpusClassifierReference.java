package l2f.corpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;


public class CorpusClassifierReference extends CorpusClassifier{

	private ArrayList<Utterance> qOutDomain;
	private ArrayList<Utterance> qContext;
	private ArrayList<Utterance> qInDomain;
	
	public CorpusClassifierReference(ArrayList<Utterance> qInDomain, ArrayList<Utterance> qOutDomain, ArrayList<Utterance> qContext, ArrayList<Utterance> answers){
//		super(corpusProperties);
		this.testUtterances = qInDomain;
		this.qInDomain = qInDomain;
		this.qOutDomain = qOutDomain;
		this.qContext = qContext;
		setAnswers(answers);
		createAnswersMap();
	}
	
	public ArrayList<Utterance> getOutDomainUtterances(){
		return qOutDomain;
	}
	
	public ArrayList<Utterance> getInDomainUtterances(){
		return qInDomain;
	}
	
	public void addInDomainUtterances(List<Utterance> idUtterances){
		getInDomainUtterances().addAll(idUtterances);
	}
	
	public ArrayList<Utterance> getContextUtterances(){
		return qContext;
	}
	
	@Override
	public void processCorpus(){
		super.processCorpus();
		for(Utterance ut : qOutDomain)
			processUtterance(ut);
		
		for(Utterance ut : qContext)
			processUtterance(ut);
	}
	
	@Override
	public void generateCorpusStats(){
		super.generateCorpusStats();
		String stats = "In domain\n" + getCorpusStats() + "\n\nOut of domain\n";
		List<Utterance> allUtterances = new ArrayList<Utterance>();
		allUtterances.addAll(getInDomainUtterances());
		allUtterances.addAll(getOutDomainUtterances());
		allUtterances.addAll(getContextUtterances());
		
		stats += generateStats(getOutDomainUtterances()) + "\n\nContext\n" + generateStats(getContextUtterances()) + "\n\nTotal\n" + generateStats(allUtterances) + "\n\n";
		setCorpusStats(stats);
	}
	
	public String generateStats(List<Utterance> allUtterances){
		HashMap<String, Integer> wordMap = new HashMap<String, Integer>();
		String word = "";
		int totalWords = 0;
		for(Utterance u : allUtterances){
			StringTokenizer strTokenizer = new StringTokenizer(u.getUtterance(), " ");
			totalWords += strTokenizer.countTokens();
			while(strTokenizer.hasMoreTokens()){
				word = strTokenizer.nextToken();
				if(wordMap.get(word) == null)
					wordMap.put(word, 1);
			}
		}
		return "Total Utterance: " + allUtterances.size() + "\nUnique words: " + wordMap.keySet().size() + "\nTotal words: " + totalWords;
	}
	
	@Override
	public String toString(){
		String str = "Out of Domain Utterances\n";
		for(Utterance ut : qOutDomain){
			str += ut.toString() + "\n";
		}
		
		str += "\nIn Domain Utterances\n";
		for(Utterance ut : qInDomain){
			str += ut.toString() + "\n";
		}
		
		str += "\nContext Utterances\n";
		for(Utterance ut : qContext){
			str += ut.toString() + "\n";
		}
		return str;
		
	}
	
	private static final long serialVersionUID = 7796854841506955860L;
}
