package l2f.evaluator.clm;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import l2f.AnswerTypeSet;
import l2f.config.ConfigCLM;
import l2f.corpus.Corpus;
import l2f.corpus.CorpusClassifier;
import l2f.corpus.Utterance;
import l2f.corpus.parser.clm.CLMParser;
import l2f.corpus.processor.UtteranceProcessor;
import l2f.evaluator.QCEAnswer;
import l2f.evaluator.QuestionClassifierEvaluator;
import l2f.evaluator.arguments.QuestionEvaluatorSet;
import l2f.tests.QCEBaseTester;
import l2f.tests.TesterInterface;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import edu.usc.ict.vhmsg.MessageEvent;
import edu.usc.ict.vhmsg.MessageListener;
import edu.usc.ict.vhmsg.VHMsg;

public class QuestionClassifierEvaluatorCLM implements QuestionClassifierEvaluator, MessageListener, Serializable{

	private CLMParser clmParser;
	private CorpusClassifier cc = new CorpusClassifier();
	public static boolean gotMessage = false;
	private String returnMessage = "";
	private String plistFilePath = "";
	private String agent = "";
	private TesterInterface tester;
	private boolean launchedNPCEditor = false;
	private boolean alwaysAnswers = true;
	private UtteranceProcessor utteranceProcessor;
	private static int questionSentCount = 0;
	private String clmDomainConfig;
	private boolean npcEditorAlive = false;

	public QuestionClassifierEvaluatorCLM(CorpusClassifier cc, String clmDomainConfig){
		ConfigCLM.parseConfig();
		parseDomainConfig(clmDomainConfig);
		this.clmDomainConfig = clmDomainConfig;
		clmParser = new CLMParser(agent, plistFilePath);
		this.cc = cc;
		this.utteranceProcessor = cc.getUtteranceProcessor();
		this.alwaysAnswers = ConfigCLM.alwaysAnswers;
		tester = new QCEBaseTester(this);
	}

	public QuestionClassifierEvaluatorCLM(UtteranceProcessor up, String clmDomainConfig){
		ConfigCLM.parseConfig();
		parseDomainConfig(clmDomainConfig);
		clmParser = new CLMParser(agent, plistFilePath);
		this.utteranceProcessor = up;
		this.alwaysAnswers = ConfigCLM.alwaysAnswers;
		this.clmDomainConfig = clmDomainConfig;
		tester = new QCEBaseTester(this);
	}

	@Override
	public QuestionClassifierEvaluator clone() {
		return new QuestionClassifierEvaluatorCLM(this.utteranceProcessor, this.clmDomainConfig);
	}

	public void setAlwaysAnswers(boolean flag){
		this.alwaysAnswers = flag;
	}

	public CorpusClassifier getCorpus(){
		return cc;
	}

	public String getDescription() {
		return "CLM Classifier" + utteranceProcessor.getDescription();
	}

	@Override
	public TesterInterface getTester() {
		return tester;
	}

