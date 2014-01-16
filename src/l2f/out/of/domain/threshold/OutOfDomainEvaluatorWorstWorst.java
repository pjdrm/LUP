package l2f.out.of.domain.threshold;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2f.corpus.Utterance;
import l2f.evaluator.QCEAnswer;
import l2f.evaluator.distance.algorithms.DistanceAlgorithm;
import l2f.evaluator.distance.algorithms.NgramGenerator;
import l2f.evaluator.distance.algorithms.QuestionClassifierEvaluatorDistance;
import l2f.nlp.SimpleTokenizer;
import l2f.nlp.Tokenizer;
import l2f.out.of.domain.OutOfDomainEvaluator;
import l2f.out.of.domain.OutOfDomainResult;

public class OutOfDomainEvaluatorWorstWorst extends OutOfDomainEvaluator{
	private Map<String, Double> thresholdMap = new HashMap<String, Double>();
	private Map<String, ArrayList<Utterance>> trainUtterancesMap = new HashMap<String, ArrayList<Utterance>>();
	private DistanceAlgorithm distanceAlgorithm;
	private int nGramOrder;
	private QuestionClassifierEvaluatorDistance qceDistance;
	private String scoresStr = "";

	public OutOfDomainEvaluatorWorstWorst(QuestionClassifierEvaluatorDistance qceDistance) {
		super(qceDistance.getCorpus());
		this.distanceAlgorithm = qceDistance.getDistanceAlgorithm();
		this.nGramOrder = qceDistance.getNgramOrder();
		this.qceDistance = qceDistance;
	}

	@Override
	public void run(){
		/*super.run();
		List<QCEScore> scores;
		for(String cat : scoresMap.keySet()){
			scores = scoresMap.get(cat);
			findThreshold(cat, scores);
		}

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("thresholdMap.txt")));
			for(String cat : thresholdMap.keySet()){
					bw.write(cat + " " + thresholdMap.get(cat) + "\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		generateTrainUtterancesMap(corpus.getTrainUtterances());
		for(String cat : trainUtterancesMap.keySet()){
			findThreshold(cat, trainUtterancesMap.get(cat));
		}

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("thresholdMaps\\thresholdMap" + getDescription() + ".txt")));
			for(String cat : thresholdMap.keySet()){
				bw.write(cat + " " + thresholdMap.get(cat) + "\n");
			}
			bw.write("\nBest Scores\n" + scoresStr);
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void findThreshold(String cat, ArrayList<Utterance> utterances) {
		System.out.println("Finding threshold for cat " + cat);
		Double lowestScore = 1.0;
		Double score;
		Double worstUtteranceScore = 1.0;
		Utterance currentUtterance;
		Tokenizer tokenizer = new SimpleTokenizer();
		for(int i = 0; i < utterances.size(); i++){
			currentUtterance = utterances.get(i);
			for(Utterance ut : utterances){
				if(ut.getUtterance().equals(currentUtterance.getUtterance()))
					continue;
				score = distanceAlgorithm.distance(NgramGenerator.getNGrams(nGramOrder, tokenizer.tokenize(ut.getUtterance())), NgramGenerator.getNGrams(nGramOrder, tokenizer.tokenize(currentUtterance.getUtterance())));
				if(score < worstUtteranceScore && score != 0.0)
					worstUtteranceScore = score;
				//				if(score < lowestScore && score != 0.0)
				//					lowestScore = score;
			}
			scoresStr += cat + " " + worstUtteranceScore + "\n";
			if(worstUtteranceScore < lowestScore && worstUtteranceScore != 0.0)
				lowestScore = worstUtteranceScore;
			worstUtteranceScore = 1.0;
		}
		scoresStr += "\n";
		thresholdMap.put(cat, lowestScore);

	}

	private void generateTrainUtterancesMap(List<Utterance> trainUtterances) {
		for(Utterance tu : trainUtterances){
			if(trainUtterancesMap.get(tu.getCat()) == null){
				trainUtterancesMap.put(tu.getCat(), new ArrayList<Utterance>());
			}
			trainUtterancesMap.get(tu.getCat()).add(tu);
		}

	}

	@Override
	public OutOfDomainResult isOutOfDomain(String strUtterance){
		strUtterance = getCorpus().getUtteranceProcessor().processString(strUtterance);
		boolean isOutOfDomain;
		QCEAnswer qceAnswer = qceDistance.answerWithQCEAnswer(strUtterance);
		String cat = qceAnswer.getPossibleAnswers().get(0).getCat();
		String debug = qceAnswer.getPossibleAnswers().get(0).getUtterance();
		debug = "CAT " + cat + " " + debug.substring(debug.indexOf('\n')+1);
		Double score = qceAnswer.getScore();
		Double threshold = thresholdMap.get(cat);
		//		System.out.println("Threshold: " + threshold + " Score: " + score);
		if(thresholdMap.get(cat) == null)
			isOutOfDomain = true;
		else if(score < thresholdMap.get(cat))
			isOutOfDomain = true;
		else
			isOutOfDomain = false;
		return new OutOfDomainResult(isOutOfDomain, score, threshold, debug);

	}

	@Override
	public String getDescription(){
		return "WorstWorst " + qceDistance.getDescription();
	}
}
