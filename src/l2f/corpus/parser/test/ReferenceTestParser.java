package l2f.corpus.parser.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import l2f.corpus.CorpusClassifier;
import l2f.corpus.CorpusClassifierReference;
import l2f.corpus.Utterance;
import l2f.corpus.parser.CorpusParser;

public class ReferenceTestParser implements CorpusParser{
	@Override
	public CorpusClassifier parseCorpus(String corpusPath, String corpusProperties) {
		ArrayList<Utterance> questionsInDomain = new ArrayList<Utterance>();
		ArrayList<Utterance> questionsOutDomain = new ArrayList<Utterance>();
		ArrayList<Utterance> questionsContext = new ArrayList<Utterance>();
		ArrayList<Utterance> answers = new ArrayList<Utterance>();

		try {
			File f = new File(corpusPath);

			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true); 
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc;

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			XPathExpression expr;
			doc = builder.parse(f);
			doc.getDocumentElement().normalize();

			expr = xpath.compile("//q[@type = 'i']");
			NodeList qNL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
			String id = "";
			String qInDomain = "";
			for(int i = 0; i < qNL.getLength(); i++){
				expr = xpath.compile("./@id");
				id = (String)expr.evaluate(qNL.item(i), XPathConstants.STRING);
				qInDomain = qNL.item(i).getTextContent();
				questionsInDomain.add(new Utterance("CAT_" + id, qInDomain));
			}
			
			expr = xpath.compile("//q[@type = 'o']");
			qNL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
			String qOutDomain = "";
			for(int i = 0; i < qNL.getLength(); i++){
				qOutDomain = qNL.item(i).getTextContent();
				questionsOutDomain.add(new Utterance("CAT_OUT_DOMAIN", qOutDomain));
			}
			
			expr = xpath.compile("//q[@type = 'c']");
			qNL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
			String qContext = "";
			for(int i = 0; i < qNL.getLength(); i++){
				qContext = qNL.item(i).getTextContent();
				questionsContext.add(new Utterance("CAT_OUT_DOMAIN", qContext));
			}
			
			expr = xpath.compile("//a");
			NodeList aNL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
			String a = "";
			for(int i = 0; i < aNL.getLength(); i++){
				expr = xpath.compile("./@id");
				id = (String)expr.evaluate(aNL.item(i), XPathConstants.STRING);
				
				a = aNL.item(i).getTextContent();
				
				answers.add(new Utterance("CAT_" + id, a));
			}
					
			return new CorpusClassifierReference(questionsInDomain, questionsOutDomain, questionsContext, answers);

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean canProcessCorpus(String corpusPath) {
		try {
			SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			File schemaLocation = new File("resources/qa/XMLSchemas/ReferenceCorpusSchema.xsd");
			Schema schema;
			schema = factory.newSchema(schemaLocation);

			Validator validator = schema.newValidator();
			Source source = new StreamSource(corpusPath);

			validator.validate(source);
			return true;
		} catch (SAXException e) {
//						e.printStackTrace();
		} catch (IOException e) {
//						e.printStackTrace();
		}
		return false;
	}
}
