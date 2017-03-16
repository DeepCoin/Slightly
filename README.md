# Slightly

Context: Adobe's new Sightly
Adobe Experience Manager 6.0 introduces a new server-side HTML templating language called "Sightly", to replace JSP+JSTL. Later, Adobe
renamed Sightly to HTML Template Language (HTL): https://docs.adobe.com/docs/en/htl/overview.html
The main idea is that the server-side template is already valid HTML. The usual operations of conditional and loop are written in HTML5's
data-attributes.
The exercise: Slightly
For this exercise, you will implement something similar to Sightly, called "Slightly".
It is a small server-side HTML templating language with some server-side scripting in javascript.
You need to know Java, servlets, HTML5, and javascript. You don't need to know any AEM.
The aim is to have a template file like the following in the web server (template index.html is already provided):

```
<!DOCTYPE html>
<html>
<script type="server/javascript">
importClass(Packages.org.slightly.Person)
var id=request.getParameter("id")
var person=Person.lookup(id)
</script>
<head>
<title>${person.name}</title>
</head>
<body>
<h1 title="${person.name}">${person.name}</h1>
<h2 data-if="person.married" title="${person.spouse}">Spouse:
${person.spouse}</h2>
<div data-for-child="person.children">Child: ${child}</div>
</body>
</html>
```

Then, a provided Person Java class:
```java
package org.slightly;
public class Person{
public static Person lookup(String id){ return ...; }
public String getName(){...}
public boolean isMarried(){...}
public String getSpouse(){...}
public List<String> getChildren(){...}
}
```

renders an HTML page like:
```
<!DOCTYPE html>
<html>
<head>
<title>Maria</title>
</head>
<body>
<h1 title="Maria">Maria</h1>
<h2 title="Pere">Spouse: Pere</h2>
<div>Child: Anna</div>
<div>Child: Berta</div>
<div>Child: Clara</div>
</body>
</html>
```

You will write a java servlet that responds to requests for HTML files. The servlet reads the requested file, parses the HTML, and processes the
server-side javascript and server-side data-attributes, and finally sends the resulting HTML to the browser.

#Specification
* <script> element
  - A server-side script element is marked by the attribute type="server/javascript". If the servlet finds such a script element, it should execute its body as javascript. The element itself produces no output for the browser. For javascript-execution you may use Java's built-in Javascript engine. Java 7 includes a version of Mozilla's Rhino engine. Java 8 has a similar Javascript engine called "Nashorn". You may also include Mozilla's Rhino engine. The script element may put new javascript variables in the state, like "person" in the example. Initially, the state should have at least one useful variable: "request", being the current HttpServletRequest object. Using that, the template can depend on input from URL parameters. It is possible to call Java classes from the javascript in the element . The details of exactly that works depend on which Javascript engine you use. In general, there will be facilities to import Java packages and classes, instantiate Java classes, and call methods on them, with a close correspondence between the Javascript syntax and Java syntax. All three mentioned Javascript engines know the getter-setter pattern. So the javascript person.name will be mapped to the Java methods getName() or setName(String) in the usual way.

* $-expressions
  - There may be server-side $-expressions in template. They may appear inside the element bodies and inside the values of attributes. That means they may not appear in the element tags or in the keys of attributes. When the servlet finds an expression, it should evaluate the javascript expression in the current javascript state, and render the result in the HTML output. If the result is rendered inside the body of an HTML element, you must escape any characters that are not allowed inside an HTML element. If the result is rendered inside the value of an attribute, you must escape any characters that are not allowed inside an html attribute value.
Please, use the attached package Slightly.zip as starting point. 

* data-if
  - An HTML element may have an attribute with key "data-if". If the result is true, the servlet should render the element in the normal way, but without the data-if attribute. If the result if false, the servlet should not render the element. data-for-x An HTML element may have an attribute like "data-for-x", where "x" stands for a something that can be a variable name in javascript. When the servlet finds such an attribute, it should evaluate the attribute value as a javascript expression, in the current javascript state. If the result is an array (or a similar thing), then the servlet should render the element once for every item in the array. During each rendering of the element, the variable x stands for the item in the array. The result of the javascript evaluation may come from a java method call, therefore objects similar should also work: javascript array, java array, java collections. If an element has both data-if and data-for attributes, what should happen? What do you think is most useful? Implementation hints You should be able to run the servlet with the provided package using mvn jetty:run. For parsing HTML, you can use any built-in library, your own code, or any libraries you like, as long as they are open source and you add them to the project. Make your life easy: You may assume all HTML files are valid XML. Having parsed HTML, the servlet should traverse the HTML elements in tree-order, and render them, in order to send them to the browser. The formatted HTML needn't be the same character-by-character, as long as it is equivalent as HTML.

The project has to be executed by the Maven goal: mvn jetty:run
The solution should provide testing URL similar to http://localhost:8080/index.html?id=2

---------------------------------------------------------------------------------------------------------------------------------

# Observations

- I'm using a Servlet filter to filter the html files. This way I can modify the content of the response using a wrapper (HtmlResponseWrapper).
- I created a template transformer (SlightlyTemplateTransformer) to iterate over the html tags and process the template.
	- Here I get the html content, extract and run the javascript script code, remove it from html, then process the html code, iterating over the nodes (tags) and checking for the javascript expressions and also for the data-if and data-for-x attributes, processing them. For the javascript expressions I am creating a new javascript variable with their value and processing them with nashorn.
	- I tried some html parsers like jsoup but could not achieve the expected result with them so I went with the W3C Dom parser

- It is working accordingly to the requisites, but there are some things that I could not finish in time so I left them as TODOs:
	- the escape of the html attributes is far from ideal, since the correct would be to take the context in consideration.
	I would have implemented this: https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet , more specifically the rules
	https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet#RULE_.231_-_HTML_Escape_Before_Inserting_Untrusted_Data_into_HTML_Element_Content and https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet#RULE_.232_-_Attribute_Escape_Before_Inserting_Untrusted_Data_into_HTML_Common_Attributes ;
	- My Utils class (DomObjectUtils and StringUtils) I would make them not static classes  and also create unit tests for them;
	- Some code refactoring to extract some methods to achieve a more clean code;
	- Finish the logic if the object return from the data-for-x is an array and not a collection
	- Tests are really basic and not complete. I would like to improve them in many ways.

- I did not have time to finish the optionals. For the first one (local variables), I would starting by checking the scope of the getBindings (scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE)). For the second one (inclusion) I din't really understand it so I would have to do some research .
- If an element has both data-if and data-for attributes, it should check the data-if condition normally and if true, render, and if false not
- There is one known bug that is not causing any issue, which is that the data-if attribute is being kept in the final html code. When I found it there was no more time to fix it.
