package l2f.evaluator.frames;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import l2f.ClassifierApp;
import l2f.config.Config;
import l2f.config.ConfigSVM;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.CorpusFrameClassifier;
import l2f.corpus.Utterance;
import l2f.corpus.factory.CorpusFrameClassifierFactory;
import l2f.corpus.parser.CorpusFrameParser;
import l2f.corpus.parser.frames.FrameParser;
import l2f.evaluator.QCEFactory;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.evaluator.arguments.SVMArguments;
import l2f.evaluator.svm.QuestionClassifierEvaluatorSVM;
import l2f.evaluator.svm.SVMFactory;
import l2f.interpretation.classification.features.FeatureSet;

public class FramesClassifierFactory {

	private ArrayList<CorpusFrameParser> frameParsers = new ArrayList<CorpusFrameParser>();
	
	public FramesClassifierFactory(){
		initializeFrameCorpusParsers();
	}
	
	private void initializeFrameCorpusParsers(){
		frameParsers.add(new FrameParser());
	}
	
	private	ArrayList<QuestionClassifierEvaluator> getFramesQCE(String corpusDomain, QuestionEvaluatorSet qceType) {
		/*ArrayList<QuestionClassifierEvaluator> qceFramesSVMArray = getRawFramesQCE(corpusDomain, qceType);
		for(QuestionClassifierEvaluator qce : qceFramesSVMArray)
			qce.runClassification();
		
		return qceFramesSVMArray;*/
		return null;
	}
	
	private	ArrayList<QuestionClassifierEvaluator> getRawFramesQCE(String corpusDomain, String[] stopwordsFlags, String[] normalizeStringFlags, String[] posTaggerFlags, String corpusPropertiesPath, QuestionEvaluatorSet qceType) {
		CorpusFrameClassifierFactory cfcFact = new CorpusFrameClassifierFactory(frameParsers);
		CorpusFrameClassifier cfc = cfcFact.parseFrameCorpus(Config.corpusDir + "/" + corpusDomain);
		ArrayList<QuestionClassifierEvaluator> qceArray = new ArrayList<QuestionClassifierEvaluator>();
		
		String nluTechsForFrames = Config.nluTechniquesForFrames.replaceAll(" ", "");
		String[] nluTechsArrayForFrames = nluTechsForFrames.split(",");
		List<QuestionClassifierEvaluator> toUseQCE = QCEFactory.getRawInstances(corpusDomain, nluTechsArrayForFrames, stopwordsFlags, normalizeStringFlags, posTaggerFlags, corpusPropertiesPath);
		HashMap<String, QuestionClassifierEvaluator> attributeClassifiers;
		for(QuestionClassifierEvaluator qceForFrame : toUseQCE){
			attributeClassifiers = new HashMap<String, QuestionClassifierEvaluator>();
			for(String attrKey :  cfc.getAttributesMap().keySet()){
				FrameAttribute fa = cfc.getAttributesMap().get(attrKey);
				QuestionClassifierEvaluator qceAttr = qceForFrame.clone();
				attributeClassifiers.put(fa.getName(), qceAttr);

			}
			
			if(qceType.equals(QuestionEvaluatorSet.FRAMESAF)){
				qceArray.add(new QuestionClassifierEvaluatorAF(cfc, qceForFrame, attributeClassifiers));
			}
			else if(qceType.equals(QuestionEvaluatorSet.FRAMESFF))
				qceArray.add(new QuestionClassifierEvaluatorFF(cfc, qceForFrame, attributeClassifiers));
		}
		
		return qceArray;
	}
	
	public 	ArrayList<QuestionClassifierEvaluator> getFramesAFQCE(String corpusDomain){
		return getFramesQCE(corpusDomain, QuestionEvaluatorSet.FRAMESAF);
	}
	
	public ArrayList<QuestionClassifierEvaluator> getRawFramesAFQCE(String corpusDomain, String[] stopwordsFlags, String[] normalizeStringFlags, String[] posTaggerFlags, String corpusPropertiesPath) {
		return getRawFramesQCE(corpusDomain, stopwordsFlags, normalizeStringFlags, posTaggerFlags, corpusPropertiesPath, QuestionEvaluatorSet.FRAMESAF);
	}
	
	public 	ArrayList<QuestionClassifierEvaluator> getFramesFFQCE(String corpusDomain){
		return getFramesQCE(corpusDomain, QuestionEvaluatorSet.FRAMESFF);
	}

	public ArrayList<QuestionClassifierEvaluator> getRawFramesFFQCE(String corpusDomain, String[] stopwordsFlags, String[] normalizeStringFlags, String[] posTaggerFlags, String corpusPropertiesPath) {
		return getRawFramesQCE(corpusDomain, stopwordsFlags, normalizeStringFlags, posTaggerFlags, corpusPropertiesPath, QuestionEvaluatorSet.FRAMESFF);
	}
}
