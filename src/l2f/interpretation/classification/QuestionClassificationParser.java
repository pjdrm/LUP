package l2f.interpretation.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import l2f.config.Config;
import l2f.interpretation.AnalyzedQuestion;
import l2f.interpretation.InterpretedQuestion;
import l2f.interpretation.QuestionAnalyzer;
import l2f.nlp.NormalizerSimple;
import l2f.utils.Utils;

import org.xml.sax.InputSource;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Parser;

/**
 * The <code>QuestionClassificationParser</code> class parses a file of
 * question classification instances, in the following format:
 * 
 * <blockquote><pre>CATEGORY_SUBCATEGORY Word_1 Word_2 Word_N</pre></blockquote>
 */
public class QuestionClassificationParser
extends Parser<ObjectHandler<InterpretedQuestion>> implements Serializable{

	private HashMap<String, ArrayList<String>> namedEntities = new HashMap<String, ArrayList<String>>();

	/**
	 * Indicates whether or not to consider fine grained categories.
	 */
	private boolean finer;
	/**
	 * Category separator, when using coarse grained categories.
	 * Default: "_" (underscore)
	 */
	private String separator;
	/**
	 * Question analyzer used to convert question strings into
	 * an AnalyzedQuestion container.
	 */
	private QuestionAnalyzer questionAnalyzer;

	private boolean namedEntityRecogition;

	/**
	 * Construct a question classification parser.
	 * @param handler Classification handler for data.
	 */


	public QuestionClassificationParser(QuestionAnalyzer questionAnalyzer,
			ObjectHandler<InterpretedQuestion> handler,
			boolean useFineGrainedCategories, String separator, boolean namedEntityRecognition) {
		super(handler);
		this.finer = useFineGrainedCategories;
		this.separator = separator;
		this.questionAnalyzer = questionAnalyzer;
		this.namedEntityRecogition = namedEntityRecognition;
	}

	public QuestionClassificationParser(QuestionAnalyzer questionAnalyzer,
			boolean useFineGrainedCategories, String separator, boolean namedEntityRecognition) {
		this(questionAnalyzer, null, useFineGrainedCategories, separator, namedEntityRecognition);
	}

	public QuestionClassificationParser(QuestionAnalyzer questionAnalyzer,
			boolean useFineGrainedCategories, boolean namedEntityRecognition) {
		this(questionAnalyzer, null, useFineGrainedCategories, "_", namedEntityRecognition);
	}

	public QuestionClassificationParser() { 
		this.namedEntityRecogition = true;
	}

	/**
	 * Construct a question classification parser, using the default
	 * parameters: finer categories and empty separator.
	 */
	public QuestionClassificationParser(QuestionAnalyzer questionAnalyzer, Boolean namedEntityRecognition) {
		this(questionAnalyzer, true, Utils.EMPTY, namedEntityRecognition);
	}

	static String stripComment(String line) {
		int commentStart = line.indexOf('#');
		return commentStart < 0
		? line
				: line.substring(0, commentStart);
	}


	public void parse(InputSource is) throws IOException {
		try {
			String fileName = is.getSystemId().replaceFirst("file:", "");
			//avoiding hidden directories
			if(fileName.contains("/.")){
				return;
			}

			if(this.namedEntityRecogition){
				File f = new File(Config.named_entities_file);
				f.delete();
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));

			String line = "";
			int lineNumber = 0;
			while ((line = br.readLine()) != null) {
				if (line.matches("[A-Za-z0-9_]+ .*")) {    
					lineNumber++;
					if (lineNumber % 10 == 0) {
						System.out.println("Processing line " + lineNumber + ".");
					}

					line = stripComment(line);
					int questionStart = line.indexOf(' ');
					if (questionStart < 0) {
						return;
					}
					String category = line.substring(0, questionStart).trim();
					if (!finer) {
						category = category.substring(0, category.indexOf(separator)).trim();
					}

					final String question;
//					if(this.namedEntityRecogition) {question = recognizeNamedEntities(line.substring(questionStart + 1).trim()); }
//					else question = line.substring(questionStart + 1).trim();
					question = line.substring(questionStart + 1).trim();

					AnalyzedQuestion analyzedQuestion = questionAnalyzer.analyze(question);
					InterpretedQuestion interpretedQuestion = new InterpretedQuestion(analyzedQuestion, category);
					getHandler().handle(interpretedQuestion);
				}
			}
		} catch (FileNotFoundException fnf) {
			System.err.println(fnf.getMessage());
		}
	}

	public Collection<ArrayList<String>> values() {
		return namedEntities.values();
	}

	public HashMap<String,ArrayList<String>> getNamedEntities() {
		return namedEntities;
	}


	@Override
	public void parseString(char[] chars, int i, int i1) throws IOException {
	}

	private static final long serialVersionUID = -3821206053563855940L;

	public void setNamedEntities(HashMap<String, ArrayList<String>> ne) {
		namedEntities = ne;
	}
}
