package l2f.config;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Config {
	public static void setResourceLoader(ResourceLoader rl){
		ResourceLoader.setResourceLoader(rl);
	}

	public static ResourceLoader getResourceLoader() {
		return ResourceLoader.getResourceLoader();
	}
	
    private static Config config = null;

    protected Config() {
        // only to defeat instantiation.
    }

    public static Config getInstance() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }
    
    /*********
     * Question interpretation variables
     *********/
    /** classification */
    /**
     * The directory where the train set is
     */
    public static String classification_trainDir = "";
    /**
     * The directory where the test set is
     */
    public static String corpusDir = "";
    public static String resulsDir = "";
    public static String classification_testDir = "";
    public static String classification_modelFile = "";
    /** analysis*/
    public static String questionAnalysis_tokenizerType = "";
    public static String questionAnalysis_parserGrammarFile = "";
    public static String questionAnalysis_lexiconmapFile = "";
    /**Natural Language Processing */
    public static String nlp_wordnetProperties = "";
    
    public static String named_entities_file = "";
    
//    public static String stopwords = "";
    public static String stopwordsFile = "";
    public static String stopwordsFlags = "";
    public static boolean stopwordsFlag = false;
    
    public static String posTagFile = "";
    public static String posTaggerFlags;
    public static boolean posTaggerFlag = false;
    
    public static String serFile = "";
    public static String framesDir = "";
    
    public static Double testPercentage = 0.0;
    
    private static boolean loadedConfig = false;
	public static Double minAccuracy = 0.0;
	
	public static boolean garbageCorpus;
	public static boolean normalizeStringFlag = false;
	public static String normalizeStringFlags;
	public static String nluTechniques = "";
	public static String nluTechniquesForFrames = "";
	public static String mode = "";
	public static String svmConfig = "";
	public static String clmConfig = "";
	public static String distanceConfig = "";
	public static String crossEntropyConfig = "";
	public static String lsaConfig = "";
	public static String importantWordsConfig = "";
	public static String vsmConfig = "";
	public static String ratersConfig = "";
	public static int nPartitions;
	public static String usersTestPath = "";
	public static String reCVdir = "";
	public static String oodTechniques;
	public static String oodDistanceConfig = "";
	public static Double alpha;
	public static String oodTestDirPath = "";
	public static int maxPredictions = 1;
	

    public static void parseConfig(String configfile) {
    	parseConfig(configfile, false);
    }
    
    public static void parseConfig(String configfile, boolean forceReload) {
    	if((!forceReload) && loadedConfig) {
    		return;
    	}
    	
    	loadedConfig = true;
            //InputStream is = new FileInputStream(configfile)
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
			try {
				db = dbf.newDocumentBuilder();
				Document doc = db.parse(getInputStream(configfile));
	            doc.getDocumentElement().normalize();

	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xpath = factory.newXPath();

	            XPathExpression expr;
	            Node node;
//	            NodeList nodeList;
	            
	            expr = xpath.compile("//interpretation/classification/serFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            serFile = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/mode");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            mode = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/systemResultsDir");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            resulsDir = node.getTextContent();
	            
	            /**classification*/
	            expr = xpath.compile("//interpretation/classification/trainDir");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            classification_trainDir = node.getTextContent();

	            expr = xpath.compile("//interpretation/classification/testDir");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            classification_testDir = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/garbageCorpus");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            garbageCorpus = Boolean.parseBoolean(node.getTextContent());
	            
	            expr = xpath.compile("//interpretation/classification/stopwordsFlags");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            stopwordsFlags = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/posTaggerFlags");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            posTaggerFlags = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/normalizeStringFlags");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            normalizeStringFlags = node.getTextContent();

	            expr = xpath.compile("//interpretation/classification/modelsClassifierFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            classification_modelFile = node.getTextContent();

	            /**analysis*/
	            expr = xpath.compile("//interpretation/analysis/tokenizerType");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            questionAnalysis_tokenizerType = node.getTextContent();

	            expr = xpath.compile("//interpretation/analysis/parserGrammarFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            questionAnalysis_parserGrammarFile = node.getTextContent();

	            expr = xpath.compile("//interpretation/analysis/lexiconmapFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            questionAnalysis_lexiconmapFile = node.getTextContent();

	            /**natural language processing */
	            expr = xpath.compile("//nlp/wordnet/properties");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            nlp_wordnetProperties = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/namedEntitiesFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            named_entities_file = node.getTextContent();
	            
	           /* expr = xpath.compile("//interpretation/classification/stopwordsFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            stopwords = node.getTextContent();*/
	            
	            expr = xpath.compile("//interpretation/classification/POSTaggerConfig");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            posTagFile = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/framesDir");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            framesDir = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/testPercentage");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            testPercentage = Double.parseDouble(node.getTextContent());
	            
	            expr = xpath.compile("//interpretation/classification/minAccuracy");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            minAccuracy  = Double.parseDouble(node.getTextContent());
	            
	            expr = xpath.compile("//interpretation/classification/corpusDir");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            corpusDir = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/reCrossValidateDir");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            reCVdir = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/nPartitions");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            nPartitions = Integer.parseInt(node.getTextContent());
	            
	            expr = xpath.compile("//interpretation/classification/NLUTechniques");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            nluTechniques  = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/NLUTechniquesForFrames");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            nluTechniquesForFrames  = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/OODTechniques");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            oodTechniques  = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/SVMConfigFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            svmConfig  = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/CLMConfigFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            clmConfig  = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/DistanceConfigFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            distanceConfig  = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/CrossEntropyConfigFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            crossEntropyConfig  = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/LSAConfigFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            lsaConfig  = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/IWConfigFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            importantWordsConfig  = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/VSMConfigFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            vsmConfig  = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/OODDistanceConfigFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            oodDistanceConfig  = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/CronbachConfigFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            ratersConfig  = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/usersTestPath");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            usersTestPath  = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/oodTestDirPath");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            oodTestDirPath   = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/stopwordsFile");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            stopwordsFile  = node.getTextContent();
	            
	            expr = xpath.compile("//interpretation/classification/alpha");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            alpha  = Double.parseDouble(node.getTextContent());
	            
	            expr = xpath.compile("//interpretation/classification/alpha");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            alpha  = Double.parseDouble(node.getTextContent());
	            
	            expr = xpath.compile("//interpretation/classification/maxPredictions");
	            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	            maxPredictions  = Integer.parseInt(node.getTextContent());
	            
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (SAXException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
    }
    
    public static InputStream getInputStream(String path){
    	return ResourceLoader.getResourceLoader().getResource(path);
    }
}
