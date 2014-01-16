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

public class ConfigSVM {
	
	public static Boolean fine;
	public static Boolean allSenses;
	public static String features = "";
	
	public static void parseConfig() {
		try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new FileInputStream(new File(Config.svmConfig)));
            doc.getDocumentElement().normalize();

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression expr;
            Node node;
            
            expr = xpath.compile("//config/fine");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            fine = Boolean.parseBoolean(node.getTextContent());
            
            expr = xpath.compile("//config/allSenses");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            allSenses = Boolean.parseBoolean(node.getTextContent());
            
            expr = xpath.compile("//config/features");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            features = node.getTextContent();
            
        } catch (Exception e) {
        	e.printStackTrace();        	
            System.exit(-1);
        }
	}
}
