package l2f.corpus.parser.qa;

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

import l2f.corpus.CorpusClassifier;
import l2f.corpus.Utterance;
import l2f.corpus.parser.CorpusParser;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class QAParser implements CorpusParser{

	@Override
	public CorpusClassifier parseCorpus(String corpusPath, String corpusProperties) {
		ArrayList<Utterance> questions = new ArrayList<Utterance>();
		ArrayList<Utterance> answers = new ArrayList<Utterance>();

		try {
			int catIndex = 0;

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

			expr = xpath.compile("//corpus/@type");
			NodeList typeList = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
			String cat = (String)expr.evaluate(typeList.item(0), XPathConstants.STRING);

			expr = xpath.compile("//qa");
			NodeList qaNL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
			NodeList questionNL;
			NodeList answersNL;
			String q = "";
			String a = "";
			for(int i = 0; i < qaNL.getLength(); i++){
				expr = xpath.compile("./questions/q/text()");
				questionNL = (NodeList)expr.evaluate(qaNL.item(i), XPathConstants.NODESET);
				for(int j = 0; j < questionNL.getLength(); j++){
					q = questionNL.item(j).getTextContent();
					questions.add(new Utterance(cat + "_" + catIndex, q));
				}

				expr = xpath.compile("./answers/a/text()");
				answersNL = (NodeList)expr.evaluate(qaNL.item(i), XPathConstants.NODESET);
				for(int j = 0; j < answersNL.getLength(); j++){
					a = answersNL.item(j).getTextContent();
					answers.add(new Utterance(cat + "_" + catIndex, a));
				}
				catIndex++;
			}
			/*CorpusClassifier cc = new CorpusClassifier(questions, answers);
			String str = cc.toString();
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("ccTest.txt")));
			bw.write(str);
			bw.close();
			System.exit(0);*/
			
			return new CorpusClassifier(questions, answers, corpusProperties);

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
			File schemaLocation = new File("resources/qa/XMLSchemas/EdgarCorpusSchema.xsd");
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
