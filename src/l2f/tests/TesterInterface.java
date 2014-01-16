package l2f.tests;



public interface TesterInterface {
	public void testClassifier();
	public void publishResults(String dirPath);
	public TestResults getResultsAcc();
	public TestResults getResultsMRR();
}
