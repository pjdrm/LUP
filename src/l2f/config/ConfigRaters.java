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

public class ConfigRaters {
	public static String mode = "";
	public static String measure = "";
	public static String resultsDir = "";
	public static String userTestFile = "";
	
	public static void parseConfig() {
		try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new FileInputStream(new File(Config.ratersConfig)));
            doc.getDocumentElement().normalize();

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression expr;
            Node node;
            
            expr = xpath.compile("//config/mode");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            mode = node.getTextContent();
            
            expr = xpath.compile("//config/measure");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            measure = node.getTextContent();
            
            expr = xpath.compile("//config/resultsDir");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            resultsDir = node.getTextContent();
            
            expr = xpath.compile("//config/userTestFile");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            userTestFile = node.getTextContent();
            

            
        } catch (Exception e) {
        	e.printStackTrace();        	
            System.exit(-1);
        }
	}
}
