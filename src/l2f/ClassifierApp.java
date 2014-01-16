package l2f;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import l2f.config.Config;
import l2f.evaluator.QCEFactory;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.ModeSet;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.out.of.domain.OutOfDomainEvaluator;
import l2f.out.of.domain.OutOfDomainEvaluatorFactory;
import l2f.out.of.domain.OutOfDomainSet;
import l2f.tests.OutOfDomainEvaluatorTester;
import l2f.tests.QCECrossValidation;
import l2f.tests.QCEReferenceTester;
import l2f.tests.QCETester;
import l2f.utils.ScriptUtils;

public class ClassifierApp {

	private static String[] nluTechsArray;
	private static String[] stopWordsFlagsArray;
	private static String[] normalizeStringFlagsArray;
	private static String[] posTaggerFlagsArray;
	private static int maxPredictions;
	private static ArrayList<QuestionClassifierEvaluator> qceArray;
	private static String mode;
	private static String corpusPropertiesPath;

	public static String[] removeLastElement(String[] input) {
		String[] result = new String[input.length - 1];
		int i = 0;

		while(i < result.length){
			result[i] = input[i];
			i++;
		}
		return result;
	}

	/**
	 * TODO review this documentation and add reference to <code>l2f.interpretation.classification.features.FeatureSet</code>
	 * @throws IOException
	 */

	public static void main(String[] args) throws IOException {
		initLUP(args);
		runLUP(args);
	}

	private static void initLUP(String[] args) {
		Config.parseConfig("./resources/qa/config/config_en.xml");

		if(args.length != 1){
			System.err.println("ERROR:\n The application requires a domain as argument.");
		}
		else
			checkDomain(args[0]);

		checkMode();

		String nluTechs = Config.nluTechniques.replaceAll(" ", "");
		nluTechsArray = nluTechs.split(",");

		String stopWordsFlags = Config.stopwordsFlags.replaceAll(" ", "");
		stopWordsFlagsArray = stopWordsFlags.split(",");

		String normalizeStringFlags = Config.normalizeStringFlags.replaceAll(" ", "");
		normalizeStringFlagsArray = normalizeStringFlags.split(",");

		String posTaggerFlags = Config.posTaggerFlags.replaceAll(" ", "");
		posTaggerFlagsArray = posTaggerFlags.split(",");

		checkNLUTechniques(nluTechsArray);
		qceArray = new ArrayList<QuestionClassifierEvaluator>();
		mode = Config.mode;
		corpusPropertiesPath = getCorpusPropertiesPath(Config.corpusDir + "/" + args[0]);
		
		maxPredictions = Config.maxPredictions;
	}

	public static void runLUP(String[] args){
		if(mode.equals(ModeSet.DEVELOP.mode())){
			qceArray = QCEFactory.getInstances(args[0], nluTechsArray, stopWordsFlagsArray, normalizeStringFlagsArray, posTaggerFlagsArray, corpusPropertiesPath, maxPredictions);
		}
		else if(mode.equals(ModeSet.TEST.mode())){
			qceArray = QCEFactory.deployInstances(args[0], nluTechsArray, stopWordsFlagsArray, normalizeStringFlagsArray, posTaggerFlagsArray, corpusPropertiesPath, maxPredictions);
			QCEReferenceTester tester = new QCEReferenceTester(Config.resulsDir, qceArray, args[0], Config.usersTestPath, corpusPropertiesPath);
			tester.publishAllResults();
			return;
		}
		else if(mode.equals(ModeSet.CrossValidation.mode())){
			qceArray = QCEFactory.getRawInstances(args[0], nluTechsArray, stopWordsFlagsArray, normalizeStringFlagsArray, posTaggerFlagsArray, corpusPropertiesPath, maxPredictions);
			QCECrossValidation qceCV = new QCECrossValidation(qceArray, Config.nPartitions, Config.alpha);
			qceCV.performCrossValidation(args[0]);
			return;
		}
		else if(mode.equals(ModeSet.ReCrossValidate.mode())){
			qceArray = QCEFactory.getRawInstances(args[0], nluTechsArray, stopWordsFlagsArray, normalizeStringFlagsArray, posTaggerFlagsArray, corpusPropertiesPath, maxPredictions);
			QCECrossValidation qceCV = new QCECrossValidation(qceArray, Config.alpha);
			qceCV.reCrossValidate(Config.resulsDir+"/"+Config.reCVdir, "./"+Config.corpusDir+"/"+args[0]);
			return;
		}
		else if(mode.equals(ModeSet.OOD.mode())){
			String oodTechs = Config.oodTechniques.replaceAll(" ", "");
			String[] oodTechsTechsArray = oodTechs.split(",");
			checkOODTechniques(oodTechsTechsArray);
			List<OutOfDomainEvaluator> oodList = OutOfDomainEvaluatorFactory.getInstances(args[0], oodTechsTechsArray, stopWordsFlagsArray, normalizeStringFlagsArray, posTaggerFlagsArray, corpusPropertiesPath);
			OutOfDomainEvaluatorTester tester = new OutOfDomainEvaluatorTester(Config.resulsDir, args[0], Config.oodTestDirPath, oodList, corpusPropertiesPath);
			tester.publishAllResults();
			return;
		}

		QCETester tester = new QCETester(Config.resulsDir, qceArray, args[0]);
		tester.publishAllResults();
		return;
	}
	
