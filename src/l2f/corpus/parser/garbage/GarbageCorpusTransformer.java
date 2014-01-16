package l2f.corpus.parser.garbage;

import java.util.List;
import java.util.StringTokenizer;

import l2f.ClassifierApp;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.Utterance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GarbageCorpusTransformer{

	private static final Logger logger = LoggerFactory.getLogger(ClassifierApp.class);
	private CorpusClassifier corpus;

	public GarbageCorpusTransformer(CorpusClassifier corpus){
		this.corpus = corpus;
	}
	
	public CorpusClassifier getCorpus(){
		return corpus;
	}
	
	public void transformCorpus() {
		if(getCorpus().getIWL().size() == 0){
			logger.debug("System exited: No IWL was provided");
			System.exit(1);
		}
		garbageUtterances(getCorpus().getTrainUtterances());
		garbageUtterances(getCorpus().getTestUtterances());

	}

	public void garbageUtterances(List<Utterance> utterances){
		StringTokenizer strTokenizer;
		String token;
		String utterance = "";
		for(Utterance ut : utterances){
			strTokenizer = new StringTokenizer(ut.getUtterance(), " ");
			token = "";
			while(strTokenizer.hasMoreTokens()){
				token = strTokenizer.nextToken();
					for(String iw : getCorpus().getIWL()){
						if(token.contains(iw)){
							utterance += " " + token;
							break;
						}
					}
			}
			ut.setUtterance(utterance);
			utterance = "";
		}
	}

}
