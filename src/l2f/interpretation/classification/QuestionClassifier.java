package l2f.interpretation.classification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import l2f.config.Config;
import l2f.interpretation.AnalyzedQuestion;
import l2f.interpretation.InterpretedQuestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliasi.classify.BaseClassifier;
import com.aliasi.classify.ConfusionMatrix;

/**
 * A machine learning-based question classifier.
 * 
 * @param <E> the type of question that can be classified, e.g., 
 * 	<code>String</code> or <code>AnalyzedQuestion</code>
 */
public class QuestionClassifier<E> implements Serializable {

	private BaseClassifier<E> classifier;
	private final String[] categories;
	private boolean neFlag;
	protected static final Random _rand = new Random(new Date().getTime());
	QuestionClassificationParser questionParser = new QuestionClassificationParser();

	private static final Logger logger = LoggerFactory.getLogger(QuestionClassifier.class);
	/**
	 * Creates a new instance of QuestionClassifier, using the classifier
	 * strategy provided by <code>classifier</code>.
	 * @param classifier
	 */
	public QuestionClassifier(BaseClassifier<E> classifier, String[] categories, boolean neFlag) {
		this.classifier = classifier;
		this.categories = categories;
		this.neFlag = neFlag;
	}

	public QuestionClassifier(QuestionClassifier<E>  classifier) {
		this.classifier = classifier.classifier;
		this.categories = classifier.categories;
	}


	/**
	 * Classifies a single question.
	 * @param instance the question to be classified
	 * @return the predicted category of the question
	 */
	public String classify(E instance) {
		return classifier.classify(instance).bestCategory();
	}

	public String classifyWithNE(E instance) {
		String cat = classify(instance);
		String namedEntities = " ";
		logger.info("Classifiyng with NE");
		for(String key : getQuestionClassificationParser().getNamedEntities().keySet()){
			logger.info(key);
			for(String entity : getQuestionClassificationParser().getNamedEntities().get(key)){
				namedEntities = namedEntities + "#" + key + "-" + entity + "# ";
			}
			namedEntities.substring(0, namedEntities.length() - 2);
		}
		logger.info("######");
		return cat + namedEntities;
	}

	public Double getMaxProb(E instance) {
		return ((l2f.classifiers.SvmClassifier<E>)classifier).getLargestProbability(instance);
	}
	
	public List<String> bestPredictions(E instance, int nPredictions) {
		return ((l2f.classifiers.SvmClassifier<E>)classifier).bestPredictions(instance, nPredictions);
	}

	public String getClassifierDescription(){
		return classifier.toString();
	}

	/**
	 * Classifies a set of questions.
	 * @param instances map that contains pairs of questions and corresponding categories
	 * @return the confusion matrix for this set of instances
	 */
	public ConfusionMatrix classify(Map<E, String> instances) {
		ConfusionMatrix cm = new ConfusionMatrix(categories);
		for (Map.Entry<E, String> entry : instances.entrySet()) {
			//interpreted question
			E key = entry.getKey();
			//				System.out.println("TEST INSTANCE: " + key.toString());
			//best category
			String value = entry.getValue();

			String predictedCategory = "";
			String currentPredictedCategory = classify(key);

			InterpretedQuestion iQuestion = ((InterpretedQuestion) key);
			AnalyzedQuestion aQuestion = iQuestion.getAnalyzedQuestion();

			if (iQuestion.getPredictedQuestionCategory().equalsIgnoreCase("")) {
				predictedCategory = currentPredictedCategory;
				iQuestion.setPredictedQuestionCategory(predictedCategory);
			} else {
				predictedCategory = iQuestion.getPredictedQuestionCategory();
			}

			String text = "Q=\"" + aQuestion.getOriginalQuestion();
			text += "\"\tCategory=\"" + value;
			text += "\"\tPredicted=\"" + predictedCategory;

			//				if(!namedEntityRecognition) out.write("\t\t\t<answer>" + getRandomAnswer(predictedCategory) + "</answer>\n");
			//				else out.write("\t\t\t<answer>" + value+"("+getNamedEntities(aQuestion.getOriginalQuestion())+")" + "</answer>\n");

			if (!predictedCategory.equalsIgnoreCase(currentPredictedCategory)) {
				text += "\"\tCurPredicted=\"" + currentPredictedCategory;
			}
			text += aQuestion.getHeuristicForHeadwordExtaction().equalsIgnoreCase("")? "" : "\"\tHeuristic=\"" + aQuestion.getHeuristicForHeadwordExtaction();
			text += "\"\tCORRECT?" + predictedCategory.equalsIgnoreCase(value);

			//				System.out.println(text);
			System.out.flush();

			cm.increment(value, predictedCategory);

		}

		return cm;
	}

	public String getNamedEntities(String question){

		try {

			BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(Config.named_entities_file), "UTF-8"));
			String line = new String();

			while((line = b.readLine()) != null){

				int endQuestion = line.indexOf("|");
				if(line.substring(0, endQuestion).equals(question)){
					b.close();
					return line.substring(endQuestion+1);
				}
			}		

		}catch (IOException e) {

		}
		return null;
	}

	public QuestionClassificationParser getQuestionClassificationParser(){
		return questionParser;
	}

	private static final long serialVersionUID = 5526851560485565880L;
}
