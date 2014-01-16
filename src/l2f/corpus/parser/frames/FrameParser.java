package l2f.corpus.parser.frames;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

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

import l2f.corpus.CorpusFrameClassifier;
import l2f.corpus.parser.CorpusFrameParser;
import l2f.evaluator.frames.AttributeValue;
import l2f.evaluator.frames.Frame;
import l2f.evaluator.frames.FrameAttribute;
import l2f.evaluator.frames.FrameQuestion;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FrameParser implements CorpusFrameParser{

	HashMap<String, FrameAttribute> attributesMap = new HashMap<String, FrameAttribute>();
	ArrayList<Frame> frames = new ArrayList<Frame>();
	ArrayList<FrameQuestion> frameQuestions = new ArrayList<FrameQuestion>();

	public CorpusFrameClassifier parseCorpus(String corpusPath) {
		try {
			File f = new File(corpusPath);

			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true); 
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc;

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			XPathExpression expr;
			NodeList nodeFrameList;
			NodeList nodeAttrList;
			NodeList nodeAttrValList;
			NodeList nodeQuestionList;
			NodeList nodeQuestionValList;

			doc = builder.parse(f);
			doc.getDocumentElement().normalize();

			expr = xpath.compile("//FramesDefinition/frame");
			nodeFrameList = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
			for(int i = 0; i < nodeFrameList.getLength(); i++){
				expr = xpath.compile("./@id");
				Frame frame = new Frame((String)expr.evaluate(nodeFrameList.item(i), XPathConstants.STRING));
				expr = xpath.compile("./attribute");
				nodeAttrList = (NodeList)expr.evaluate(nodeFrameList.item(i), XPathConstants.NODESET);
				for(int j = 0; j < nodeAttrList.getLength(); j++){
					expr = xpath.compile("./@name");
					String frameAttr = (String)expr.evaluate(nodeAttrList.item(j), XPathConstants.STRING);

					expr = xpath.compile("./value");
					nodeAttrValList = (NodeList)expr.evaluate(nodeAttrList.item(j), XPathConstants.NODESET);
					ArrayList<AttributeValue> avArray = new ArrayList<AttributeValue>();
					ArrayList<String> valuesArray;
					String vals = "";
					char[] stringArray;
					String valToken;
					for(int j1 = 0; j1 < nodeAttrValList.getLength(); j1++){
						valuesArray = new ArrayList<String>();
						vals = nodeAttrValList.item(j1).getTextContent();
						
						if(vals.contains(",")){
							StringTokenizer strToken = new StringTokenizer(vals, ",");
							while(strToken.hasMoreTokens()){
								valToken = strToken.nextToken();
								valuesArray.add(valToken);
								stringArray = valToken.toCharArray();
								stringArray[0] = Character.toUpperCase(stringArray[0]);
								valuesArray.add(new String(stringArray));
							}
						}
						else{
							valuesArray.add(vals);
							stringArray = vals.toCharArray();
							stringArray[0] = Character.toUpperCase(stringArray[0]);
							valuesArray.add(new String(stringArray));
						}
						avArray.add(new AttributeValue(valuesArray));
					}

					ArrayList<AttributeValue> avClone = new ArrayList<AttributeValue>();
					for(AttributeValue av : avArray)
						avClone.add(new AttributeValue(av));
					addFrameAttr(new FrameAttribute(frameAttr, avArray));
					frame.addAttribute(new FrameAttribute(frameAttr, avClone));
				}

				expr = xpath.compile("./questions");
				nodeQuestionList = (NodeList)expr.evaluate(nodeFrameList.item(i), XPathConstants.NODESET);
				String question;
				for(int j = 0; j < nodeQuestionList.getLength(); j++){
					expr = xpath.compile("./q");
					nodeQuestionValList = (NodeList)expr.evaluate(nodeQuestionList.item(j), XPathConstants.NODESET);
					for(int j1 = 0; j1 < nodeQuestionValList.getLength(); j1++){
						question = nodeQuestionValList.item(j1).getTextContent();
						addFrameQuestion(new FrameQuestion(question, frame.getId(), fillSlotValues(frame, question)));
						frame.addQuestion(nodeQuestionValList.item(j1).getTextContent());
					}

				}
				addFrame(frame);

			}
			return new CorpusFrameClassifier(getAttributesMap(), getFrames(), getFrameQuestions());

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

	private ArrayList<String> fillSlotValues(Frame frame, String question) {
		ArrayList<String> slotValues = new ArrayList<String>();
		for(FrameAttribute fa : frame.getFramesAttributes()){
			slotValues.add(fa.getSlotValues(question));			
		}
		return slotValues;
	}

	private void addFrame(Frame frame) {
		getFrames().add(frame);
	}

	public HashMap<String, FrameAttribute> getAttributesMap() {
		return attributesMap;
	}

	public void addFrameAttr(FrameAttribute fa){
		if(getAttributesMap().containsKey(fa.getName())){
			getAttributesMap().get(fa.getName()).addValues(fa.getAttrValues());
		}
		else{
			getAttributesMap().put(fa.getName(), fa);

		}
	}

	public ArrayList<Frame> getFrames(){
		return frames;
	}

	public ArrayList<FrameQuestion> getFrameQuestions(){
		return frameQuestions;
	}
	public void addFrameQuestion(FrameQuestion fq){
		getFrameQuestions().add(fq);
	}

	@Override
	public boolean canProcessCorpus(String corpusPath) {
		try {
			SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			File schemaLocation = new File("resources/qa/XMLSchemas/FrameCorpusSchema.xsd");
			Schema schema;
			schema = factory.newSchema(schemaLocation);

			Validator validator = schema.newValidator();
			Source source = new StreamSource(corpusPath);

			validator.validate(source);
			return true;
		} catch (SAXException e) {
//			e.printStackTrace();
		} catch (IOException e) {
//			e.printStackTrace();
		}
		return false;
	}

}
