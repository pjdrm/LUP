package l2f.corpus.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import l2f.config.Config;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.Utterance;
import l2f.corpus.parser.CorpusParser;
import l2f.corpus.parser.garbage.GarbageCorpusTransformer;
import l2f.nlp.NormalizerSimple;

import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.MapDictionary;

public class CorpusClassifierFactory {

	private static ArrayList<CorpusParser> cpArray;
	private static int remainingPartitionLinesIndex = 0;

	public CorpusClassifierFactory(ArrayList<CorpusParser> cp){
		cpArray = cp;
	}

	public CorpusClassifier parseCorpus(String corpusDirPath) {
		ArrayList<CorpusClassifier> corpora = new ArrayList<CorpusClassifier>();
		ArrayList<File> properties = new ArrayList<File>();
		boolean parsedFile = false;
		String corpusPropertiesPath = getCorpusPropertiesPath(corpusDirPath);
		try {
			File corpusDir = new File(corpusDirPath);
			for(File f : corpusDir.listFiles()){
				if(f.isDirectory() || f.getCanonicalPath().equals(corpusPropertiesPath))
					continue;

				for(CorpusParser cp : cpArray){
					if(cp.canProcessCorpus(f.getCanonicalPath())){
						corpora.add(cp.parseCorpus(f.getCanonicalPath(), corpusPropertiesPath));
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
			return createCorpus(corpora, properties, corpusPropertiesPath);
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getCorpusPropertiesPath(String corpusDirPath) {
		File corpusDir = new File(corpusDirPath);
		try {
			for(File f : corpusDir.listFiles()){
				if(f.isDirectory())
					continue;
				else if(f.getName().substring(f.getName().lastIndexOf(".")).equals(".properties")){
					return f.getCanonicalPath();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("ERROR: Please provide a properties file for " + corpusDirPath);
		System.exit(1);
		return null;
	}

	private static CorpusClassifier createCorpus(ArrayList<CorpusClassifier> corpora, ArrayList<File> properties, String corpusProperties) {
		ArrayList<Utterance> testCorpus = new ArrayList<Utterance>();
		ArrayList<Utterance> trainCorpus = new ArrayList<Utterance>();
		ArrayList<Utterance> answers = new ArrayList<Utterance>();
		for(CorpusClassifier corpus : corpora){
			testCorpus.addAll(corpus.getTestUtterances());
			trainCorpus.addAll(corpus.getTrainUtterances());
			answers.addAll(corpus.getAnswers());
		}

		ArrayList<String> iwl = new ArrayList<String>();
		Properties props = new Properties();

		for(File prop : properties){
			try {
				props.load(new FileInputStream(prop.getCanonicalPath()));
				String iwlPath = props.getProperty("iwl");
				if(iwlPath != null)
					iwl.addAll(loadIWL(iwlPath));
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		CorpusClassifier corpus = new CorpusClassifier(trainCorpus, testCorpus, answers, iwl, corpusProperties);

		if(Config.garbageCorpus)
			new GarbageCorpusTransformer(corpus).transformCorpus();

		//		addModifiers(corpus);
		//		corpus.processCorpus();
		return corpus;
	}

	/*private static void addModifiers(CorpusClassifier corpus) {
		if(Config.stopwordsFlag)
			corpus.addModifier(new StopWordModifier(Config.stopwordsFile));
		corpus.processCorpus();
	}*/

	private static ArrayList<String> loadIWL(String iwlPath) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(iwlPath), "UTF-8"));
			String line;
			ArrayList<String> wlist = new ArrayList<String>();
			while((line = in.readLine()) != null){
				if(line.trim().isEmpty())
					continue;
				wlist.add(NormalizerSimple.normPunctLCaseDMarks(line));
			}
			return wlist;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void loadNE(String nePath){
		double CHUNK_SCORE = 1.0;
		MapDictionary<String> DICTIONARY = new MapDictionary<String>();

		String line = new String();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(nePath), "UTF-8"));
			while ((line=reader.readLine())!=null) {
				String aux[] = line.split("\t");
				DICTIONARY.addEntry(new DictionaryEntry<String>(aux[1],aux[0],CHUNK_SCORE));
			}
			reader.close();

		} catch (IOException e) { }
	}

	public static List<CorpusClassifier> getCorpusClassifierPartitionsFromCVDir(String cvDir, String corpusDomain){
		List<CorpusClassifier> corpusClassifierList = new ArrayList<CorpusClassifier>();
		File runsDirs = new File(cvDir);
		for(String dir: runsDirs.list()){
			if(!dir.contains("run"))
				continue;
			String line;
			BufferedReader in;
			List<Utterance> trainCorpus = new ArrayList<Utterance>();
			List<Utterance> testCorpus = new ArrayList<Utterance>();
			ArrayList<Utterance> answers = new ArrayList<Utterance>();
			boolean testUtteranceLines = false;
			boolean trainUtteranceLines = false;
			boolean answerLines = false;
			try {
				File resultDirsFile = new File("./"+cvDir+"/"+dir);
				String individualResultDir = resultDirsFile.list()[0];
				in = new BufferedReader(new InputStreamReader(new FileInputStream("./"+cvDir+"/"+dir+"/"+individualResultDir+"/Corpus.txt"), "UTF-8"));
				while((line = in.readLine()) != null){
					if(line.equals("Test:")){
						testUtteranceLines = true;
						continue;
					}
					if(line.equals("Train:")){
						trainUtteranceLines = true;
						continue;
					}
					if(line.equals("Answers")){
						answerLines = true;
						continue;
					}

					if(testUtteranceLines){
						if(line.length() == 0)
							testUtteranceLines = false;
						else{
							int index = line.indexOf(" ");
							String cat = line.substring(0, index);
							String question = line.substring(index+1);
							testCorpus.add(new Utterance(cat, question));
						}

					}

					if(trainUtteranceLines){
						if(line.length() == 0)
							trainUtteranceLines = false;
						else{
							int index = line.indexOf(" ");
							String cat = line.substring(0, index);
							String question = line.substring(index+1);
							trainCorpus.add(new Utterance(cat, question));
						}
					}

					if(answerLines){
						if(line.length() == 0)
							break;
						int index = line.indexOf(" ");
						String cat = line.substring(0, index);
						String question = line.substring(index+1);
						answers.add(new Utterance(cat, question));
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			CorpusClassifier part = new CorpusClassifier(getCorpusPropertiesPath(corpusDomain));
			if(answers.size() > 0){
				part.setAnswers(answers);
				part.createAnswersMap();
			}

			part.setTestUtterances(testCorpus);
			part.setTrainUtterances(trainCorpus);
			corpusClassifierList.add(part);
		}
		return corpusClassifierList;
	}

	public List<CorpusClassifier> getCorpusClassifierPartitions(String corpusDirPath, int nPartitions) {
		CorpusClassifier cc = parseCorpus(corpusDirPath);
		ArrayList<Utterance> answers = cc.getAnswers();

		List<Utterance> utteranceCorpus = new ArrayList<Utterance>();
		List<Utterance> trainCorpus = new ArrayList<Utterance>();
		List<Utterance> testCorpus = new ArrayList<Utterance>();
		for(Utterance testUt : cc.getTestUtterances()){
			if(!testUt.getCat().equals("dummy"))
				utteranceCorpus.add(testUt);
		}
		utteranceCorpus.addAll(cc.getTrainUtterances());

		List<List<Utterance>> partitions = getCorpusPartitions(nPartitions, utteranceCorpus);
		List<CorpusClassifier> returnPartitions = new ArrayList<CorpusClassifier>();
		int partIndex = 0;
		int i = 0;
		while(partIndex < nPartitions){
			CorpusClassifier part = new CorpusClassifier(getCorpusPropertiesPath(corpusDirPath));
			part.setIWL(cc.getIWL());
			while(i < nPartitions){
				if(i == partIndex)
					testCorpus.addAll(copyUtterances(partitions.get(i)));
				else
					trainCorpus.addAll(copyUtterances(partitions.get(i)));
				i++;
			}

			if(answers.size() > 0){
				part.setAnswers(answers);
				part.createAnswersMap();
			}

			part.setTestUtterances(testCorpus);
			part.setTrainUtterances(trainCorpus);

			returnPartitions.add(part);
			i = 0;
			partIndex++;
			trainCorpus = new ArrayList<Utterance>();
			testCorpus = new ArrayList<Utterance>();
		}

		/*ArrayList<ArrayList<Utterance>> utterancePartitions = new ArrayList<ArrayList<Utterance>>();
		int i = 0;
		while(i < nPartitions){
			utterancePartitions.add(new ArrayList<Utterance>());
			i++;
		}
		ArrayList<Utterance> utteranceCorpus = new ArrayList<Utterance>();
		utteranceCorpus.addAll(cc.getTestUtterances());
		utteranceCorpus.addAll(cc.getTrainUtterances());
		ArrayList<CorpusClassifier> partitions = new ArrayList<CorpusClassifier>();

		int totalOfLines = cc.getTestUtterances().size() + cc.getTrainUtterances().size();
		int linesOfPartition = totalOfLines / nPartitions;
		List<List<Integer>> partionDistribution = distributePartionLines(linesOfPartition, nPartitions, totalOfLines);
		int partIndex = 0;
		for(List<Integer> lineDist : partionDistribution){
			for(Integer lineNumber : lineDist){
				Utterance u = utteranceCorpus.get(lineNumber);
				utterancePartitions.get(partIndex).add(new Utterance(u.getCat(), u.getUtterance()));
			}
			partIndex++;
		}

		partIndex = 0;
		i = 0;
		ArrayList<Utterance> train = new ArrayList<Utterance>();
		while(partIndex < nPartitions){
			CorpusClassifier part = new CorpusClassifier();
			part.setIWL(cc.getIWL());
			part.setNE(cc.getNE());
			for(ArrayList<Utterance> up: utterancePartitions){
				if(i == partIndex){
					part.setTestUtterances(up);
				}
				else{
					train.addAll(up);
				}
				i++;
			}
			part.setTrainUtterances(train);
			if(answers.size() > 0){
				part.setAnswers(answers);
				part.createAnswersMap();
			}
			partitions.add(part);
			i = 0;
			partIndex++;
			train = new ArrayList<Utterance>();
		}*/
		return returnPartitions;
	}

	private List<Utterance> copyUtterances(List<Utterance> utterances) {
		List<Utterance> ret = new ArrayList<Utterance>();
		for(Utterance ut : utterances){
			ret.add(new Utterance(ut.getCat(), ut.getUtterance()));
		}

		return ret;
	}

	public List<List<Utterance>> getCorpusPartitions(int numberOfPartitions, List<Utterance> corpus){
		List<List<Utterance>> partitions = new ArrayList<List<Utterance>>();
		int i = 0;
		while(i < numberOfPartitions){
			partitions.add(new ArrayList<Utterance>());
			i++;
		}
		Map<String, List<Utterance>> utteranceMap = new HashMap<String, List<Utterance>>();
		List<Utterance> utteranceList;
		for(Utterance ut : corpus){
			if(utteranceMap.get(ut.getCat()) == null){
				utteranceList = new ArrayList<Utterance>();
				utteranceList.add(ut);
				utteranceMap.put(ut.getCat(), utteranceList);
			}
			else
				utteranceMap.get(ut.getCat()).add(ut);
		}

		List<List<Integer>> partitionsLineDistributions;
		List<Utterance> currentUtterances;
		for(String cat : utteranceMap.keySet()){
			currentUtterances = utteranceMap.get(cat);
			partitionsLineDistributions = distributePartionLines((int)Math.round(currentUtterances.size()/numberOfPartitions), numberOfPartitions, currentUtterances.size());
			i = 0;
			for(List<Integer> partitionsLineDistribution : partitionsLineDistributions){
				for(Integer line : partitionsLineDistribution){
					partitions.get(i).add(currentUtterances.get(line));
				}
				i++;
			}
		}
		return partitions;

	}

	public static List<List<Integer>> distributePartionLines(int linesOfPartition, int numberOfPartitions, int numberOfLines) {
		List<List<Integer>> partions = new ArrayList<List<Integer>>();
		int i = 0;
		Random randomGenerator = new Random();
		int randPartionIndex = randomGenerator.nextInt(numberOfPartitions);

		while(i < numberOfPartitions){
			partions.add(new ArrayList<Integer>());
			i++;
		}

		i = 0;
		while(i < numberOfPartitions * linesOfPartition){
			while(true){
				if(partions.get(randPartionIndex).size() < linesOfPartition){
					partions.get(randPartionIndex).add(i);
					break;
				}
				else{
					randPartionIndex = (randPartionIndex + 1) % numberOfPartitions;
				}
			}
			randPartionIndex = randomGenerator.nextInt(numberOfPartitions);
			i++;
		}

		while(i < numberOfLines){
			partions.get(remainingPartitionLinesIndex).add(i);
			remainingPartitionLinesIndex = (remainingPartitionLinesIndex + 1) % numberOfPartitions;
			i++;
		}

		return  partions;
	}

}