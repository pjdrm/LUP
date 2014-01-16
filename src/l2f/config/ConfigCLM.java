package l2f.config;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ConfigCLM {
	public static String plistFileEmptyPath = "";
	public static String npcEditorPath = "";
	public static String historyFilePath = "";
	public static boolean alwaysAnswers = true;
	public static String clmDomainConfigProp = "clmConfig";
	public static long pingTime = 0;
	
	public static void parseConfig() {
		try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(Config.getInputStream(Config.clmConfig));
            doc.getDocumentElement().normalize();

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression expr;
            Node node;
            
                    
            expr = xpath.compile("//plistEmpty");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            plistFileEmptyPath = node.getTextContent();
            
            expr = xpath.compile("//npcEditorPath");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            npcEditorPath = node.getTextContent();
            
            expr = xpath.compile("//historyFilePath");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            historyFilePath = node.getTextContent();
            
            expr = xpath.compile("//pingTime");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            pingTime = Long.parseLong(node.getTextContent())*1000;
            
            expr = xpath.compile("//alwaysAnswers");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            alwaysAnswers = Boolean.parseBoolean(node.getTextContent());
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
	}
}