	public void launchNPCBot(){
		launchedNPCEditor = true;

		try {
			Process p;
			System.out.println("Starting NPCEditor Bot");
			p = Runtime.getRuntime().exec("cmd /c start autoTrain.bat");
			p.waitFor();


			Socket skt;
			while(true){
				try {
					skt = new Socket("localhost", 4446);
					PrintWriter out = new PrintWriter(skt.getOutputStream(), true);
					out.print("launch\n");
					out.flush();

					BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
					while (!in.ready()) {}
					System.out.println(in.readLine());
					out.close();
					in.close();
					skt.close();
					break;

				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Server not up");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void trainNPCEditor(){
		if(launchedNPCEditor)
			quitNPCEditor();
		launchNPCBot();

		Socket skt;
		try {
			skt = new Socket("localhost", 4446);
			PrintWriter out = new PrintWriter(skt.getOutputStream(), true);
			out.print("train\n");
			out.flush();

			BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
			while (!in.ready()) {}
			System.out.println(in.readLine());
			out.close();
			in.close();
			skt.close();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void quitNPCEditor(){
		Socket skt;
		try {
			skt = new Socket("localhost", 4446);
			PrintWriter out = new PrintWriter(skt.getOutputStream(), true);
			out.print("quit\n");
			out.flush();

			out.close();
			skt.close();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		launchedNPCEditor = false;
	}

	@Override
	public void runClassification() {
		clmParser.parseCorpus(cc);
		trainNPCEditor();
		if(alwaysAnswers){
			quitNPCEditor();
			System.out.println("Setting threshold.");
			clmParser.setThreshold(-100.0);
			launchNPCBot();
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*if(launchedNPCEditor)
			npcEditorBot.quitNPCEditor();
		System.out.println("QCE CLM: parsing corpus");
		clmParser.parseCorpus(cc);
		try {
			System.out.println("QCE CLM: training classifier");
			npcEditorBot.trainCLM();

			if(alwaysAnswers){
				System.out.println("Relaunching NPCEditor so that the threshold is -100.");
				killNPCEditor();
				System.out.println("Setting threshold.");
				clmParser.setThreshold(-100.0);
				npcEditorBot.launchNPCEditor();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		launchedNPCEditor = true;*/

	}

	@Override
	public ArrayList<String> answerQuestion(String question) {
		sendQuestion(question);
		if(returnMessage.equals(AnswerTypeSet.OUTDOMAIN.type())){
			ArrayList<String> possibleAnswers = new ArrayList<String>();
			possibleAnswers.add(AnswerTypeSet.OUTDOMAIN.type());
			return possibleAnswers;
		}
		else
			return cc.getAnswersStringArray(returnMessage);
	}

	@Override
	public QCEAnswer answerWithQCEAnswer(String question) {
		question = utteranceProcessor.processString(question);
		String modifications = utteranceProcessor.getModifications();
		sendQuestion(question);
		ArrayList<Utterance> possibleAnswers;
		Double score = 1.0;
		if(returnMessage.equals(AnswerTypeSet.OUTDOMAIN.type())){
			possibleAnswers = new ArrayList<Utterance>();
			possibleAnswers.add(new Utterance(AnswerTypeSet.OUTDOMAIN.type(), AnswerTypeSet.OUTDOMAIN.type()));
			score = 0.0;
		}
		else
			possibleAnswers = cc.getAnswer(returnMessage);
		return new QCEAnswer(possibleAnswers, modifications, getDescription(), score);
	}

	public void sendQuestion(String question){
		/*if(!launchedNPCEditor){
			NPCEditorLauncher launcher = new NPCEditorLauncher();
			launcher.launchNPCEditor();
			checkHistory();
			launcher.waitForNPCEditor();
			launchedNPCEditor = true;

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String continueKey = null;
			try {
				System.out.println("Please click the Start Training button on NPCEDITOR. Enter c char on cmd when done.");
				while(true){
					continueKey = br.readLine();
					if(continueKey.equals("c"))
						break;
					else
						System.out.println("Please enter c char to continue");
				}
				if(alwaysAnswers){
					System.out.println("Relaunching NPCEditor so that the threshold is -100.");
					killNPCEditor();
					System.out.println("Setting threshold.");
					clmParser.setThreshold(-100.0);
					launcher.launchNPCEditor();
					launcher.waitForNPCEditor();
				}
				launchedNPCEditor = true;
				//System.out.println("Check tresshold.");
				//while(true){
					//continueKey = br.readLine();
					//if(continueKey.equals("c"))
						//break;
				//}
			} catch (IOException ioe) {
				System.exit(1);
			}
		}*/

		VHMsg vhmsg = new VHMsg();

		boolean ret = vhmsg.openConnection();
		if(!ret){
			System.out.println("VHMsg: Connection error!"); 
			System.exit(1);
		}

		vhmsg.enableImmediateMethod();
		vhmsg.addMessageListener(this);
		vhmsg.subscribeMessage("vrExpress");

		vhmsg.sendMessage("vrSpeech start user0001 user");
		vhmsg.sendMessage("acquireSpeech startedListening user 201202100724 user0001 1328887516188");
		vhmsg.sendMessage("vrSpeech finished-speaking user0001");
		vhmsg.sendMessage("acquireSpeech stoppedListening user 201202100724 user0001 1328887516193");
		vhmsg.sendMessage("vrSpeech emotion user0001 1 1.0 normal neutral");
		vhmsg.sendMessage("vrSpeech tone user0001 1 1.0 normal down");
		vhmsg.sendMessage("vrSpeech interp user0001 1 1.0 normal " + question);
		vhmsg.sendMessage("vrSpeech asr-complete user0001");

		System.out.println("Sent Question " + questionSentCount);
		questionSentCount++;

		while(!gotMessage){
			System.out.print("");
			if(!alwaysAnswers){
				System.out.println("Waiting for answer.");
				try {
					Thread.sleep(350);
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
				if(!gotMessage){
					returnMessage = AnswerTypeSet.OUTDOMAIN.type();
				}
				break;
			}
		}
		System.out.println("Received Answer");
		gotMessage = false;
		vhmsg.closeConnection();
	}

	private void checkHistory() {
		try {
			File historyFile = new File(ConfigCLM.historyFilePath);
			if(historyFile.length() == 0){
				updateHistory();
			}
			else{
				BufferedReader brHistory = new BufferedReader(new FileReader(historyFile));
				String lastPlist = brHistory.readLine();
				if(!lastPlist.equals(plistFilePath))
					updateHistory();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	private void updateHistory() {
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(ConfigCLM.historyFilePath));
			bw.write(plistFilePath);
			bw.close();

			System.out.println("Open the file " + plistFilePath + " in NPCEditor.\nPress enter when done.");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			br.readLine();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	public void killNPCEditor(){
		VHMsg vhmsg = new VHMsg();
		boolean ret = vhmsg.openConnection();
		if(!ret){
			System.out.println("VHMsg: Connection error!"); 
			System.exit(1);
		}

		vhmsg.enableImmediateMethod();
		vhmsg.sendMessage("vrKillComponent npceditor");
		vhmsg.closeConnection();

		try { 

			Robot robot = new Robot();
			//			System.out.println("Killing NPCEditor");
			Thread.sleep(500);
			robot.keyPress(KeyEvent.VK_ENTER);
			Thread.sleep(500);

		} catch (AWTException e) { 
			e.printStackTrace(); 
			System.exit(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		launchedNPCEditor = false;
	}

	public String getJarFolder() {
		String name = this.getClass().getName().replace('.', '/');
		String s = this.getClass().getResource("/" + name + ".class").toString();
		s = s.replace('/', File.separatorChar);
		s = s.substring(0, s.indexOf(".jar")+4);
		s = s.substring(s.lastIndexOf(':')-1);
		return s.substring(0, s.lastIndexOf(File.separatorChar)+1);
	}

	private void parseDomainConfig(String clmDomainConfig) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new FileInputStream(clmDomainConfig));
			doc.getDocumentElement().normalize();

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr;
			Node node;

			expr = xpath.compile("//plistFile");
			node = (Node) expr.evaluate(doc, XPathConstants.NODE);
			plistFilePath = node.getTextContent();

			expr = xpath.compile("//agent");
			node = (Node) expr.evaluate(doc, XPathConstants.NODE);
			agent  = node.getTextContent();
		} catch (Exception e) {
			System.err.println("Config: Can't load the given configuration! File: "+clmDomainConfig+" Exception Message: "+e.getLocalizedMessage());
			e.printStackTrace();
			System.exit(-1);
		}

	}

	@Override
	public void messageAction(MessageEvent e){
		returnMessage = e.toString();
		if(!returnMessage.contains("user0001"))
			return;

		String[] arrayMessage = returnMessage.split(" ");
		int i = 0;
		while(i < arrayMessage.length){
			if(arrayMessage[i].contains("ref")){
				returnMessage = arrayMessage[i].replace("ref=", "");
				returnMessage = returnMessage.replace("\"", "");
				break;
			}
			i++;
		}
		gotMessage = true;
		return;
	}

	@Override
	public QuestionEvaluatorSet getType() {
		return QuestionEvaluatorSet.CLM;
	}

	@Override
	public void setCorpus(Corpus corpus) {
		List<Utterance> newTestUtterances = new ArrayList<Utterance>();
		Utterance newUt;
		int i = 0;
		System.out.println(corpus.getTrainUtterances().size());
		for(Utterance ut : corpus.getTestUtterances()){
			newUt = new Utterance(ut.getCat(), ut.getUtterance());
			//			utteranceProcessor.processUtterance(newUt);
			newTestUtterances.add(newUt);
		}
		cc.setTestUtterances(newTestUtterances);

		List<Utterance> newTrainUtterances = new ArrayList<Utterance>();
		for(Utterance ut : corpus.getTrainUtterances()){
			newUt = new Utterance(ut.getCat(), ut.getUtterance());
			utteranceProcessor.processUtterance(newUt);
			newTrainUtterances.add(newUt);
		}
		cc.setTrainUtterances(newTrainUtterances);

		//		this.cc = (CorpusClassifier)corpus;
		cc.setAnswers(((CorpusClassifier) corpus).getAnswers());
		cc.setAnswersMap(((CorpusClassifier) corpus).getAnswersMap());
		//		launchedNPCEditor = false;
	}

	public void waitForNPCEditor() {
		VHMsg vhmsg = new VHMsg();

		boolean ret = vhmsg.openConnection();
		if(!ret){
			System.out.println("VHMsg: Connection error!"); 
			System.exit(1);
		}

		vhmsg.enableImmediateMethod();
		vhmsg.addMessageListener(this);
		vhmsg.subscribeMessage("vrExpress");

		vhmsg.sendMessage("vrAllCall");

		System.out.println("NPCEditor is initializing. This may take a few seconds, please wait.");
		long sleepTime = ConfigCLM.pingTime;
		int nPings = 0;
		while(!npcEditorAlive){
			if(nPings == 3){
				sleepTime = ConfigCLM.pingTime;
				nPings = 0;
			}

			try {
				System.out.println("Pinging NPCEditor...");
				Thread.sleep(sleepTime);
				sleepTime = sleepTime/4;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			vhmsg.sendMessage("vrSpeech start user0002 user");
			vhmsg.sendMessage("acquireSpeech startedListening user 201202100724 user0002 1328887516188");
			vhmsg.sendMessage("vrSpeech finished-speaking user0002");
			vhmsg.sendMessage("acquireSpeech stoppedListening user 201202100724 user0002 1328887516193");
			vhmsg.sendMessage("vrSpeech emotion user0002 1 1.0 normal neutral");
			vhmsg.sendMessage("vrSpeech tone user0002 1 1.0 normal down");
			vhmsg.sendMessage("vrSpeech interp user0002 1 1.0 normal teste");
			vhmsg.sendMessage("vrSpeech asr-complete user0002");
			nPings++;
		}
		npcEditorAlive = false;
		vhmsg.closeConnection();

	}

	private static final long serialVersionUID = 9208795493117718398L;

}
