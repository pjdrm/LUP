package l2f.evaluator;

import java.util.ArrayList;

import l2f.config.Config;
import l2f.config.ConfigCrossEntropy;
import l2f.config.ConfigDistance;
import l2f.config.ConfigImportantWords;
import l2f.config.ConfigLSA;
import l2f.config.ConfigVSM;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.evaluator.clm.CLMFactory;
import l2f.evaluator.distance.algorithms.DistanceFactory;
import l2f.evaluator.entropy.CrossEntropyFactory;
import l2f.evaluator.frames.FramesClassifierFactory;
import l2f.evaluator.important.words.ImportantWordsFactory;
import l2f.evaluator.logistic.regression.LogisticRegreessionFactory;
import l2f.evaluator.lsa.LSAFactory;
import l2f.evaluator.svm.SVMFactory;
import l2f.evaluator.vsm.VSMFactory;

public class QCEFactory {

	private static ArrayList<QuestionClassifierEvaluator> generatedQCE = new ArrayList<QuestionClassifierEvaluator>();

	public static ArrayList<QuestionClassifierEvaluator> getInstances(String corpusDomain, String[] nluTechniques, String[] stopwordsFlags, String[] normalizeStringFlags, String[] posTaggerFlags, String corpusPropertiesPath){
		return getInstances(corpusDomain, nluTechniques, stopwordsFlags, normalizeStringFlags, posTaggerFlags, corpusPropertiesPath, 1);
	}
	
