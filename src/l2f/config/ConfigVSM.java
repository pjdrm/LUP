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

public class ConfigVSM {
	public static String features = "";
	public static String utteranceWeight = "";
	public static String freqCounters = "";
	public static String k_neighbours = "";

	public static void parseConfig() {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new FileInputStream(new File(Config.vsmConfig)));
			doc.getDocumentElement().normalize();

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr;
			Node node;

			expr = xpath.compile("//config/features");
			node = (Node) expr.evaluate(doc, XPathConstants.NODE);
			features = node.getTextContent();
			
			expr = xpath.compile("//config/utteranceWeight");
			node = (Node) expr.evaluate(doc, XPathConstants.NODE);
			utteranceWeight = node.getTextContent();
			
			expr = xpath.compile("//config/frequencyCounter");
			node = (Node) expr.evaluate(doc, XPathConstants.NODE);
			freqCounters = node.getTextContent();
			
			expr = xpath.compile("//config/k-neighbours");
			node = (Node) expr.evaluate(doc, XPathConstants.NODE);
			k_neighbours = node.getTextContent();

		} catch (Exception e) {
			e.printStackTrace();        	
			System.exit(-1);
		}
	}
}
