package l2f.evaluator.clm;

import java.io.File;
import java.io.IOException;

import edu.usc.ict.vhmsg.MessageEvent;
import edu.usc.ict.vhmsg.MessageListener;
import edu.usc.ict.vhmsg.VHMsg;

import l2f.config.ConfigCLM;

public class NPCEditorLauncher implements MessageListener{

	public static boolean npcEditorAlive = false;
	
	public void launchNPCEditor() {
		String batPath = getJarFolder() + ConfigCLM.npcEditorPath;
		try {
			Process p;
			p = Runtime.getRuntime().exec("cmd /c start " + batPath);
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
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

	public String getJarFolder() {
		String name = this.getClass().getName().replace('.', '/');
		String s = this.getClass().getResource("/" + name + ".class").toString();
		s = s.replace('/', File.separatorChar);
		s = s.substring(0, s.indexOf(".jar")+4);
		s = s.substring(s.lastIndexOf(':')-1);
		return s.substring(0, s.lastIndexOf(File.separatorChar)+1);
//		return "C:/Users/Mota/workspace/LUP/";
	}

	@Override
	public void messageAction(MessageEvent e) {
			npcEditorAlive = true;
	}
}
