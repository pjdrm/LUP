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
 * Make partitions of the corpus in a random way.
 */
public class RandomCorpusPartitions {

	public void randomPartition(int numberOfPartitions, String corpusPath, String destDir){
		int totalOfLines = ScriptUtils.countLines(corpusPath);
		int linesOfPartition = totalOfLines / numberOfPartitions;
		int remainingLines = totalOfLines % numberOfPartitions;
		ArrayList<ArrayList<Integer>> partionsLineDistribution = distributePartionLines(linesOfPartition, numberOfPartitions, totalOfLines - remainingLines);
		ArrayList<Boolean> firstLineFlags = new ArrayList<Boolean>();
		


		try {
			int i = 0;
			ArrayList<OutputStreamWriter> partitionsOut = new ArrayList<OutputStreamWriter>();

			while(i < numberOfPartitions){
				OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File(destDir + "\\partion.p" + i)), "UTF-8");
				partitionsOut.add(osw);
				firstLineFlags.add(true);
				i++;
			}

			Reader in = new InputStreamReader(new FileInputStream(corpusPath), "UTF-8");
			BufferedReader br = new BufferedReader(in);
			String str;
			int currentLine = 0;
			boolean remainingLinesFlag = false;

			while ((str = br.readLine()) != null) {
				int partitionIndex = 0;
				if(!remainingLinesFlag){
					for(ArrayList<Integer> partionLines : partionsLineDistribution){
						if(partionLines.contains(currentLine)){
							if(firstLineFlags.get(partitionIndex)){
								partitionsOut.get(partitionIndex).write(str);
								firstLineFlags.set(partitionIndex, false);
							}
							else{
								partitionsOut.get(partitionIndex).write("\n" + str);
							}
							break;
						}
						partitionIndex++;
					}
					if(partitionIndex == numberOfPartitions){
						partitionsOut.get(partitionIndex - 1).write("\n" + str);
						remainingLinesFlag = true;
					}
				}
				else{
					partitionsOut.get(partitionIndex).write("\n" + str);
					partitionIndex++;
				}
				currentLine++;
			}

			in.close();
			for(OutputStreamWriter osw : partitionsOut){
				osw.close();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<ArrayList<Integer>> distributePartionLines(int linesOfPartition, int numberOfPartitions, int numberOfLines) {
		ArrayList<ArrayList<Integer>> partions = new ArrayList<ArrayList<Integer>>();
		int i = 0;
		Random randomGenerator = new Random();
		int randPartionIndex = randomGenerator.nextInt(numberOfPartitions);
		
		while(i < numberOfPartitions){
			partions.add(new ArrayList<Integer>());
			i++;
		}
		
		i = 0;
		while(i < numberOfLines){
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
		
		return  partions;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RandomCorpusPartitions rcp = new RandomCorpusPartitions();
		rcp.randomPartition(Integer.parseInt(args[0]), args[1], args[2]);
	}

}