	public static QuestionClassifierEvaluator getDeployedQCE(String[] args){
		initLUP(args);
		qceArray = QCEFactory.deployInstances(args[0], nluTechsArray, stopWordsFlagsArray, normalizeStringFlagsArray, posTaggerFlagsArray, corpusPropertiesPath, maxPredictions);
		return qceArray.get(0);
	}

	public static String getCorpusPropertiesPath(String corpusDirPath) {
		File corpusDir = new File(corpusDirPath);
		try {
			for(File f : corpusDir.listFiles()){
				if(f.isDirectory())
					continue;
				else if(f.getName().substring(f.getName().lastIndexOf(".")).equals(".properties")){
					return f.getCanonicalPath();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("ERROR: Please provide a properties file for " + corpusDirPath);
		System.exit(1);
		return null;
	}

	private static Object loadObject(String objectPath) {
		try {
			FileInputStream f_in;
			f_in = new FileInputStream(objectPath);

			ObjectInputStream obj_in = new ObjectInputStream (f_in);
			return obj_in.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static QuestionClassifierEvaluator getDefaultQCE() {
		try {
			FileInputStream f_in;
			f_in = new FileInputStream(Config.serFile);

			ObjectInputStream obj_in = new ObjectInputStream (f_in);
			return (QuestionClassifierEvaluator)obj_in.readObject();
		} catch (FileNotFoundException e) {
			System.out.println("ClassifierApp: No QuestionClassifierEvaluator was serialised.");
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("ClassifierApp: No QuestionClassifierEvaluator was serialised.");
			System.exit(-1);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void prepareDirs() {
		try {
			ScriptUtils.deleteAllfiles(Config.classification_trainDir);
			ScriptUtils.deleteAllfiles(Config.classification_testDir);
			for(String dir : new File(Config.classification_trainDir).list()){

				deleteDir(new File(Config.classification_trainDir + "/" + dir));

			}

			for(String dir : new File(Config.classification_testDir).list()){
				deleteDir(new File(Config.classification_testDir + "/" + dir));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void deleteDir(File file)	throws IOException{
		if(file.isDirectory()){
			if(file.list().length==0){
				file.delete();
			}
			else{
				String files[] = file.list();
				for (String temp : files) {
					File fileDelete = new File(file, temp);
					deleteDir(fileDelete);
				}

				if(file.list().length==0){
					file.delete();
				}
			}
		}
		else{
			file.delete();
		}
	}

	private static void checkMode(){
		String modes = "";
		boolean modeExists = false;
		for(ModeSet ms : ModeSet.values()){
			if(ms.mode().equals(Config.mode)){
				modeExists = true;
				break;
			}
			modes += ms.mode() + "\n";
		}
		if(!modeExists){
			System.err.println("ERROR:\nThe following mode does not exist: " + Config.mode + "\nAvailable modes:\n" + modes);
			System.exit(1);
		}
	}

	private static void checkDomain(String domain){
		String availableCorpus = "";
		File corpusDir = new File(Config.corpusDir);
		for(String str : corpusDir.list()){
			File f = new File(Config.corpusDir + "/" + str);
			if(!f.isDirectory() || str.startsWith("."))
				continue;
			if(domain.equals(str)){
				return;
			}
			availableCorpus += str + " ";
		}

		System.err.println("ERROR:\nNo corpus found. The directory " +Config.corpusDir + "/" + domain + " does not exist.\nThe following corpus are available: " + availableCorpus);
		System.exit(1);
	}

	private static void checkNLUTechniques(String[] nluTechsArray) {
		String availableTechniques = "";
		boolean qesExists = false;
		for(String tech : nluTechsArray){
			availableTechniques = "";
			for(QuestionEvaluatorSet qes : QuestionEvaluatorSet.values()){
				if(qes.type().equalsIgnoreCase(tech)){
					qesExists = true;
				}
				availableTechniques += qes.type() + "\n";
			}
			if(!qesExists){
				System.err.println("ERROR:\nThe following NLU technique does not exist: " + tech + "\nAvailable Classifiers:\n" + availableTechniques);
			}
			qesExists = false;
		}

	}

	private static void checkOODTechniques(String[] oodTechsArray) {
		String availableTechniques = "";
		boolean qesExists = false;
		for(String tech : oodTechsArray){
			availableTechniques = "";
			for(OutOfDomainSet oodSet : OutOfDomainSet.values()){
				if(oodSet.type().equalsIgnoreCase(tech)){
					qesExists = true;
				}
				availableTechniques += oodSet.type() + "\n";
			}
			if(!qesExists){
				System.err.println("ERROR:\nThe following Out of Domain technique does not exist: " + tech + "\nAvailable Techniques:\n" + availableTechniques);
			}
			qesExists = false;
		}

	}
}
