package l2f.corpus.parser.clm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import l2f.config.ConfigCLM;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.Utterance;
import l2f.utils.ScriptUtils;

public class CLMParser implements Serializable{

	private int questionID = 0;
	private int answerID = 0;
	private HashMap<String, Integer> answersIDMap = new HashMap<String, Integer>();
	private String questions = "";
	private String answers = "";
	private String qaMap = "";
	private String agent;
	private String plistFilePath;
	private static int defaultAnswerId = 0;
	
	public CLMParser(String agent, String plistFilePath){
		this.agent = agent;
		this.plistFilePath = plistFilePath;
	}

	public  void parseCorpus(CorpusClassifier cc) {
		File prevPlist = new File(plistFilePath);
		if(prevPlist.exists())
			ScriptUtils.deleteFile(plistFilePath);
		
		try {

			for(Utterance ut : cc.getTrainUtterances()){
				questions += generateDictQuestion(ut);
				qaMap += generateMapping(ut, cc);
				questions += "\n";
			}

			questions = questions.substring(0, questions.length() - 1);
			qaMap = qaMap.substring(0, qaMap.length() - 1);
			answers = answers.substring(0, answers.length() - 1);

			String firstName;
			String lastName = "Default";
			String[] name = agent.split(" ");
			if(name.length == 2)
				lastName = name[1];
			firstName = name[0];

			BufferedReader br = new BufferedReader(new FileReader(new File(ConfigCLM.plistFileEmptyPath)));
			String input = "";
			String outPut = "";

			while((input = br.readLine()) != null){
				outPut += input + "\n";
				if(input.equals("<key>answers</key>")){
					input = br.readLine() + "\n";
					outPut += input + answers + "\n";
				}

				if(input.equals("<key>questions</key>")){
					outPut += br.readLine() + "\n";
					outPut += questions + "\n";
					outPut += br.readLine() + "\n";

					outPut += "<key>speakers</key>\n<array>\n<dict>\n<key>lastName</key>\n<string>" + lastName + "</string>\n";
					outPut += "<key>class</key>\n<string>edu.usc.ict.npc.editor.model.Person</string>\n";
					outPut += "<key>firstName</key>\n<string>" + firstName + "</string>\n</dict>\n</array>\n";
				}

				if(input.equals("<key>map</key>")){
					input = br.readLine() + "\n";
					outPut += input + qaMap + "\n";
				}

				if(input.equals("<string>Toss</string>")){
					outPut += br.readLine() + "\n" + br.readLine() + "\n" + br.readLine() + "\n" + br.readLine() + "\n";
					outPut += "<dict>\n<key>description</key>\n<string></string>\n<key>name</key>\n";
					outPut += "<string>" + agent + "</string>\n<key>ID</key>\n";
					outPut += "<string>" + agent + "</string>\n<key>colorAsInt</key>\n<integer>0</integer>\n</dict>\n";
				}



				if(input.equals("<key>questionColumnVisible</key>")){
					String str = br.readLine() + "\n" + br.readLine() + "\n" + br.readLine() + "\n";
					if(str.equals("<false/>\n</dict>\n</array>\n")){
						outPut += "<false/>\n</dict>\n";
						outPut += "<dict>\n<key>chatEditingAllowed</key>\n<false/>\n<key>answerCategory</key>\n<true/>\n";
						outPut += "<key>readOnly</key>\n<false/>\n<key>classifierCategory</key>\n<false/>\n<key>name</key>\n";
						outPut += "<string>Speaker</string>\n<key>ID</key>\n<string>speaker</string>\n";
						outPut += "<key>tokens</key>\n<array>\n<dict>\n";
						outPut += "<key>name</key>\n" + "<string>" + firstName + "</string>\n";
						outPut += "<key>ID</key>\n<string>" + firstName + "</string>\n";
						outPut += "<key>colorAsInt</key>\n<integer>-1</integer>\n</dict>\n</array>\n";
						outPut += "<key>answerEditingAllowed</key>\n<true/>\n<key>questionCategory</key>\n<false/>\n<key>questionEditingAllowed</key>\n";
						outPut += "<true/>\n<key>answerColumnVisible</key>\n<true/>\n<key>questionColumnVisible</key>\n<true/>\n</dict>\n</array>\n";
					}
					else{
						outPut += str;
					}
				}
			}
			br.close();

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(plistFilePath), "UTF-8"));
			bw.write(outPut);
			bw.close();
			
