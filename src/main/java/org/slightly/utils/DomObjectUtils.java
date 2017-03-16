package org.slightly.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DomObjectUtils {

	// return the dom object of the html document
	public static Document getDomObjectFromHtml (String htmlContent) throws ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document domObject = builder.parse(new InputSource(new ByteArrayInputStream(htmlContent.getBytes("utf-8"))));
		domObject.getDocumentElement().normalize();
		
		return domObject;
	}
	
	// transform xml into String
	public static String getStringFromDomObject (Document htmlContent) throws TransformerException {
		DOMSource domSource = new DOMSource(htmlContent);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.transform(domSource, result);
		
		return writer.toString();
	}
}
