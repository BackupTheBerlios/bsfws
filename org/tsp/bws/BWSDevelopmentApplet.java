package org.tsp.bws;

import java.lang.*;
import java.applet.*;
import netscape.javascript.*;
import java.security.*;

public class bwsDevelopmentApplet extends Applet {
	public JSObject jsWindow;
	private JSObject jsDocument;
	private JSNode currentNode;

	/** applet constructor */
	public bwsDevelopmentApplet() {
		System.out.println("[bwsDevelopmentApplet constructor] applet object created...");
	}

	/** start method, calls getWindow() */
	public void start() {
		jsWindow=JSObject.getWindow(this);
		System.out.println("[getWindow] window: " + jsWindow.toString());

		JSObject jsDocument=(JSObject)jsWindow.getMember("document");
		System.out.println("[getWindow] document: " + jsDocument.toString());

		JSNode headNode=new JSNode(jsWindow,"testhead");
		JSNode bodyNode=new JSNode(jsWindow,"theBody");
		JSNode dieListe=new JSNode(jsWindow,"dieListe");
		JSNode span=new JSNode(jsWindow,"sp2");

		JSNode newHeading=bodyNode.createElement("h2");
		JSNode headingText=bodyNode.createTextNode("die zweite überschrift");

		System.out.println("[main] headingText has child nodes: " + headingText.hasChildNodes());

		newHeading.appendChild(headingText);

		bodyNode.insertBefore(newHeading,span);

		//bodyNode.appendChild(newHeading);

		span.appendData(0,"--append");
		span.deleteData(0,2,5);
		dieListe.removeChild(0);
		dieListe.removeChild(1);
		System.out.println("[main] dieListe has child nodes: " + dieListe.hasChildNodes());

		bodyNode.setAttribute("bgColor","#00ff00");
		bodyNode.setStyleAttribute("color","red");
		headNode.setStyleAttribute("backgroundColor","#88ff88");
		headNode.setStyleAttribute("border","1px solid #555555");
		bodyNode.setStyleAttribute("fontFamily","verdana");
		System.out.println("[main] body - color attribute: " + bodyNode.getAttribute("bgColor"));
		System.out.println("[main] body - color attribute node: " + bodyNode.getAttributeNode("bgColor").getNode().getMember("value"));

		headNode.replaceChild("Replaced",0);
		headNode.replaceData(2,7,"---");

		headNode.insertData(0,2,"(TEST)");
	}

	/** obtains the document window and references it in jsWindow */
	public void getWindow() {
		jsWindow=JSObject.getWindow(this);
		System.out.println("[getWindow] window: " + jsWindow.toString());
	}
}