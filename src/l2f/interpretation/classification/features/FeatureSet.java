package l2f.interpretation.classification.features;

import l2f.evaluator.arguments.QuestionEvaluatorSet;

public enum FeatureSet {

    //Frequency features
	/**
	 * Trains with unigrams
	 */
    UNIGRAM("u"),
    
    /**
     * Trains with bigrams
     */
    BIGRAM("b"),
    
    /**
     * Trains with trigrams
     */
    TRIGRAM("t"),
    
    FOURGRAM("f"),
    FIVEGRAM("fi"),
    SIXGRAM("s"),
    SEVENGRAM("se"),
    
    LENGTH("l"),
    POS("p"),
    
    NER_REPL("nr"),
    NER_INCR("ni"),
    
    /**
     * Trains with questions headwords
     */
    HEADWORD("h"),
    
    /**
     * Trains with questions categories
     */
    CATEGORY("c"),
    
    WORD_SHAPE("x"),
    
    /**
     * Trains with IWL Preffixes
     */
    IMPORTANT_WORDS_LIST_PREFFIX("iwlp"),
    
    /**
     * Trains with IWL, list can contain multiple word entries
     */
    IMPORTANT_WORDS_LIST_EXPR("iwle"),
    
    /**
     * Trains with IWL_EXPR (see IMPORTANT_WORDS_LIST_EXPR), giving more height to IWL words/expressions
     */
    IMPORTANT_WORDS_LIST_OTHER("iwlo"),
    
    /**
     * Discards stopwords
     */
    STOPWORDS("s"),

    //Binary features: either exist or not in each sample
    BINARY_UNIGRAM("bu"),
    BINARY_BIGRAM("bb"),
    BINARY_TRIGRAM("bt"),
    BINARY_POS("bp"),
    BINARY_NER_REPL("bner"),
    BINARY_NER_INCR("bneri"),
    BINARY_WORD_SHAPE("bx"),

    //My dummy feature
    DUMMY("--DUMMY--");

    /** word unigrams
     * word bigrams
     * word trigrams
     * word unigrams with n-first threshold
     * number of tokens in the question

     * part-of-speech tags
     * named entities (replace)
     * named entities (incremental)

     * question focus
     * target synset of question focus (+ adjective attribute)
     * automatically extracted semantically important words
     * semantically related words of Li&Roth
     * 
     * stopword removal
     * */
    private final String shortName;
    
    private FeatureSet(String shortName) {
    	this.shortName = shortName;
    }

	public String getShortName() {
		return shortName;
	}
}
