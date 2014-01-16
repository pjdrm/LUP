package l2f.interpretation.classification.features;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import l2f.interpretation.AnalyzedQuestion;
import l2f.interpretation.InterpretedQuestion;

import com.aliasi.util.Counter;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToCounterMap;

/**
 * A FeatureExtractor used for testing purposes.
 * 
 */
public class TestFeatureExtractor implements FeatureExtractor<InterpretedQuestion>, Serializable {

    /**
	 * General serial version ID
	 */
	private static final long serialVersionUID = -8900164547667619728L;
	public final EnumSet<FeatureSet> activeFeatures;
	
	ArrayList<String> wlist = new ArrayList<String>();
	ArrayList<String> swords = null;

    public TestFeatureExtractor(EnumSet<FeatureSet> activeFeatures, ArrayList<String> iwl) {
        this.activeFeatures = activeFeatures;
        this.wlist.addAll(iwl);
    }
    
    public String toString(){
    	return activeFeatures.toString();
    }

    @Override
    public Map<String, Counter> features(InterpretedQuestion it) {
        AnalyzedQuestion in = it.getAnalyzedQuestion();
        ObjectToCounterMap<String> map = new ObjectToCounterMap<String>();
        List<String> tokens = in.getTokens();
        if (activeFeatures.contains(FeatureSet.BINARY_UNIGRAM)) {
            for (int i = 0; i < tokens.size(); i++) {
                if (!map.containsKey("#B#" + tokens.get(i))) {
                    map.increment("#B#" + tokens.get(i));
                }
            }
        }
        if (activeFeatures.contains(FeatureSet.UNIGRAM)) {
            for (int i = 0; i < tokens.size(); i++) {
                map.increment(tokens.get(i));
            }
        }
        if (activeFeatures.contains(FeatureSet.BINARY_BIGRAM)) {
            for (int i = 1; i < tokens.size(); i++) {
                if (!map.containsKey("#B#" + tokens.get(i - 1) + " " + tokens.get(i))) {
                    map.increment("#B#" + tokens.get(i - 1) + " " + tokens.get(i));
                }
            }
        }
        if (activeFeatures.contains(FeatureSet.BIGRAM)) {
            for (int i = 1; i < tokens.size(); i++) {
                map.increment(tokens.get(i - 1) + " " + tokens.get(i));
            }
        }
        if (activeFeatures.contains(FeatureSet.TRIGRAM)) {
            for (int i = 2; i < tokens.size(); i++) {
                map.increment(tokens.get(i - 2) + " " +  tokens.get(i - 1) + " " + tokens.get(i));
            }
        }
        if (activeFeatures.contains(FeatureSet.BINARY_TRIGRAM)) {
            for (int i = 2; i < tokens.size(); i++) {
                if (!map.containsKey("#B#" + tokens.get(i - 2) +  " " + tokens.get(i - 1) + " " + tokens.get(i))) {
                    map.increment("#B#" + tokens.get(i - 2) + " " +  tokens.get(i - 1) + " " + tokens.get(i));
                }
            }
        }

        if (activeFeatures.contains(FeatureSet.WORD_SHAPE)) {
            for (int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i).matches("^[a-z]+$")) {
                    map.increment("#F#LOWERCASED#");
                    continue;
                }
                if (tokens.get(i).matches("^[A-Z]+$")) {
                    map.increment("#F#UPPERCASED#");
                    continue;
                }
                if (tokens.get(i).matches("^[A-Z][a-zA-Z]+$")) {
                    map.increment("#F#CAPITALIZED#");
                    continue;
                }
                if (tokens.get(i).matches("^[a-zA-Z]+$")) {
                    map.increment("#F#MIXEDCASED#");
                    continue;
                }
                if (tokens.get(i).matches("^[0-9]+$")) {
                    map.increment("#F#DIGITSONLY#");
                    continue;
                }
                map.increment("#F#OTHERCASED#");
            }
        }

        if (activeFeatures.contains(FeatureSet.BINARY_WORD_SHAPE)) {
            for (int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i).matches("^[a-z]+$")) {
                    if (!map.containsKey("#B#LOWERCASED#")) {
                        map.increment("#B#LOWERCASED#");
                    }
                    continue;
                }
                if (tokens.get(i).matches("^[A-Z]+$")) {
                    if (!map.containsKey("#B#UPPERCASED#")) {
                        map.increment("#B#UPPERCASED#");
                    }
                    continue;
                }
                if (tokens.get(i).matches("^[A-Z][a-zA-Z]+$")) {
                    if (!map.containsKey("#B#CAPITALIZED#")) {
                        map.increment("#B#CAPITALIZED#");
                    }
                    continue;
                }
                if (tokens.get(i).matches("^[a-zA-Z]+$")) {
                    if (!map.containsKey("#B#MIXEDCASED#")) {
                        map.increment("#B#MIXEDCASED#");
                    }
                    continue;
                }

                if (tokens.get(i).matches("^[0-9]+$")) {
                    if (!map.containsKey("#B#DIGITSONLY#")) {
                        map.increment("#B#DIGITSONLY#");
                    }
                    continue;
                }
                if (!map.containsKey("#B#OTHERCASED#")) {
                    map.increment("#B#OTHERCASED#");
                }
            }
        }
        if (activeFeatures.contains(FeatureSet.LENGTH)) {
            map.increment("#LENGHT" + (tokens.size() < 6 ? "#S#" : "#L#"));
        }
        if (activeFeatures.contains(FeatureSet.POS)) {
            List<String> posTags = in.getPosTags();
            for (int i = 0; i < posTags.size(); i++) {
                map.increment(posTags.get(i));
            }
            /*for (int i = 1; i < posTags.size(); i++) {
            map.increment(posTags.get(i-1) + " " + posTags.get(i));
            }*/
        }
        if (activeFeatures.contains(FeatureSet.HEADWORD)) {
            map.increment("#HW#" + in.getHeadword());
            
        }
        if (activeFeatures.contains(FeatureSet.CATEGORY)) {         // WORDNET MAP
            map.increment("#WN#" + in.getHeadwordLexiconTarget());
            
        }
        
        //adds a new attribute, with prefix "iwlp_", for each important word in the list and if the word is present in the question
        //the corresponding increment is made (at most once)
        if (activeFeatures.contains(FeatureSet.IMPORTANT_WORDS_LIST_PREFFIX)) {
//      		this.parseIWordList();

        	for (int i = 0; i < tokens.size(); i++) {
				for(String word : wlist){
					if(word.split("\\s").length < 2 &&	tokens.get(i).startsWith(word) && map.getCount("iwlp_" + word) == 0)
						map.increment("iwlp_" + word);
				}
            }    	       
        }
        if (activeFeatures.contains(FeatureSet.IMPORTANT_WORDS_LIST_EXPR)) {
//      		this.parseIWordList();
      		
      		//unigrama em cada expressao da lista contida na pergunta
      		for(String word : wlist){
      			if(word.split("\\s").length >= 2 &&	
						in.getOriginalQuestion().replaceAll("\\s", "_").contains(word.replaceAll("\\s", "_"))){
      				if(map.getCount("iwle_" + word.replaceAll("\\s", "_")) == 0){
      					map.increment("iwle_" + word.replaceAll("\\s", "_"));
      				}
				}
      		}
        }
        if (activeFeatures.contains(FeatureSet.IMPORTANT_WORDS_LIST_OTHER)) {
//      		this.parseIWordList();
      		
      		//semantica menos correcta...mas melhor accuracy (isolado)
        	for (int i = 0; i < tokens.size(); i++) {
				for(String word : wlist){
					if(tokens.get(i).startsWith(word))
						map.increment(tokens.get(i));
					if(word.split("\\s").length >= 2 &&	
							in.getOriginalQuestion().replaceAll("\\s", "_").contains(word.replaceAll("\\s", "_"))){
						map.increment(tokens.get(i));
					}
				}
            }    	 
        }

//if (activeFeatures.contains(FeatureSet.NER_INCR)) {
//	Chunking chunking = recognizer.chunk(in.getOriginalQuestion());
//	for (Chunk chunk : chunking.chunkSet()) {
//		map.increment(chunk.type());
//	}   
//}
//if (activeFeatures.contains(FeatureSet.NER_REPL)) {
//	Chunking chunking = recognizer.chunk(in.getOriginalQuestion());
//	for (Chunk chunk : chunking.chunkSet()) {
//		map.increment(chunk.type());
//	}   
//}
        
        return map;
    }
    
   /* private void parseIWordList(){
    	if(wlist != null)
    		return;
    	
    	//parse iwordlist
    	try {
			BufferedReader in2 = new BufferedReader(new InputStreamReader(new FileInputStream(Config.iwordlist), "UTF-8"));
			String line;
			wlist = new ArrayList<String>();
			while((line = in2.readLine()) != null){
				if(line.trim().isEmpty())
					continue;
				wlist.add(NormalizerSimple.normPunctLCaseDMarks(line));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }*/
}
