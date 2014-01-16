package l2f.out.of.domain;

import java.util.ArrayList;
import java.util.List;

import l2f.config.Config;
import l2f.config.ConfigOutOfDomain;
import l2f.evaluator.QCEFactory;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.evaluator.clm.QuestionClassifierEvaluatorCLM;
import l2f.evaluator.distance.algorithms.DistanceFactory;
import l2f.evaluator.distance.algorithms.QuestionClassifierEvaluatorDistance;
import l2f.out.of.domain.clm.OutOfDomainEvaluatorCLM;
import l2f.out.of.domain.threshold.OutOfDomainEvaluatorAverageGeneral;
import l2f.out.of.domain.threshold.OutOfDomainEvaluatorFixedThreshold;
import l2f.out.of.domain.threshold.OutOfDomainEvaluatorWorstBest;
import l2f.out.of.domain.threshold.OutOfDomainEvaluatorAverage;
import l2f.out.of.domain.threshold.OutOfDomainEvaluatorWorstWorst;

public class OutOfDomainEvaluatorFactory {

	public static List<OutOfDomainEvaluator> getInstances(String corpusDomain, String[] outOfDomainTechniques, String[] stopwordsFlags, String[] normalizeStringFlags, String[] posTaggerFlags, String corpusPropertiesPath){
		List<QuestionClassifierEvaluator> qceList;
		List<OutOfDomainEvaluator> generatedOutOfDomainQCE = new ArrayList<OutOfDomainEvaluator>();
		OutOfDomainEvaluator outOfDomainQCE = null;
		ConfigOutOfDomain.parseConfig();
		for(String stopwordsFlag : stopwordsFlags){
			Config.stopwordsFlag = Boolean.parseBoolean(stopwordsFlag);

			for(String normalizeStringFlag : stopwordsFlags){
				Config.normalizeStringFlag = Boolean.parseBoolean(normalizeStringFlag);

				for(String posTaggerFlag: posTaggerFlags){
					Config.posTaggerFlag = Boolean.parseBoolean(posTaggerFlag);

					//Major HACK!!!
					//should behave like old version
					DistanceFactory distanceFactory = new DistanceFactory(ConfigOutOfDomain.features, "1", "regular", corpusPropertiesPath);

					for(String oodTech : outOfDomainTechniques){
						qceList = new ArrayList<QuestionClassifierEvaluator>();
						if(oodTech.equals(OutOfDomainSet.AVERAGE.type()) || oodTech.equals(OutOfDomainSet.AVERAGEGENERAL.type()) || 
								oodTech.equals(OutOfDomainSet.WORSTBEST.type()) || oodTech.equals(OutOfDomainSet.WORSTWORST.type()) || 
								oodTech.equals(OutOfDomainSet.FIXEDTHRESHOLD.type())){
							String distanceAlgs = ConfigOutOfDomain.distanceAlgs;
							distanceAlgs = distanceAlgs.replaceAll(" ", "");
							String[] distanceAlgsArray = distanceAlgs.split(",");
							for(String tech : distanceAlgsArray){
								if(tech.equals(QuestionEvaluatorSet.DICE.type())){
									qceList.addAll(distanceFactory.getDiceQCE(corpusDomain));
								}
								else if(tech.equals(QuestionEvaluatorSet.JACCARD.type())){
									qceList.addAll(distanceFactory.getJaccardQCE(corpusDomain));
								}
								else if(tech.equals(QuestionEvaluatorSet.JACCARDOVERLAP.type())){
									qceList.addAll(distanceFactory.getJaccardOverlapQCE(corpusDomain));
								}
								else if(tech.equals(QuestionEvaluatorSet.JACCARDOVERLAPTFIDF.type())){
									qceList.addAll(distanceFactory.getJaccardOverlapTfidfQCE(corpusDomain));
								}
								else if(tech.equals(QuestionEvaluatorSet.OVERLAP.type())){
									qceList.addAll(distanceFactory.getOverlapQCE(corpusDomain));
								}
								else{
									System.err.println(tech + " is not a distance algorithm");
									System.exit(1);
								}
							}
							for(QuestionClassifierEvaluator qce : qceList){
								if(oodTech.equals(OutOfDomainSet.AVERAGE.type()))
									outOfDomainQCE = new OutOfDomainEvaluatorAverage((QuestionClassifierEvaluatorDistance) qce);
								else if(oodTech.equals(OutOfDomainSet.AVERAGEGENERAL.type()))
									outOfDomainQCE = new OutOfDomainEvaluatorAverageGeneral((QuestionClassifierEvaluatorDistance) qce);
								else if(oodTech.equals(OutOfDomainSet.WORSTBEST.type()))
									outOfDomainQCE = new OutOfDomainEvaluatorWorstBest((QuestionClassifierEvaluatorDistance) qce);
								else if(oodTech.equals(OutOfDomainSet.WORSTWORST.type()))
									outOfDomainQCE = new OutOfDomainEvaluatorWorstWorst((QuestionClassifierEvaluatorDistance) qce);
								else if(oodTech.equals(OutOfDomainSet.FIXEDTHRESHOLD.type())){
									String threholds = ConfigOutOfDomain.fixedThreshold.replaceAll(" ", "");
									String[] thresholdArray = threholds.split(",");
									for(String threshold : thresholdArray){
										outOfDomainQCE = new OutOfDomainEvaluatorFixedThreshold((QuestionClassifierEvaluatorDistance) qce, Double.parseDouble(threshold));
										outOfDomainQCE.run();
										generatedOutOfDomainQCE.add(outOfDomainQCE);
									}
									continue;
								}

								outOfDomainQCE.run();
								generatedOutOfDomainQCE.add(outOfDomainQCE);
							}
						}
						else if(oodTech.equals(OutOfDomainSet.CLM.type())){
							outOfDomainQCE = new OutOfDomainEvaluatorCLM((QuestionClassifierEvaluatorCLM) QCEFactory.getInstances(corpusDomain, new String[]{OutOfDomainSet.CLM.type()}, new String[]{stopwordsFlag}, new String[]{normalizeStringFlag}, new String[]{posTaggerFlag}, corpusPropertiesPath).get(0));
//							outOfDomainQCE.run();
							generatedOutOfDomainQCE.add(outOfDomainQCE);				
						}
					}
				}
			}
		}
		return generatedOutOfDomainQCE;
	}
}
