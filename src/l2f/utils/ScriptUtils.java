package l2f.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import l2f.config.Config;

/**
 * Common functions that facilitate the testing classes of the application.
 * @author Pedro Mota
 *
 */
public class ScriptUtils {

	/**
	 * Counts the lines of a file.
	 * @param filename Path to the file from which the lines are to be counted.
	 * @return
	 */
	public static int countLines(String filename) {
		try {
			InputStream is = Config.getInputStream(filename);
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			while ((readChars = is.read(c)) != -1) {
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n')
						++count;
				}
			}

			is.close();
			return count;

		} catch (IOException e) {
			System.err.println("Error closing/reading the InputStream for the corpus file! Msg: " + e.getLocalizedMessage());
			e.printStackTrace(System.err);
			return 0;
		}
	}

	/**
	 * Strips all question from a file where each line is in the format "question category".
	 * All categories must have a at least one number in the name at the end.
	 * @param testCorpusFile Path to file from which questions are to be extracted.
	 * @return
	 */
	public static ArrayList<String> getQuestionsFromFile(String testCorpusFile) {
		ArrayList<String> questions = new ArrayList<String>();

		try {
			Reader in = new InputStreamReader(new FileInputStream(testCorpusFile), "UTF-8");
			BufferedReader br = new BufferedReader(in);
			String str;
			Pattern p = Pattern.compile(".*[0-9] ");

			while ((str = br.readLine()) != null) {
				if(str.equals("\n"))
					continue;
				String[] items = p.split(str);
				questions.add(items[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return questions;
	}

	/**
	 * Deletes all files in directory specified in the argument.
	 * @param dirPath Path to the directory.
	 */
	public static void deleteAllfiles(String dirPath){
		File dir = new File(dirPath);
		for(String fileName : dir.list()){
			String filePath = dir.getAbsolutePath() + "//" + fileName;
			if(!new File(filePath).isDirectory())
				deleteFile(filePath);

		}
	}
	
	public static void deleteFile(String filePath){
		File f = new File(filePath);
		// Make sure the file or directory exists and isn't write protected
		if (!f.exists())
			throw new IllegalArgumentException(
					"Delete: no such file or directory: " + filePath);

		if (!f.canWrite())
			throw new IllegalArgumentException("Delete: write protected: "
					+ filePath);

		// If it is a directory, make sure it is empty
		if (f.isDirectory()) {
			String[] files = f.list();
			if (files.length > 0)
				throw new IllegalArgumentException(
						"Delete: directory not empty: " + filePath);
		}

		// Attempt to delete it
		boolean success = f.delete();
		if (!success)
			System.out.println("File " + filePath + " not deleted");
	}

	/**
	 * Copies a the content from a source file to a destination file.
	 * @param srFile
	 * @param dtFile
	 */
	public static void copyFile(String srFile, String dtFile){
		try{
			File f1 = new File(srFile);
			File f2 = new File(dtFile);
			InputStream in = new FileInputStream(f1);

			//For Append the file.
			//  OutputStream out = new FileOutputStream(f2,true);

			//For Overwrite the file.
			FileOutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			System.out.println("File copied.");
		}
		catch(FileNotFoundException ex){
			System.out.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		}
		catch(IOException e){
			System.out.println(e.getMessage());  
		}
//		try {
//			
//			FileReader fr = new FileReader(srFile);
//			BufferedReader br = new BufferedReader(fr);
//			
//			FileOutputStream fos = new FileOutputStream(new File(dtFile));
//			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
//			String str;
//			while((str = br.readLine()) != null){
//				osw.write(str);
//			}
//			osw.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * Returns all questions that belong the given category.
	 * @param questionCategory
	 * @return
	 */
	public static ArrayList<String> getQuestionsInTrainCorpus(String questionCategory){
		Config.parseConfig("./resources/qa/config/config_en.xml");
		ArrayList<String> result = new ArrayList<String>();
		try {
			Reader in;
			BufferedReader br;
			File trainDir = new File(Config.classification_trainDir);
			String str;
			String questionCat;

			for(String trainFile : trainDir.list()){
				in = new InputStreamReader(new FileInputStream(Config.classification_trainDir + trainFile), "UTF-8");
				br = new BufferedReader(in);
				while ((str = br.readLine()) != null) {
					if(str.equals("\n"))
						continue;
					questionCat = str.substring(0, str.indexOf(' ')).trim();
					if(questionCategory.equals(questionCat))
						result.add(str.substring(str.indexOf(' ')).trim());
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String getFileName(String filePath) {
		StringTokenizer strTokenizer = new StringTokenizer(filePath, "/");
		String fileName = "";
		String token;
		while(strTokenizer.hasMoreTokens()){
			token = strTokenizer.nextToken();
			if(!strTokenizer.hasMoreTokens())
				fileName = token;
		}
		return fileName;
	}
	
	public static String getDir(String filePath){
		StringTokenizer strTokenizer = new StringTokenizer(filePath, "/");
		String dirName = "./";
		String token;
		while(strTokenizer.hasMoreTokens()){
			token = strTokenizer.nextToken();
			if(strTokenizer.hasMoreTokens())
				dirName += token + "/";
		}
		return dirName;
	}
}
