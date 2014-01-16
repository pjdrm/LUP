package l2f.evaluator.svm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import l2f.ClassifierApp;
import l2f.config.Config;
import l2f.corpus.Corpus;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.Utterance;
import l2f.corpus.processor.UtteranceProcessor;
import l2f.evaluator.QCEAnswer;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.evaluator.arguments.SVMArguments;
import l2f.interpretation.InterpretedQuestion;
import l2f.interpretation.QuestionAnalyzer;
import l2f.interpretation.classification.QuestionCategory;
import l2f.interpretation.classification.QuestionClassificationCorpus;
import l2f.interpretation.classification.QuestionClassificationParser;
import l2f.interpretation.classification.QuestionClassifier;
import l2f.interpretation.classification.QuestionClassifierFactory;
import l2f.interpretation.classification.features.FeatureSet;
import l2f.interpretation.classification.features.TestFeatureExtractor;
import l2f.tests.QCEBaseTester;
import l2f.tests.TesterInterface;
import l2f.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.corpus.ObjectHandler;

/**
 * Evaluates a <code>QuestionClassifier</code> using different feature
 * combinations, and reports the results to the filesystem.
 * 
 */
public class QuestionClassifierEvaluatorSVM implements QuestionClassifierEvaluator, Serializable{

	private Double classifierMinAccuracy = 0.1;
	/**
	 * Value for the accuracy that the classifier obtained in
	 * the testing.
	 */
	private double accuracy;
	private int nPredictions;
	/**
	 * Classifiers with good accuracy results are stored in this variable.
	 */
	private ArrayList<QuestionClassifier<InterpretedQuestion>> classifiers;
	private CorpusClassifier corpus;
	private SVMArguments args;
	private String individualScores = "";
	private TesterInterface tester;
	private String desc= "";
	private UtteranceProcessor utteranceProcessor;
	protected static final Logger logger = LoggerFactory.getLogger(ClassifierApp.class);

	public QuestionClassifierEvaluatorSVM(CorpusClassifier corpus, SVMArguments args, Double minAccuracy, int nPredictions) {
		this.corpus = corpus;
		this.utteranceProcessor = corpus.getUtteranceProcessor();
		this.args = args;
		this.classifierMinAccuracy = minAccuracy;
		this.desc = "SVM " + args.getFeatureDesc() + utteranceProcessor.getDescription()  + "Predictions " + nPredictions;
		this.nPredictions = nPredictions;
		
		tester = new QCEBaseTester(this);
	}
	
	public QuestionClassifierEvaluatorSVM(UtteranceProcessor up, SVMArguments args, Double minAccuracy, int nPredictions) {
		this.utteranceProcessor = up;
		this.args = args;
		this.classifierMinAccuracy = minAccuracy;
		this.desc = "SVM " + args.getFeatureDesc() + utteranceProcessor.getDescription() + "Predictions " + nPredictions;
		this.corpus = new CorpusClassifier();
		this.nPredictions = nPredictions;
		
		tester = new QCEBaseTester(this);
	}
	
	@Override
	public QuestionClassifierEvaluator clone() {
		return new QuestionClassifierEvaluatorSVM(this.utteranceProcessor, this.args, this.classifierMinAccuracy, this.nPredictions);
	}

	@Override
	public String getDescription(){
		return desc;
	}
	
	public String getindividualScores(){
		return individualScores;
	}

	public void setindividualScores(String scores) {
		this.individualScores = scores;

	}

	public CorpusClassifier getCorpus(){
		return corpus;
	}

	public SVMArguments getArgs(){
		return args;
	}

	public double getClassifierMinAccuracy(){
		return classifierMinAccuracy;
	}

	/**
	 * 
	 * @return The current classifier accurary.
	 */
	public double getAccuracy() {
		return accuracy;
	}

	/**
	 * @return Returns all classifiers that had a accuracy greater than classifierMinAccuracy. 
	 */
	public ArrayList<QuestionClassifier<InterpretedQuestion>> getClassifiers(){
		return classifiers;
	}

	/**
	 * 
	 * @param qcArray The new array of QuestionClassifiers.
	 */
	public void setClassifiers(ArrayList<QuestionClassifier<InterpretedQuestion>> qcArray) {
		classifiers = qcArray;
	}

	public TesterInterface getTester() {
		return tester;

	}

