package l2f;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SchemaValidator extends DefaultHandler{
	 	public static Locator locator;
	    
	    public void setDocumentLocator(Locator locator) {
	        SchemaValidator.locator = locator;
	    }
	    
	    /*public void error( SAXParseException parseException ) throws SAXException {
	    	System.out.println("Message: " + parseException.getMessage() + "\nLine: " + parseException.getLineNumber());
	    }*/
	    
		public static void main(String[]args){
			SchemaValidator sv = new SchemaValidator();
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
	            factory.setValidating(true);
	            SAXParser parser = factory.newSAXParser();
	            parser.parse(args[0], sv);
	            
				System.out.println("Schema is valid");
			}
			catch (SAXException ex) {
				System.out.println("Failure:\nError:\n" + ex.getMessage() + "\nError\nLine:" + SchemaValidator.locator.getLineNumber());
				return;
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}  
			
			try {
	            
				SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
//				factory.setErrorHandler(sv);
				File schemaLocation = new File(args[0]);
				Schema schema = factory.newSchema(schemaLocation);
				
				Validator validator = schema.newValidator();
				Source source = new StreamSource(args[1]);

				validator.validate(source);
			}
			catch (SAXException ex) {
				System.out.println("Failure:\nError:\n" + ex.getMessage() + "\nError\nLine:" + SchemaValidator.locator.getLineNumber());
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}  
			
			System.out.println(args[1] + " was validated by " + args[0]);
		}

}
