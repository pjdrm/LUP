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

public class OutOfDomainEvaluatorAverage extends OutOfDomainEvaluator{

	private Map<String, Double> thresholdMap = new HashMap<String, Double>();
	private Map<String, ArrayList<Utterance>> trainUtterancesMap = new HashMap<String, ArrayList<Utterance>>();
	private DistanceAlgorithm distanceAlgorithm;
	private int nGramOrder;
	private QuestionClassifierEvaluatorDistance qceDistance;
	private String scoresStr = "";

	public OutOfDomainEvaluatorAverage(QuestionClassifierEvaluatorDistance qceDistance) {
		super(qceDistance.getCorpus());
		this.distanceAlgorithm = qceDistance.getDistanceAlgorithm();
		this.nGramOrder = qceDistance.getNgramOrder();
		this.qceDistance = qceDistance;
	}

	@Override
	public void run(){
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
		Double total = 0.0;
		Double nComparisons = 0.0;
		Utterance currentUtterance;
		Tokenizer tokenizer = new SimpleTokenizer();
		int j;
		for(int i = 0; i < utterances.size(); i++){
			j = i;
			currentUtterance = utterances.get(i);
			for( ; j < utterances.size(); j++){
				if(i == j)
					continue;
				total += distanceAlgorithm.distance(NgramGenerator.getNGrams(nGramOrder, tokenizer.tokenize(utterances.get(j).getUtterance())), NgramGenerator.getNGrams(nGramOrder, tokenizer.tokenize(currentUtterance.getUtterance())));
				nComparisons++;
			}
		}
		scoresStr += "\n";
		thresholdMap.put(cat, total / nComparisons);

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
		return "AveragePerCategory " + qceDistance.getDescription();
	}
}
