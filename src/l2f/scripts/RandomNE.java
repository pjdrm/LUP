package l2f.scripts;

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
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

import l2f.config.Config;

public class RandomNE {
	public static void main(String[] args){
		try {
			Config.parseConfig("./resources/qa/config/config_en.xml");
			HashMap<String, ArrayList<String>> neMap = new HashMap<String, ArrayList<String>>();
			Reader in = new InputStreamReader(new FileInputStream(args[0]), "UTF-8");
			BufferedReader br = new BufferedReader(in);
			String str = "";
			String entCat = "";
			String entVal = "";
			StringTokenizer strToken;
			while((str = br.readLine()) != null){
				strToken = new StringTokenizer(str, "\t");
				entCat = strToken.nextToken();
				entVal = strToken.nextToken();
				if(neMap.get(entCat) == null){
					ArrayList<String> array = new ArrayList<String>();
					array.add(entVal);
					neMap.put(entCat, array);
				}
				else{
					neMap.get(entCat).add(entVal);
				}
			}
			
			in = new InputStreamReader(new FileInputStream("corpus.txt"), "UTF-8");
			br = new BufferedReader(in);
			FileOutputStream fosTrain = new FileOutputStream("questions.dat");
			OutputStreamWriter oswTrain = new OutputStreamWriter(fosTrain, "UTF-8");
			boolean firstLine = true;
			while((str = br.readLine()) != null){
				for(String key : neMap.keySet()){
					if(str.contains(key)){
						int i = neMap.get(key).size() - 1;
						if(i <= 0){
							i = 1;
						}
						Random randomGenerator = new Random();
						str = str.replaceAll(key, neMap.get(key).get(randomGenerator.nextInt(i))); 
					}
				}
				if(firstLine){
					oswTrain.write(str);
					firstLine = false;
				}
				else
					oswTrain.write("\n" + str);
			}
			br.close();
			oswTrain.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
