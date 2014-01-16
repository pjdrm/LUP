package l2f.config;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ConfigLSA {
	public static String features = "";
	public static String vectorCalc = "";
	public static String maxFactors = "";
	public static String featureInit;
	public static String initialLearningRate = "";
	public static String annealingRate = "";
	public static String regularization = "";
	public static String minImprovement = "";
	public static String minEpochs = "";
	public static String maxEpochs = "";
	
	public static void parseConfig() {
		try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new FileInputStream(new File(Config.lsaConfig)));
            doc.getDocumentElement().normalize();

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression expr;
            Node node;
            
            expr = xpath.compile("//config/features");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            features = node.getTextContent();
            
            expr = xpath.compile("//config/vectorCalc");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            vectorCalc = node.getTextContent();
            
            expr = xpath.compile("//config/maxFactors");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            maxFactors = node.getTextContent();
            
            expr = xpath.compile("//config/featureInit");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            featureInit = node.getTextContent();

            expr = xpath.compile("//config/initialLearningRate");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            initialLearningRate = node.getTextContent();
            
            expr = xpath.compile("//config/annealingRate");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            annealingRate = node.getTextContent();
            
            expr = xpath.compile("//config/regularization");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            regularization = node.getTextContent();
            
            expr = xpath.compile("//config/minImprovement");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            minImprovement = node.getTextContent();
            
            expr = xpath.compile("//config/minEpochs");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            minEpochs = node.getTextContent();
            
            expr = xpath.compile("//config/maxEpochs");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            maxEpochs = node.getTextContent();
            
        } catch (Exception e) {
        	e.printStackTrace();        	
            System.exit(-1);
        }
	}
}
