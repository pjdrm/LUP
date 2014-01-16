package l2f.scripts.split.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import l2f.utils.ScriptUtils;

/**
 * @author Pedro Mota
 * Splits the corpus in a random way, according to a given percentage.
 */
public class RandomSplitCorpus {

	public void randomSplit(double testPercentage, String corpusPath) {
		System.out.println(corpusPath);
		int numberOfLines = ScriptUtils.countLines(corpusPath);
		int numberOfTestLines = (int)Math.round(numberOfLines*testPercentage);
		ArrayList<Integer> testLines = getRandomTestLines(numberOfTestLines, numberOfLines, corpusPath);

		try {
			FileOutputStream fosTest = new FileOutputStream(new File(corpusPath + ".test"));
			OutputStreamWriter oswTest = new OutputStreamWriter(fosTest, "UTF-8");
			boolean firstLineTestFlag = true;

			FileOutputStream fosTrain = new FileOutputStream(new File(corpusPath + ".train"));
			OutputStreamWriter oswTrain = new OutputStreamWriter(fosTrain, "UTF-8");
			boolean firstLineTrainFlag = true;

			Reader in = new InputStreamReader(new FileInputStream(corpusPath), "UTF-8");
			BufferedReader br = new BufferedReader(in);
			String str;
			int currentLine = 0;

			while ((str = br.readLine()) != null) {
				if(testLines.contains(currentLine)){
					if(firstLineTestFlag){
						oswTest.write(str);
						firstLineTestFlag = false;
					}
					else{
						oswTest.write("\n" + str);
					}
				}
				else{
					if(firstLineTrainFlag){
						oswTrain.write(str);
						firstLineTrainFlag = false;
					}
					else{
						oswTrain.write("\n" + str);
					}
				}
				currentLine++;
			}

			in.close();
			oswTest.close();
			oswTrain.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<Integer> getRandomTestLines(int numberOfTestLines, int totalOfLines, String filename) {
		ArrayList<Integer> testLines = new ArrayList<Integer>();
		int lineCounter = 0;
		Integer lineNumber = 0;
		Random randomGenerator = new Random();

		while(lineCounter < numberOfTestLines){
			lineNumber = randomGenerator.nextInt(totalOfLines);
			if(!testLines.contains(lineNumber)){
				testLines.add(lineNumber);
				lineCounter++;
			}
		}
		return testLines;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RandomSplitCorpus rsc = new RandomSplitCorpus();
		rsc.randomSplit(Double.parseDouble(args[0]), args[1]);

	}

}
