package l2f.evaluator.arguments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;

import l2f.config.Config;
import l2f.config.ConfigSVM;
import l2f.interpretation.classification.features.FeatureSet;

public class SVMArguments implements Serializable{
	private static final long serialVersionUID = 1799067896021717384L;

	public boolean fine;
	public boolean allSenses;
	public boolean neFlag;
	public ArrayList<ArrayList<FeatureSet>> features = new ArrayList<ArrayList<FeatureSet>>();
	public QuestionEvaluatorSet qceType;
	public String corpusDomain;
	public ArrayList<String> classifierType = new ArrayList<String>();
	public String corpusPath;
	private String featureDesc = "-";

	public SVMArguments(String corpusDomain, ArrayList<ArrayList<FeatureSet>> features){
		this.fine = ConfigSVM.fine;
		this.allSenses = ConfigSVM.allSenses;
		this.corpusDomain = corpusDomain;
		this.classifierType.add("SVM");
		this.qceType = QuestionEvaluatorSet.SVM;
		this.features = features;
		this.neFlag = processNEFlag();
		this.featureDesc += generateFeatureDesc(features).trim();
	}

	private String generateFeatureDesc(ArrayList<ArrayList<FeatureSet>> features) {
		String desc = "";
		for(FeatureSet fs : features.get(0)){
			desc += fs.getShortName() + "-";
		}
		return desc;
	}

	public String getCorpusPath(){
		return corpusPath;
	}


	private boolean processNEFlag() {
		Properties props = new Properties();

		try {
			File corpusDir = new File(Config.corpusDir + "/" + getCorpusDomain());
			for(File f : corpusDir.listFiles()){
				if(f.isDirectory())
					continue;
				else if(f.getName().substring(f.getName().lastIndexOf(".")).equals(".properties")){
					props.load(new FileInputStream(f));
					return Boolean.parseBoolean(props.getProperty("processNE"));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return false;
	}

	public boolean isNE(){
		return neFlag;
	}

	public boolean isFine() {
		return fine;
	}

	public boolean isAllSenses() {
		return allSenses;
	}

	public ArrayList<ArrayList<FeatureSet>> getFeatures() {
		return features;
	}

	public QuestionEvaluatorSet getQceType() {
		return qceType;
	}

	public String getCorpusDomain() {
		return corpusDomain;
	}

	public ArrayList<String> getClassifierType() {
		return classifierType;
	}
	
	public String getFeatureDesc(){
		return featureDesc;
	}

	public boolean getNeFlag(){
		return neFlag;
	}

	public String toString(){
		String featuresStr = "";
		for(ArrayList<FeatureSet> fCombs : getFeatures()){
			for(FeatureSet f : fCombs){
				featuresStr += f.toString() + " ";
			}
			featuresStr += "\n";
		}
		return corpusDomain+ "\n" + classifierType + "\n" + qceType.toString()  + "\n" + fine + "\n" + allSenses + "\n" + featuresStr;
	}
}