	private ArrayList<ArrayList<String>> createDirs(File trainingDirBase, File testDirBase) {
		ArrayList<ArrayList<String>> dirPairs = new ArrayList<ArrayList<String>>();
		BufferedWriter br;
		boolean firstLine;
		String trainPath;
		String testPath;
		for(int i = 0; i < getArgs().getFeatures().size(); i++ ){
			firstLine = true;
			try {
				trainPath = trainingDirBase + "/train" + i;
				File newTrainDir = new File(trainPath);
				newTrainDir.mkdir();
				br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newTrainDir + "/train.dat"), "UTF-8"));
				for(Utterance ut : getCorpus().getTrainUtterances()){
					if(firstLine){
						br.write(ut.getCat() + " " + ut.getUtterance());
						firstLine = false;
					}
					else
						br.write("\n" + ut.getCat() + " " + ut.getUtterance());
				}
				br.close();

				firstLine = true;
				testPath = testDirBase + "/test" + i;
				File newTestDir = new File(testPath);
				newTestDir.mkdir();
				br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newTestDir+ "/test.dat"), "UTF-8"));
				for(Utterance ut : getCorpus().getTestUtterances()){
					if(firstLine){
						br.write(ut.getCat() + " " + ut.getUtterance());
						firstLine = false;
					}
					else
						br.write("\n" + ut.getCat() + " " + ut.getUtterance());
				}
				br.close();
				ArrayList<String> pair = new ArrayList<String>();
				pair.add(trainPath);
				pair.add(testPath);
				dirPairs.add(pair);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		return dirPairs;
	}

	private class ClassifierResults{
		private String stats;
		private QuestionClassifier<InterpretedQuestion> qc;

		public ClassifierResults(String stats, QuestionClassifier<InterpretedQuestion> qc){
			this.stats = stats;
			this.qc = qc;
		}

		public String getStats(){
			return stats;
		}

		public QuestionClassifier<InterpretedQuestion> getQC(){
			return qc;
		}
	}

	private ClassifierResults createClassifier(boolean allSenses, boolean fineCategories, boolean namedEntityRecognition, ArrayList<String> dirPair, ArrayList<FeatureSet> feature, ArrayList<String> classifierTypes){
		try {
			QuestionClassifier<InterpretedQuestion> obtainedQC = null;
			final SortedMap<InterpretedQuestion, String> testInstances = new TreeMap<InterpretedQuestion, String>();
			QuestionAnalyzer questionAnalyzer = new QuestionAnalyzer(allSenses);
			String testFile = new File(dirPair.get(1)).list()[0];
			load(questionAnalyzer, testInstances, new File(dirPair.get(1) + "/" + testFile), fineCategories, namedEntityRecognition);
			QuestionClassificationCorpus<InterpretedQuestion> corpus = new QuestionClassificationCorpus<InterpretedQuestion>(
					new QuestionClassificationParser(questionAnalyzer, fineCategories, false), new File(dirPair.get(0)), new File(dirPair.get(1)));
			Map<Double, EnumSet<FeatureSet>> scores;
			scores = new TreeMap<Double, EnumSet<FeatureSet>>(new Comparator<Double>() {

				public int compare(Double arg1, Double arg2) {
					return (int) (arg2 - arg1);
				}
			});

			EnumSet<FeatureSet> current = EnumSet.copyOf(feature);
			logger.debug("Features => " + current);

			TestFeatureExtractor featureExtractor = new TestFeatureExtractor(current, getCorpus().getIWL());
			ConfusionMatrix matrix = null;
			String passedtypes = "";
			String stats = "";

			long startTotal = System.currentTimeMillis();
			int testCount = 1;
			long start = System.currentTimeMillis();
			for (int i = 0; i < classifierTypes.size(); i++) {
				passedtypes += classifierTypes.get(i) + "_";

				QuestionClassifier<InterpretedQuestion> qc =
					QuestionClassifierFactory.newQuestionClassifier(
							featureExtractor, corpus, classifierTypes.get(i), fineCategories, getArgs().getNeFlag());
				matrix = qc.classify(testInstances);

//				if(matrix.totalAccuracy() > getClassifierMinAccuracy())
					obtainedQC = qc;

				logger.debug("Test " + testCount + "\\" + 1);
				testCount++;

				long end = System.currentTimeMillis();
				String time = "";
				if (fineCategories) {
					time = passedtypes.replaceAll("_$", "") + " FINE Question classification of " + testInstances.keySet().size()
					+ " instances in " + (end - start) + " ms.";
				} else {
					time = passedtypes.replaceAll("_$", "") + " COARSE Question classification of " + testInstances.keySet().size()
					+ " instances in " + (end - start) + " ms.";
				}

				stats = "Features= " + current + "\tAccuracy= " + matrix.totalAccuracy() + "\tTime(ms)= " + (end - start) + "\n";
				logger.debug("\n" + time + " with accuracy " + matrix.totalAccuracy());
				/*if (testInstances.isEmpty()) {
					continue;
				}
				InterpretedQuestion test = testInstances.keySet().iterator().next();
				start = System.currentTimeMillis();
				qc.classify(test);
				end = System.currentTimeMillis();
				logger.debug("Question classification of a single instance in " + (end - start) + " ms.");
				scores.put(matrix.totalAccuracy(), current);*/

				//clean questionCategory of testInstances
				//					for(InterpretedQuestion iq : testInstances.keySet()){
				//						iq.setPredictedQuestionCategory("");
				//					}

				// clean up
				System.gc();
				return new ClassifierResults(stats, obtainedQC);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	
	/*private class CreateClassifier implements Callable<ClassifierResults>{
		private boolean allSenses;
		private boolean fineCategories;
		private boolean namedEntityRecognition;
		private ArrayList<String> dirPair;
		private ArrayList<FeatureSet> feature;
		private ArrayList<String> classifierTypes;
		private String stats = "";

		private CreateClassifier(boolean allsenses, boolean fineCategories, boolean namedEntityRecognition, ArrayList<String> dirPair, ArrayList<FeatureSet> feature, ArrayList<String> classifierTypes){
			this.allSenses = allsenses;
			this.fineCategories = fineCategories;
			this.namedEntityRecognition = namedEntityRecognition;
			this.dirPair = dirPair;
			this.feature = feature;
			this.classifierTypes = classifierTypes;
		}

		public ClassifierResults call() {
			try {
				QuestionClassifier<InterpretedQuestion> obtainedQC = null;
				final SortedMap<InterpretedQuestion, String> testInstances = new TreeMap<InterpretedQuestion, String>();
				QuestionAnalyzer questionAnalyzer = new QuestionAnalyzer(allSenses);
				String testFile = new File(dirPair.get(1)).list()[0];
				load(questionAnalyzer, testInstances, new File(dirPair.get(1) + "/" + testFile), fineCategories, namedEntityRecognition);
				QuestionClassificationCorpus<InterpretedQuestion> corpus = new QuestionClassificationCorpus<InterpretedQuestion>(
						new QuestionClassificationParser(questionAnalyzer, fineCategories, false), new File(dirPair.get(0)), new File(dirPair.get(1)));
				Map<Double, EnumSet<FeatureSet>> scores;
				scores = new TreeMap<Double, EnumSet<FeatureSet>>(new Comparator<Double>() {

					public int compare(Double arg1, Double arg2) {
						return (int) (arg2 - arg1);
					}
				});

				EnumSet<FeatureSet> current = EnumSet.copyOf(feature);
				logger.debug("Features => " + current);

				TestFeatureExtractor featureExtractor = new TestFeatureExtractor(current, getCorpus().getIWL());
				ConfusionMatrix matrix = null;
				String passedtypes = "";

				long startTotal = System.currentTimeMillis();
				int testCount = 1;
				long start = System.currentTimeMillis();
				for (int i = 0; i < classifierTypes.size(); i++) {
					passedtypes += classifierTypes.get(i) + "_";

					QuestionClassifier<InterpretedQuestion> qc =
						QuestionClassifierFactory.newQuestionClassifier(
								featureExtractor, corpus, classifierTypes.get(i), fineCategories, getArgs().getNeFlag());
					matrix = qc.classify(testInstances);

//					if(matrix.totalAccuracy() > getClassifierMinAccuracy())
						obtainedQC = qc;

					logger.debug("Test " + testCount + "\\" + 1);
					testCount++;

					long end = System.currentTimeMillis();
					String time = "";
					if (fineCategories) {
						time = passedtypes.replaceAll("_$", "") + " FINE Question classification of " + testInstances.keySet().size()
						+ " instances in " + (end - start) + " ms.";
					} else {
						time = passedtypes.replaceAll("_$", "") + " COARSE Question classification of " + testInstances.keySet().size()
						+ " instances in " + (end - start) + " ms.";
					}

					stats = "Features= " + current + "\tAccuracy= " + matrix.totalAccuracy() + "\tTime(ms)= " + (end - start) + "\n";
					logger.debug("\n" + time + " with accuracy " + matrix.totalAccuracy());
					if (testInstances.isEmpty()) {
						continue;
					}
					InterpretedQuestion test = testInstances.keySet().iterator().next();
					start = System.currentTimeMillis();
					qc.classify(test);
					end = System.currentTimeMillis();
					logger.debug("Question classification of a single instance in " + (end - start) + " ms.");
					scores.put(matrix.totalAccuracy(), current);

					//clean questionCategory of testInstances
					//					for(InterpretedQuestion iq : testInstances.keySet()){
					//						iq.setPredictedQuestionCategory("");
					//					}

					// clean up
					System.gc();
					return new ClassifierResults(stats, obtainedQC);
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			return null;
		}
	}*/

	public void run(ArrayList<String> classifierTypes, ArrayList<ArrayList<FeatureSet>> singleFeatureCombination,
			boolean fineCategories, boolean allsenses, boolean namedEntityRecognition) throws IOException {

		ArrayList<QuestionClassifier<InterpretedQuestion>> qcArray = new ArrayList<QuestionClassifier<InterpretedQuestion>>();
		File testDirBase = Utils.checkDir(Config.classification_testDir);

		logger.debug("Loaded QuestionAnalyzer!");
		logger.debug("Loaded test file!");
		File trainingDirBase = Utils.checkDir(Config.classification_trainDir);
		ArrayList<ArrayList<String>> dirPairs = createDirs(trainingDirBase, testDirBase);

		loadCategories();
		logger.debug("Loaded training file!");
		logger.info("ARGS\n" + getArgs().toString());

		int pairIndex = 0;
//		ExecutorService executor = Executors.newCachedThreadPool();
		List<ClassifierResults> results = new ArrayList<ClassifierResults>();
		ClassifierResults result;
		for(ArrayList<FeatureSet> feature : getArgs().getFeatures()){
			result = createClassifier(allsenses, fineCategories, namedEntityRecognition, dirPairs.get(pairIndex), feature, classifierTypes);
			results.add(result);
			pairIndex++;
		}
		
		String scores = "";
		boolean firstScore = true;
		for(ClassifierResults res : results){
			if(res.getQC() != null)
				qcArray.add(res.getQC());
			System.out.println("Stats " + res.getStats());
			if(firstScore){
				scores += res.getStats();
				firstScore = false;
			}
			else{
				scores += "\n" + res.getStats();
			}
		}
		setindividualScores(scores);
		
		/*for(ArrayList<FeatureSet> feature : getArgs().getFeatures()){
			Callable<ClassifierResults> worker = new CreateClassifier(allsenses, fineCategories, namedEntityRecognition, dirPairs.get(pairIndex), feature, classifierTypes);
			Future<ClassifierResults> submit = executor.submit(worker);
			futureResults.add(submit);
			pairIndex++;
		}

		try {
			String scores = "";
			boolean firstScore = true;
			for(Future<ClassifierResults> crFuture : futureResults){
				ClassifierResults cr = crFuture.get();
				if(cr.getQC() != null)
					qcArray.add(cr.getQC());
				System.out.println("Stats " + cr.getStats());
				if(firstScore){
					scores += cr.getStats();
					firstScore = false;
				}
				else{
					scores += "\n" + cr.getStats();
				}
			}
			setindividualScores(scores);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("InterruptedException");
			System.exit(1);
		} catch (ExecutionException e) {
			e.printStackTrace();
			System.out.println("ExecutionException\n" + e.getMessage());
			System.exit(1);
		}

		executor.shutdown();*/

		//		long endTotal = System.currentTimeMillis();
		//		String timeTotal = "Question classification of " + singleFeatureCombination
		//		+ " in " + (endTotal - startTotal) + " ms.";
		//		logger.debug(timeTotal);
		setClassifiers(qcArray);
	}

	private void loadCategories(){
		for(Utterance ut : getCorpus().getTestUtterances()){
			QuestionCategory.addCategory(ut.getCat());
		}

		for(Utterance ut : getCorpus().getTrainUtterances()){
			QuestionCategory.addCategory(ut.getCat());
		}
	}

	private void load(QuestionAnalyzer questionAnalyzer,
			final Map<InterpretedQuestion, String> testInstances, File testDir,
			boolean useFineGrainedCategories, boolean namedEntities) throws IOException {
		QuestionClassificationCorpus<InterpretedQuestion> corpus =
			new QuestionClassificationCorpus<InterpretedQuestion>(
					new QuestionClassificationParser(questionAnalyzer, useFineGrainedCategories, namedEntities), testDir);
		corpus.visitTest(new ObjectHandler<InterpretedQuestion>() {

			@Override
			public void handle(InterpretedQuestion question) {
				testInstances.put(question, question.getQuestionCategory().toString());
			}
		});
	}

	public QCEAnswer answerWithQCEAnswer(String question){	
		question = utteranceProcessor.processString(question);
		String modifications = utteranceProcessor.getModifications();
		
		InterpretedQuestion interpretedQuestion = new InterpretedQuestion(
				QuestionAnalyzer.getInstance().analyze(question), 
		"dummy_cat");

		QuestionClassifier<InterpretedQuestion> qc = null;
		Double bestScore = null;
		if(getClassifiers().size() > 1){
			// find best classifier for question based on SVM class(category) probability
			TreeMap<Double, QuestionClassifier<InterpretedQuestion>> bestqc = new TreeMap<Double, QuestionClassifier<InterpretedQuestion>>();
			for(QuestionClassifier<InterpretedQuestion> qc1 : getClassifiers()){
				double cprob = qc1.getMaxProb(interpretedQuestion);
				bestqc.put(cprob, qc1);
			}
			Entry<Double, QuestionClassifier<InterpretedQuestion>> key = bestqc.pollLastEntry();
			bestScore = key.getKey();
			qc = key.getValue();
		}else{
			//skip when only one
			bestScore = getClassifiers().get(0).getMaxProb(interpretedQuestion);
			qc = getClassifiers().get(0);
		}

		String cat;
		cat = qc.classify(interpretedQuestion);
//		List<String> bp = qc.bestPredictions(interpretedQuestion, nPredictions);
//		ArrayList<Utterance> possibleAnswers = new ArrayList<Utterance>();
//		for(String predCat : bp){
//			possibleAnswers.addAll(getCorpus().getAnswer(predCat));
//		}
		int index = qc.getClassifierDescription().indexOf("[");
		String classifierType = qc.getClassifierDescription().substring(index);
		ArrayList<Utterance> possibleAnswers = getCorpus().getAnswer(cat);
		return new QCEAnswer(possibleAnswers, modifications, classifierType, bestScore);
	}

	public ArrayList<String> answerQuestion(String question){
		return answerWithQCEAnswer(question).getStringPossibleAnswers();
	}


	public void runClassification() {
		boolean allsenses = getArgs().isAllSenses();
		boolean fineCategories = getArgs().isFine();
		String resultsDir = "results/" + (fineCategories ? "fine" : "coarse") + "/";

		boolean exists = (new File(resultsDir)).exists();
		if (!exists) {
			boolean success = (new File(resultsDir)).mkdirs();
			if (success) {
				logger.debug("Directory: " + resultsDir + " created");
			}
		}

		logger.debug("STARTING:   " + getArgs().toString());

		try {
			run(getArgs().getClassifierType(), getArgs().getFeatures(), fineCategories, allsenses, getArgs().isNE());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void resetClassifiers() {
		getClassifiers().clear();
	}

	@Override
	public QuestionEvaluatorSet getType() {
		return QuestionEvaluatorSet.SVM;
	}
	
	@Override
	public void setCorpus(Corpus corpus) {
		List<Utterance> newTestUtterances = new ArrayList<Utterance>();
		Utterance newUt;
		for(Utterance ut : corpus.getTestUtterances()){
			newUt = new Utterance(ut.getCat(), ut.getUtterance());
//			utteranceProcessor.processUtterance(newUt);
			newTestUtterances.add(newUt);
		}
		this.corpus.setTestUtterances(newTestUtterances);
		
		List<Utterance> newTrainUtterances = new ArrayList<Utterance>();
		for(Utterance ut : corpus.getTrainUtterances()){
			newUt = new Utterance(ut.getCat(), ut.getUtterance());
			utteranceProcessor.processUtterance(newUt);
			newTrainUtterances.add(newUt);
		}
		this.corpus.setTrainUtterances(newTrainUtterances);
		
//		this.corpus = (CorpusClassifier)corpus;
		this.corpus.setAnswers(((CorpusClassifier) corpus).getAnswers());
		this.corpus.setAnswersMap(((CorpusClassifier) corpus).getAnswersMap());
	}
	private static final long serialVersionUID = -3335726457228405193L;
}
