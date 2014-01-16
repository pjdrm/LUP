package l2f.corpus.processor;

import pos.tagger.processor.POSProcessor;

public class POSTaggerModifier extends StringModifier {

	private POSProcessor posProcessor;
	
	public POSTaggerModifier(String taggerConfig, String taggerProcessorConfig){
		this.posProcessor = new POSProcessor(taggerConfig, taggerConfig);
	}
	
	@Override
	public String modify(String str) {
		return posProcessor.process(str);
	}

	@Override
	public String getDescription() {
		return "POSTagger";
	}

}