			resetParser();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void resetParser(){
		questionID = 0;
		answerID = 0;
		answersIDMap = new HashMap<String, Integer>();
		questions = "";
		answers = "";
		qaMap = "";
	}

	private String generateMapping(Utterance ut, CorpusClassifier cc) {
		String qaMap = "";
		if(answersIDMap.get(ut.getCat()) == null){
			answersIDMap.put(ut.getCat(), answerID);
			ArrayList<String> answersList = cc.getAnswersMap().get(ut.getCat());
			if(answersList == null){
				answersList = new ArrayList<String>();
				answersList.add("DefaultAnswer"+defaultAnswerId);
				defaultAnswerId++;
				cc.getAnswersMap().put(ut.getCat(), answersList);
			}
				
			for(String answer : answersList){
				qaMap += "<dict>\n<key>value</key>\n<integer>6</integer>\n<key>qid</key>\n<integer>" + questionID + "</integer>\n<key>aid</key>\n<integer>" + answerID + "</integer>\n</dict>\n";
				answers += generateDictAnswer(new Utterance(ut.getCat(), answer)) + "\n";
				answerID++;
			}
		}
		else{
			int j = new Integer(answersIDMap.get(ut.getCat()));
			for(String answer : cc.getAnswersMap().get(ut.getCat())){
				qaMap += "<dict>\n<key>value</key>\n<integer>6</integer>\n<key>qid</key>\n<integer>" + questionID + "</integer>\n<key>aid</key>\n<integer>" + j + "</integer>\n</dict>\n";
				j++;
			}
		}
		questionID++;
		return qaMap;
	}

	private String generateDictQuestion(Utterance ut) {
		String str = "<dict>\n<key>text</key>\n<string>" + ut.getUtterance() + "</string>\n<key>ID</key>\n<string>" + ut.getCat() + "</string>\n<key>modified</key>\n<date>2012-02-09T15:42:31Z</date>\n</dict>";
		return str;
	}

	private String generateDictAnswer(Utterance ut) {
		String str = "<dict>\n<key>text</key>\n<string>" + ut.getUtterance() + "</string>\n<key>speaker</key>\n<integer>0</integer>\n<key>ID</key>\n<string>" + ut.getCat() + "</string>\n<key>modified</key>\n<date>2012-02-09T15:42:31Z</date>\n</dict>";
		return str;
	}
	
	private static final long serialVersionUID = 7918901258241131287L;

	public void setThreshold(double threshold) {
		try {
			BufferedReader br;
			br = new BufferedReader(new FileReader(new File(plistFilePath)));
			String input = "";
			String output = "";
			String params = "";
			StringTokenizer strTokenizer;

			while((input = br.readLine()) != null){
				output += input + "\n";
				if(input.equals("<key>searcherSessions</key>")){
					output += br.readLine() + "\n" + br.readLine() + "\n" + br.readLine() + "\n";
					input = br.readLine();
					input = input.replaceAll("<string>", "");
					input = input.replaceAll("</string>", "");
					strTokenizer = new StringTokenizer(input, " ");
					params = strTokenizer.nextToken() + " ";
					params += strTokenizer.nextToken() + " ";
					params += threshold;
					output += "<string>" + params + "</string>\n";
				}
			}
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(plistFilePath), "UTF-8"));
			bw.write(output);
			bw.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		
	}

}