	public static ArrayList<QuestionClassifierEvaluator> getInstances(String corpusDomain, String[] nluTechniques, String[] stopwordsFlags, String[] normalizeStringFlags, String[] posTaggerFlags, String corpusPropertiesPath, int maxPredictions){
		for(String swFlag : stopwordsFlags){
			Config.stopwordsFlag = Boolean.parseBoolean(swFlag);

			for(String normalizeStringFlag : normalizeStringFlags){
				Config.normalizeStringFlag = Boolean.parseBoolean(normalizeStringFlag);

				for(String posTaggerFlag : posTaggerFlags){
					Config.posTaggerFlag = Boolean.parseBoolean(posTaggerFlag); 

					for(String tech : nluTechniques){
						if(tech.equals(QuestionEvaluatorSet.SVM.type())){
							SVMFactory svmFactory = new SVMFactory(corpusPropertiesPath);
							generatedQCE.addAll(svmFactory.getSVMQCE(corpusDomain, maxPredictions));
						}
						else if(tech.equals(QuestionEvaluatorSet.LSA.type())){
							ConfigLSA.parseConfig();
							LSAFactory lsaFactory = new LSAFactory(ConfigLSA.features, 
									ConfigLSA.vectorCalc,
									corpusPropertiesPath, 
									maxPredictions,
									ConfigLSA.maxFactors,
									ConfigLSA.featureInit,
									ConfigLSA.initialLearningRate,
									ConfigLSA.annealingRate,
									ConfigLSA.regularization,
									ConfigLSA.minImprovement,
									ConfigLSA.minEpochs,
									ConfigLSA.maxEpochs);
							generatedQCE.addAll(lsaFactory.getLSAQCE(corpusDomain));
						}
						else if(tech.equals(QuestionEvaluatorSet.IW.type())){
							ConfigImportantWords.parseConfig();
							ImportantWordsFactory iwFactory = new ImportantWordsFactory(ConfigImportantWords.features, ConfigImportantWords.maxIW, corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(iwFactory.getImportantWordsQCE(corpusDomain));
						}
						else if(tech.equals(QuestionEvaluatorSet.VSM.type())){
							ConfigVSM.parseConfig();
							VSMFactory vsmFactory = new VSMFactory(corpusPropertiesPath, maxPredictions, ConfigVSM.k_neighbours, ConfigVSM.features, ConfigVSM.utteranceWeight, ConfigVSM.freqCounters);
							generatedQCE.addAll(vsmFactory.getVSMQCE(corpusDomain));
						}
						else if(tech.equals(QuestionEvaluatorSet.LR.type())){
							ConfigCrossEntropy.parseConfig();
							LogisticRegreessionFactory lrFactory = new LogisticRegreessionFactory(corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(lrFactory.getLogisticRegressionQCE(corpusDomain));
						}
						else if(tech.equals(QuestionEvaluatorSet.CE.type())){
							ConfigCrossEntropy.parseConfig();
							CrossEntropyFactory clmFactory = new CrossEntropyFactory(ConfigCrossEntropy.features, corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(clmFactory.getCrossEntropyQCE(corpusDomain));
						}
						else if(tech.equals(QuestionEvaluatorSet.CLM.type())){
							CLMFactory clmFactory = new CLMFactory(corpusPropertiesPath);
							generatedQCE.addAll(clmFactory.getCLMQCE(corpusDomain));
						}
						else if(tech.equals(QuestionEvaluatorSet.FRAMESAF.type())){
							FramesClassifierFactory fafFactory = new FramesClassifierFactory();
							generatedQCE.addAll(fafFactory.getFramesAFQCE(corpusDomain));
						}
						else if(tech.equals(QuestionEvaluatorSet.FRAMESFF.type())){
							FramesClassifierFactory fafFactory = new FramesClassifierFactory();
							generatedQCE.addAll(fafFactory.getFramesFFQCE(corpusDomain));
						}
						else if(tech.equals(QuestionEvaluatorSet.DICE.type())){
							ConfigDistance.parseConfig();
							DistanceFactory distanceFactory = new DistanceFactory(ConfigDistance.features, ConfigDistance.k_neighbours, ConfigDistance.setIntersectors, corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(distanceFactory.getDiceQCE(corpusDomain));
						}
						else if(tech.equals(QuestionEvaluatorSet.JACCARD.type())){
							ConfigDistance.parseConfig();
							DistanceFactory distanceFactory = new DistanceFactory(ConfigDistance.features, ConfigDistance.k_neighbours, ConfigDistance.setIntersectors, corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(distanceFactory.getJaccardQCE(corpusDomain));
						}
						else if(tech.equals(QuestionEvaluatorSet.JACCARDOVERLAP.type())){
							ConfigDistance.parseConfig();
							DistanceFactory distanceFactory = new DistanceFactory(ConfigDistance.features, ConfigDistance.k_neighbours, ConfigDistance.setIntersectors, corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(distanceFactory.getJaccardOverlapQCE(corpusDomain));
						}
						else if(tech.equals(QuestionEvaluatorSet.JACCARDOVERLAPTFIDF.type())){
							ConfigDistance.parseConfig();
							DistanceFactory distanceFactory = new DistanceFactory(ConfigDistance.features, ConfigDistance.k_neighbours, ConfigDistance.setIntersectors, corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(distanceFactory.getJaccardOverlapTfidfQCE(corpusDomain));
						}
						else if(tech.equals(QuestionEvaluatorSet.OVERLAP.type())){
							ConfigDistance.parseConfig();
							DistanceFactory distanceFactory = new DistanceFactory(ConfigDistance.features, ConfigDistance.k_neighbours, ConfigDistance.setIntersectors, corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(distanceFactory.getOverlapQCE(corpusDomain));
						}
					}
				}
			}
		}
		return generatedQCE;
	}

	public static ArrayList<QuestionClassifierEvaluator> deployInstances(String corpusDomain, String[] nluTechniques, String[] stopwordsFlags, String[] normalizeStringFlags, String[] posTaggerFlags, String corpusPropertiesPath, int maxPredictions) {
		Config.testPercentage = 0.0;
		return getInstances(corpusDomain, nluTechniques, stopwordsFlags, normalizeStringFlags, posTaggerFlags, corpusPropertiesPath, maxPredictions);
	}
	
	public static ArrayList<QuestionClassifierEvaluator> getRawInstances(String corpusDomain, String[] nluTechniques, String[] stopwordsFlags, String[] normalizeStringFlags, String[] posTaggerFlags, String corpusPropertiesPath){
		return getRawInstances(corpusDomain, nluTechniques, stopwordsFlags, normalizeStringFlags, posTaggerFlags, corpusPropertiesPath, 1);
	}

	public static ArrayList<QuestionClassifierEvaluator> getRawInstances(String corpusDomain, String[] nluTechniques, String[] stopwordsFlags, String[] normalizeStringFlags, String[] posTaggerFlags, String corpusPropertiesPath, int maxPredictions) {
		ArrayList<QuestionClassifierEvaluator> generatedQCE = new ArrayList<QuestionClassifierEvaluator>();
		for(String swFlag : stopwordsFlags){
			Config.stopwordsFlag = Boolean.parseBoolean(swFlag);

			for(String normalizeStringFlag : normalizeStringFlags){
				Config.normalizeStringFlag = Boolean.parseBoolean(normalizeStringFlag);

				for(String posTaggerFlag : posTaggerFlags){
					Config.posTaggerFlag = Boolean.parseBoolean(posTaggerFlag); 

					for(String tech : nluTechniques){
						if(tech.equals(QuestionEvaluatorSet.SVM.type())){
							SVMFactory svmFactory = new SVMFactory(corpusPropertiesPath);
							generatedQCE.addAll(svmFactory.getRawSVMQCE(corpusDomain, maxPredictions));
						}
						else if(tech.equals(QuestionEvaluatorSet.IW.type())){
							ConfigImportantWords.parseConfig();
							ImportantWordsFactory iwFactory = new ImportantWordsFactory(ConfigImportantWords.features, ConfigImportantWords.maxIW, corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(iwFactory.getRawImportantWordsQCE());
						}
						else if(tech.equals(QuestionEvaluatorSet.VSM.type())){
							ConfigVSM.parseConfig();
							VSMFactory vsmFactory = new VSMFactory(corpusPropertiesPath, maxPredictions, ConfigVSM.k_neighbours, ConfigVSM.features, ConfigVSM.utteranceWeight, ConfigVSM.freqCounters);
							generatedQCE.addAll(vsmFactory.getRawVSMQCE());
						}
						else if(tech.equals(QuestionEvaluatorSet.LSA.type())){
							ConfigLSA.parseConfig();
							LSAFactory lsaFactory = new LSAFactory(ConfigLSA.features, 
									ConfigLSA.vectorCalc,
									corpusPropertiesPath, 
									maxPredictions,
									ConfigLSA.maxFactors,
									ConfigLSA.featureInit,
									ConfigLSA.initialLearningRate,
									ConfigLSA.annealingRate,
									ConfigLSA.regularization,
									ConfigLSA.minImprovement,
									ConfigLSA.minEpochs,
									ConfigLSA.maxEpochs);
							generatedQCE.addAll(lsaFactory.getRawLSAQCE());
						}
						else if(tech.equals(QuestionEvaluatorSet.LR.type())){
							ConfigCrossEntropy.parseConfig();
							LogisticRegreessionFactory clmFactory = new LogisticRegreessionFactory(corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(clmFactory.getRawLogisticRegressionQCE());
						}
						else if(tech.equals(QuestionEvaluatorSet.CE.type())){
							ConfigCrossEntropy.parseConfig();
							CrossEntropyFactory clmFactory = new CrossEntropyFactory(ConfigCrossEntropy.features, corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(clmFactory.getRawCrossEntropyQCE());
						}
						else if(tech.equals(QuestionEvaluatorSet.CLM.type())){
							CLMFactory clmFactory = new CLMFactory(corpusPropertiesPath);
							generatedQCE.addAll(clmFactory.getRawCLMQCE(corpusDomain));
						}
						else if(tech.equals(QuestionEvaluatorSet.DICE.type())){
							ConfigDistance.parseConfig();
							DistanceFactory distanceFactory = new DistanceFactory(ConfigDistance.features, ConfigDistance.k_neighbours, ConfigDistance.setIntersectors, corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(distanceFactory.getRawDiceQCE());
						}
						else if(tech.equals(QuestionEvaluatorSet.JACCARD.type())){
							ConfigDistance.parseConfig();
							DistanceFactory distanceFactory = new DistanceFactory(ConfigDistance.features, ConfigDistance.k_neighbours, ConfigDistance.setIntersectors, corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(distanceFactory.getRawJaccardQCE());
						}
						else if(tech.equals(QuestionEvaluatorSet.JACCARDOVERLAP.type())){
							ConfigDistance.parseConfig();
							DistanceFactory distanceFactory = new DistanceFactory(ConfigDistance.features, ConfigDistance.k_neighbours, ConfigDistance.setIntersectors, corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(distanceFactory.getRawJaccardOverlapQCE());
						}
						else if(tech.equals(QuestionEvaluatorSet.JACCARDOVERLAPTFIDF.type())){
							ConfigDistance.parseConfig();
							DistanceFactory distanceFactory = new DistanceFactory(ConfigDistance.features, ConfigDistance.k_neighbours, ConfigDistance.setIntersectors, corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(distanceFactory.getRawJaccardOverlapTfidfQCE());
						}
						else if(tech.equals(QuestionEvaluatorSet.OVERLAP.type())){
							ConfigDistance.parseConfig();
							DistanceFactory distanceFactory = new DistanceFactory(ConfigDistance.features, ConfigDistance.k_neighbours, ConfigDistance.setIntersectors, corpusPropertiesPath, maxPredictions);
							generatedQCE.addAll(distanceFactory.getRawOverlapQCE());
						}
						else if(tech.equals(QuestionEvaluatorSet.FRAMESAF.type())){
							FramesClassifierFactory fafFactory = new FramesClassifierFactory();
							generatedQCE.addAll(fafFactory.getRawFramesAFQCE(corpusDomain, stopwordsFlags, normalizeStringFlags, posTaggerFlags, corpusPropertiesPath));
						}
						else if(tech.equals(QuestionEvaluatorSet.FRAMESFF.type())){
							FramesClassifierFactory fafFactory = new FramesClassifierFactory();
							generatedQCE.addAll(fafFactory.getRawFramesFFQCE(corpusDomain, stopwordsFlags, normalizeStringFlags, posTaggerFlags, corpusPropertiesPath));
						} 
					}
				}
			}
		}
		return generatedQCE;
	}

}
