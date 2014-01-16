package l2f.evaluator;

import java.util.ArrayList;

import l2f.corpus.Corpus;
import l2f.corpus.CorpusClassifier;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.tests.TesterInterface;

public interface QuestionClassifierEvaluator {
	public void runClassification();
	public TesterInterface getTester();
	public ArrayList<String> answerQuestion(String question);
	public QCEAnswer answerWithQCEAnswer(String question);
	public QuestionEvaluatorSet getType();
	public void setCorpus(Corpus corpus);
	public String getDescription();
	public Corpus getCorpus();
	public QuestionClassifierEvaluator clone();
}
