package org.slightly.utils;

import java.util.LinkedList;
import java.util.StringTokenizer;

public class StringUtils {

	// find expressions ex ${person.name}
	public static boolean isExpression (String word) {
		if (word.startsWith("${")) {
			return true;
		}
		return false;
	}
	
	// get script tag if present or null from html content
	public static String getScriptTagIfPresentOrNull (String htmlContent) {
		// TODO transform into constants
		int beginIndex = htmlContent.indexOf("<script type=\"server/javascript\">");
		int endIndex = htmlContent.indexOf("</script>");
		if (beginIndex == -1 || endIndex == -1) {
			return null;
		}
		
		return htmlContent.substring(beginIndex, endIndex + "</script>".length());
	}
	
	// get pure javascript code (without the tag) from javascript code with the tag
	public static String getPureJavascriptCode (String javascriptContentWithTag) {
		// TODO transform into constants
		int beginIndex = javascriptContentWithTag.indexOf("<script type=\"server/javascript\">") + "<script type=\"server/javascript\">".length();
		int endIndex = javascriptContentWithTag.indexOf("</script>");	
		
		return javascriptContentWithTag.substring(beginIndex, endIndex);
	}
	
	// get words in order from whole line 
	public static LinkedList<String> getWordsFromLine(String line) {
		LinkedList<String> textWords = new LinkedList<>();
		 StringTokenizer st = new StringTokenizer(line);
		 while (st.hasMoreTokens()) {
		     //System.out.println(st.nextToken());
		     textWords.add(st.nextToken());
		 }
		 
		return textWords;
	}
	
	

}
