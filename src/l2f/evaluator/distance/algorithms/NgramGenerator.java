package l2f.evaluator.distance.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import l2f.interpretation.classification.features.FeatureSet;

public class NgramGenerator {
	public static List<String> getNGrams(int nGramsOrder, List<String> words){
		words.add(0, "<s>");
		words.add("</s>");
		if(nGramsOrder == 1 || nGramsOrder > words.size())
			return words;
		
		ArrayList<String> nGrams = new ArrayList<String>();
		String nGram = "";
		int wordIndex = 0;
		for(String w : words){
			if(wordIndex + nGramsOrder == words.size() + 1)
				break;
			
			nGram = w;
			for(int i = 1; i < nGramsOrder; i++){
				nGram += " " + words.get(wordIndex + i);
			}
			nGrams.add(nGram);
			nGram = "";
			wordIndex++;
		}
		return nGrams;
	}
	
	public static List<Integer> getNgramOrder(String strNgram){
		List<Integer> nGrams = new ArrayList<Integer>();
		StringTokenizer strTokenizer = new StringTokenizer(strNgram, ",");
		String feature;
		while(strTokenizer.hasMoreTokens()){
			feature = strTokenizer.nextToken();
			feature = feature.replaceAll("-", "");
			feature = feature.replaceAll(" ", "");
			if(feature.equals(FeatureSet.BIGRAM.getShortName()))
				nGrams.add(2);
			else if(feature.equals(FeatureSet.TRIGRAM.getShortName()))
				nGrams.add(3);
			else if(feature.equals(FeatureSet.UNIGRAM.getShortName()))
				nGrams.add(1);
			else if(feature.equals(FeatureSet.FOURGRAM.getShortName()))
				nGrams.add(4);
			else if(feature.equals(FeatureSet.FIVEGRAM.getShortName()))
				nGrams.add(5);
			else if(feature.equals(FeatureSet.SIXGRAM.getShortName()))
				nGrams.add(6);
			else if(feature.equals(FeatureSet.SEVENGRAM.getShortName()))
				nGrams.add(7);
			else{
				System.err.println("ERROR:\nInvalid feature " + feature);
				System.exit(1);
			}
		}
		return nGrams;
	}
}
