package l2f.tests.t;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;

public class QCETTest {

	private double alpha;
	private TTest tTestImple = new TTestImpl();
	private List<TTestHypothesis> hypothesis = new ArrayList<TTestHypothesis>();

	public QCETTest(double alpha){
		this.alpha = alpha;
	}
	
	public boolean tTest(double[] sample1, double[] sample2){
		try {
			return tTestImple.tTest(sample1, sample2, alpha);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		catch (MathException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void performTTest(String cvResultsDir){
		File cvFile = new File(cvResultsDir);
		File resultsFile;
		File runFile;
		String sampleDesc;
		String accuracy;
		double[] sample = new double[5];
		BufferedReader br;
		for(String resultsDir1 : cvFile.list()){
			resultsFile = new File(cvResultsDir + "/" + resultsDir1);
			if(resultsFile.isDirectory()){
				sampleDesc = resultsFile.getName();
				for(int i = 1 ; i < 6; i++){
					runFile = new File(resultsFile.getPath() + "/run" + i + "/" + sampleDesc + "/SystemResults.txt");
					try {
						br = new BufferedReader(new FileReader(runFile));
						accuracy = br.readLine();
						if(!accuracy.contains("Accuracy"))
							accuracy = br.readLine();
						accuracy = accuracy.replaceAll("Accuracy: ", "");
						sample[i - 1] = Double.parseDouble(accuracy);
						
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.exit(1);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				hypothesis.add(new TTestHypothesis(sampleDesc, sample));
				sample = new double[5];
				
			}
		}
		
		boolean isStaticallyRelevant;
		String results = "";
		for(TTestHypothesis h1 : hypothesis){
			for(TTestHypothesis h2 : hypothesis){
				if(h1.getSampleDesc().equals(h2.getSampleDesc()))
					continue;
				isStaticallyRelevant = tTest(h1.getSample(), h2.getSample());
				results += h1.getSampleDesc() + " | " + h2.getSampleDesc() + " | " + isStaticallyRelevant + "\n";
			}
		}
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(new File(cvResultsDir + "/t-test.text")));
			bw.write(results);
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		System.out.println(hypothesis);
	}
	
	public static void main(String[] args){
		QCETTest t = new QCETTest(0.005);
		t.performTTest(args[0]);
	}

}
