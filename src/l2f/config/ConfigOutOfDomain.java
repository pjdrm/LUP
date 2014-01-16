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

public class ConfigOutOfDomain {
	public static String features = "";
	public static String distanceAlgs = "";
	public static String fixedThreshold = "";
	public static Double jaccardOverlapWeight = 0.0;
	public static String oodDistanceConfigPath;
	
	public static void parseConfig() {
		if(oodDistanceConfigPath == null)
			oodDistanceConfigPath = Config.oodDistanceConfig;
		
		try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new FileInputStream(new File(oodDistanceConfigPath)));
            doc.getDocumentElement().normalize();

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression expr;
            Node node;
            
            expr = xpath.compile("//config/features");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            features = node.getTextContent();
            
            expr = xpath.compile("//config/DistanceAlgorithm");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            distanceAlgs = node.getTextContent();
            
            expr = xpath.compile("//config/FixedThreshold");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            fixedThreshold = node.getTextContent();
            
            expr = xpath.compile("//config/JaccardOverlapWeight");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            jaccardOverlapWeight = Double.parseDouble(node.getTextContent());
            
        } catch (Exception e) {
        	e.printStackTrace();        	
            System.exit(-1);
        }
	}
}
