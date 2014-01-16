package l2f.corpus.factory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import l2f.corpus.CorpusFrameClassifier;
import l2f.corpus.parser.CorpusFrameParser;
import l2f.evaluator.frames.FrameQuestion;

public class CorpusFrameClassifierFactory {
	private ArrayList<CorpusFrameParser> frameParsers = new ArrayList<CorpusFrameParser>();
	
	public CorpusFrameClassifierFactory(ArrayList<CorpusFrameParser> frameParsers){
		this.frameParsers = frameParsers;
	}
	
	public CorpusFrameClassifier parseFrameCorpus(String corpusDirPath) {
		CorpusFrameClassifier cfc = null;
		boolean parsedFile = false;
		try {
			File corpusDir = new File(corpusDirPath);
			for(File f : corpusDir.listFiles()){
				if(f.isDirectory() || f.getName().contains(".properties"))
					continue;
				for(CorpusFrameParser cp : frameParsers){
					if(cp.canProcessCorpus(f.getCanonicalPath())){
						cfc = cp.parseCorpus(f.getCanonicalPath());
						parsedFile = true;
						break;
					}
				}
				if(!parsedFile){
					System.err.println("ERROR:\n The structure of file " + f.getPath() + " is not supported.");
					System.exit(1);
				}
				parsedFile = false;
			}
			return cfc;
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<CorpusFrameClassifier> getCorpusFrameClassifierPartitions(String corpusDirPath, int nPartitions) {
		CorpusFrameClassifier cc = parseFrameCorpus(corpusDirPath);
		ArrayList<ArrayList<FrameQuestion>> frameQuestionPartitions = new ArrayList<ArrayList<FrameQuestion>>();
		int i = 0;
		while(i < nPartitions){
			frameQuestionPartitions.add(new ArrayList<FrameQuestion>());
			i++;
		}
		ArrayList<FrameQuestion> frameQuestionCorpus = new ArrayList<FrameQuestion>();
		frameQuestionCorpus.addAll(cc.getTestFrameQuestions());
		frameQuestionCorpus.addAll(cc.getTrainFrameQuestions());
		ArrayList<CorpusFrameClassifier> partitions = new ArrayList<CorpusFrameClassifier>();

		int totalOfLines = cc.getTestFrameQuestions().size() + cc.getTrainFrameQuestions().size();
		int linesOfPartition = totalOfLines / nPartitions;
		List<List<Integer>> partionDistribution = CorpusClassifierFactory.distributePartionLines(linesOfPartition, nPartitions, totalOfLines);
		int partIndex = 0;
		for(List<Integer> lineDist : partionDistribution){
			for(Integer lineNumber : lineDist){
				FrameQuestion fq = frameQuestionCorpus.get(lineNumber);
				frameQuestionPartitions.get(partIndex).add(new FrameQuestion(fq.getQuestion(), fq.getFrameCat(), fq.getSlotValues()));
			}
			partIndex++;
		}

		partIndex = 0;
		i = 0;
		ArrayList<FrameQuestion> train = new ArrayList<FrameQuestion>();
		while(partIndex < nPartitions){
			CorpusFrameClassifier part = new CorpusFrameClassifier(cc);
			for(ArrayList<FrameQuestion> fqPart: frameQuestionPartitions){
				if(i == partIndex){
					part.setTestFrameQuestions(fqPart);
				}
				else{
					train.addAll(fqPart);
				}
				i++;
			}
			part.setTrainFrameQuestions(train);
			partitions.add(part);
			i = 0;
			partIndex++;
			train = new ArrayList<FrameQuestion>();
		}
		return partitions;
	}
}
