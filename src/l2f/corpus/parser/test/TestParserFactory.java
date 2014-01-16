package l2f.corpus.parser.test;

import java.util.ArrayList;
import java.util.List;

import l2f.corpus.parser.CorpusParser;
import l2f.corpus.parser.qa.QAParser;

public class TestParserFactory {

	private static List<CorpusParser> testParsers = new ArrayList<CorpusParser>();

	public static List<CorpusParser> getTestParsers(){
		//testParsers.add(new ReferenceTestParser());
		//testParsers.add(new MultipleChoiceTestParser());
		testParsers.add(new QuizTestParser());
		//testParsers.add(new QAParser());
		return testParsers;
	}
}
