package org.slightly;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;
import javax.servlet.ServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.SAXException;

@RunWith(MockitoJUnitRunner.class)
public class SlightlyTest {

	 String htmlContent = "<!DOCTYPE html>\n"
			+ "<html>\n"
		+ "<script type=\"server/javascript\">\n"
		+ "importClass(Packages.org.slightly.Person)\n"
		+ "var id=request.getParameter(\"id\")\n"
		+ "var person=Person.lookup(id)\n"
		+ "</script>\n"
		+ "<head>\n"
		+ "<title>${person.name}</title>\n"
		+ "</head>\n"
		+ "<body>\n"
		+ "<h1 title=\"${person.name}\">${person.name}</h1>\n"
			+ "<h2 data-if=\"person.married\" title=\"${person.spouse}\">Spouse:\n"
			+ "		${person.spouse}</h2>\n"
+ "	<div data-for-child=\"person.children\" >Child: ${child} father ${person.name}</div>\n"
+ "</body>\n"
+ "</html>\n";
	 SlightlyTemplateTransformer slightlyTemplateTransformer;
	 @Mock
	 ServletRequest request; 

	@Before
	public void setUp() throws ScriptException, UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException, TransformerException  {
		Mockito.when(request.getParameter("id")).thenReturn("1");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("request", request);
		
		slightlyTemplateTransformer = new SlightlyTemplateTransformer("nashorn", htmlContent, parameters);
	}
	
	public void setUpErik() throws ScriptException, UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException, TransformerException  {
		Mockito.when(request.getParameter("id")).thenReturn("2");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("request", request);
		
		slightlyTemplateTransformer = new SlightlyTemplateTransformer("nashorn", htmlContent, parameters);
	}	
	
	private BufferedReader readLinesFromHtmlContent () {
		StringReader reader = new StringReader(slightlyTemplateTransformer.getProcessedContent());
	    return new BufferedReader(reader);
	    
	}
	
	@Test
	public void shouldReturnPersonDataExpression () throws UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException, ScriptException, TransformerException {
		// iterate nodes and check ${person.name} is exchanged for the person actual name, in this case would be Kerstin (id=1)
		String line;
		BufferedReader br = readLinesFromHtmlContent();
	    while((line=br.readLine())!=null)
	    {
	        if (line.startsWith("<title>")) {
	        	assertEquals("Line should replace ${person.name} for Kerstin.", line.contains("Kerstin"), true);
	        	assertEquals("${person.name} should not be on the line.", line.contains("${person.name}"), false);
	        }
	    }		
	}
	
	@Test
	public void shouldRenderElementWhenDataIfTrue () throws UnsupportedEncodingException, ScriptException, ParserConfigurationException, SAXException, IOException, TransformerException {
		// iterate nodes and check data-if element is render only if expression is true
		setUpErik();
		String line;
		boolean rendered = false;
		BufferedReader br = readLinesFromHtmlContent();
	    while((line=br.readLine())!=null)
	    {
	    	if (line.startsWith("<h2")) {
	    		rendered = true;
	    		break;
	        }
	    }	
	    assertEquals("The element <h2> should be rendered.", rendered, true);
		
	}

	@Test
	public void shouldNotRenderElementWhenDataIfFalse () throws IOException {
		String line;
		boolean rendered = false;
		BufferedReader br = readLinesFromHtmlContent();
	    while((line=br.readLine())!=null)
	    {
	    	if (line.startsWith("<h2 title")) {
	    		rendered = true;
	    		break;
	        }
	    }	
	    assertEquals("The element <h2> should not be rendered.", rendered, false);
	}
	
	@Test
	public void shouldRenderElementsWhenDataForXIsJavaCollection () throws IOException {
		// iterate nodes and check ${person.name} is exchanged for the person actual name, in this case would be Kerstin (id=1)
		String line;
		BufferedReader br = readLinesFromHtmlContent();
	    while((line=br.readLine())!=null)
	    {
	        if (line.startsWith("<div>Child")) {
	        	assertEquals("Line should contain Child 0.", line.contains("Child 0"), true);
	        }
	    }	
		
	}
	
	// TODO complete these tests
	@Test
	public void shouldRenderElementsWhenDataForXIsJavascriptArray () {
	}
	
	@Test
	public void shouldRenderElementsWhenDataForXIsJavaArray () {
	}	
	
}
