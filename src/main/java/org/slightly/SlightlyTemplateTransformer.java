package org.slightly;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.slightly.utils.DomObjectUtils;
import org.slightly.utils.StringUtils;

public class SlightlyTemplateTransformer  {
		
	private ScriptEngine scriptEngine;
	private String htmlContent;
	private String processedHtmlContent;
	private final String JS_EXPRESSION_VARIABLE = "expression";
	private final String DATA_FOR_ELEMENT = "data-for-";
	private final String DATA_IF_ELEMENT = "data-if";
	
	// evaluate the javascript code and also remove it from the html content
	public SlightlyTemplateTransformer (String scriptEngineName, String htmlContent, Map <String, Object> parameters) throws ScriptException, UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException, TransformerException {
		this.htmlContent = htmlContent;
		// TODO change to Java 8 Optional 
		if (null != StringUtils.getScriptTagIfPresentOrNull(htmlContent)) {
			ScriptEngineManager manager = new ScriptEngineManager();
			this.scriptEngine = manager.getEngineByName(scriptEngineName);
			for (Map.Entry<String,Object> parameter: parameters.entrySet()) {
				this.scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).put(parameter.getKey(), parameter.getValue());
			}
			String javascriptContent = StringUtils.getPureJavascriptCode(htmlContent);
			// this is a workaround to use importClass(Packages.org.slightly.Person)
			// TODO extract this to not create a dependency on the nashorn engine, even though it would not cause any issue
			javascriptContent = "load(\"nashorn:mozilla_compat.js\");\n" + javascriptContent;
			this.scriptEngine.eval(javascriptContent);
			this.htmlContent = this.htmlContent.replace(StringUtils.getScriptTagIfPresentOrNull(htmlContent), "");
		}
		this.processedHtmlContent = processTemplate();
		
	}
	
	public String getProcessedContent () {
		return this.processedHtmlContent;
	}
	
	// TODO run this on the constructor and then save the state on a variable so it doesnt have to be re run everytime
	private String processTemplate () throws UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException, ScriptException, TransformerException {
		Document domObject = DomObjectUtils.getDomObjectFromHtml(this.htmlContent);

		// process all nodes from html content
		processNodes(domObject.getElementsByTagName("*"));
		
		return  DomObjectUtils.getStringFromDomObject(domObject);

	}
	
	private void processNodes(NodeList nodes) throws ScriptException {
		// this first iteration is over the nodes (tag elements) for instance <script> <title> <h1>
		for (int temp = 0; temp < nodes.getLength(); temp++) {
			Node node = nodes.item(temp);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (node.hasAttributes()) {
					NamedNodeMap nodeMap = node.getAttributes();
					// TODO extract this second for iteration to a method
					// this second iteration is over the node children (the attributes) for instance in <h1 title="${person.name}"> would go over the title attribute
					for (int i = 0; i < nodeMap.getLength(); i++) {
						Node tempNode = nodeMap.item(i);
						
						// TODO extract this to a method
						// TODO there is a bug here, as the data-if attribute is kept in the final html content, it is not causing any issues however
						if (tempNode.getNodeName().equals(DATA_IF_ELEMENT)) {
							boolean renderElement = (Boolean)returnExpressionValueFromJavaScript(tempNode.getNodeValue());
							if (!renderElement) {
								node.getParentNode().removeChild(node);
								temp = 0;
								continue;
							} 
						}
						
						// TODO extract this to a method
						if (tempNode.getNodeName().startsWith(DATA_FOR_ELEMENT)) {
							String variableName = tempNode.getNodeName().substring(DATA_FOR_ELEMENT.length());
							Object returnJs = returnExpressionValueFromJavaScript(tempNode.getNodeValue());
							
							// TODO finish the logic for Array
							if (returnJs.getClass().isArray()) {
								
							} else if (returnJs instanceof Collection) {
								// TODO extract this to a method
								for (Object item: (Collection)returnJs) {
									// add new nodes with the items from Collection
									Node newNode = node.getParentNode().getOwnerDocument().createElement(node.getNodeName());
									// process the node and checks that only the x variable name is exchanged for the item
									// if there are other expressions in the content they will be evaluated normally with javascript
									// for instance Child: ${child} father ${person.name} would normally return the ${person.name} from the javascript
									String textContent =  processDataForNode(node.getTextContent(), variableName, item.toString());
									newNode.setTextContent(escapeInvalidCharsHtmlTagElement(textContent));
									
									// TODO extract this to a method
									//  if the tag has more attributes besides the data-for-x this is needed to populate all of them 
									// for instance <div data-for-child="person.children" id="children">, this is needed to keep the id="children" in the new nodes
									if (node.hasAttributes()) {
										// get attributes names and values
										nodeMap = node.getAttributes();
										for (i = 0; i < nodeMap.getLength(); i++) {
											tempNode = nodeMap.item(i);
											// skips data-for-child since it is already being processed
											if (!tempNode.getNodeName().startsWith(DATA_FOR_ELEMENT)) {
												((Element)newNode).setAttribute(tempNode.getNodeName(), tempNode.getNodeValue());
											}
										}
									}
									// append the new item as a child
									node.getParentNode().appendChild(newNode);
								}
								// removing the original data-for node since all items where already rendered
								node.getParentNode().removeChild(node);
								// reseting the index since a node was removed and it may cause weird behavior
								temp=0;
								break;
							}
						}
						
						// process the content of the attribute value
						tempNode.setNodeValue( escapeInvalidCharsHtmlAttribute( processNode( tempNode.getNodeValue())));
						
						// process the text content of tag element
						node.getFirstChild().setTextContent(escapeInvalidCharsHtmlTagElement( processNode( node.getFirstChild().getTextContent())));
					}
					if (node.hasChildNodes()) {
						// Visit child nodes if present (other attributes)
						processNodes(node.getChildNodes());
					}
				} else {
					// even if the node has no attributes we still need to check for the expressions (for instance on the <title> $person.name </title>
					node.getFirstChild().setTextContent(escapeInvalidCharsHtmlTagElement( processNode( node.getFirstChild().getTextContent())));				
				}
			}
		}		
	}

	// process a node evaluating the possible expressions
	private String processNode(String textContent) throws ScriptException {
		Map<String, String> expressionsValues = new LinkedHashMap<>();
		for (String word: StringUtils.getWordsFromLine(textContent)) {
			if (StringUtils.isExpression(word)) {
				expressionsValues.put(word, returnExpressionValueFromJavaScript(word).toString());
			}
		}
		
		for (Map.Entry<String,String> expressionValue: expressionsValues.entrySet()) {
			textContent = textContent.replaceFirst(Pattern.quote(expressionValue.getKey()), expressionValue.getValue());
		} 
		
		return textContent;
	}
	
	// process a node inside a data-for- element , since here the expressions are the items and not evaluated in javascript
	private String processDataForNode (String textContent, String variableName, String dataForItem) {
		Map<String, String> expressionsValues = new LinkedHashMap<>();
		for (String word: StringUtils.getWordsFromLine(textContent)) {
			if (StringUtils.isExpression(word) && word.replace("${", "").replace("}", "").equals(variableName)) {
				expressionsValues.put(word, dataForItem);
			}
		}
		for (Map.Entry<String,String> expressionValue: expressionsValues.entrySet()) {
			textContent = textContent.replaceFirst(Pattern.quote(expressionValue.getKey()), expressionValue.getValue());
		} 
		
		return textContent;
	}
	
	// content inside element bodies
	private String escapeInvalidCharsHtmlTagElement(String tagElement) {
		// escape characters
		// TODO not working , throwing exception , check for alternatives
		//String safe = ESAPI.encoder().encodeForHTML(tagElement);
		
		// this is not 100% accurate as wont escape html5 and also it's not specific for the context (escaping inside the tag element)
		return org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(tagElement);
	}
	
	// content inside attribute values
	private String escapeInvalidCharsHtmlAttribute(String attribute) {
		// TODO not working , throwing exception, check for alternatives
		//String safe = ESAPI.encoder().encodeForHTMLAttribute(attribute);

		// this is not 100% accurate as wont escape html5 and also it's not specific for the context (escaping inside the attribute)
		return org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(attribute);
	}
	
	// get desired expression from javascript, passing a java script engine (taking in consideration that the html file 
	// is valid and there is a valid <script type="server/javascript"> tag )
	private Object returnExpressionValueFromJavaScript (String expression ) throws ScriptException {
		expression = expression.replace("{", "").replace("$", "").replace("}", "");
		String javascriptContent = "var " + JS_EXPRESSION_VARIABLE + " = " + expression + " ;";
		this.scriptEngine.eval(javascriptContent);

		return this.scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).get(JS_EXPRESSION_VARIABLE);
	}
}
