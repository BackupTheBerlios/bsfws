package org.tsp.bws;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.net.URL;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

public class bwsDocument {
	/** internal representation of the non-bws'd xml document */
	private Document xmlDocument;
	/** list of all bws scripts */
	private Vector scriptNames=new Vector();

	/** loads a document from an URL and parses it to a dom4j XML document
	 *
	 * @param documentURLString URL of the document to be rewriten
	 * @throws DocumentException occurs when building the document fails
	 */
	public void readDocumentFromURL(String documentURLString) throws DocumentException {
		URL documentURL;
		try {
			documentURL=new URL(documentURLString);
			SAXReader xmlReader = new SAXReader();
			this.xmlDocument = xmlReader.read(documentURL);
			System.out.println(xmlDocument.toString());
		} catch (Exception e) {
			System.out.println("[Error] Exception creating URL, probably malformed URL");
			e.printStackTrace();
		}
	}

	/** prints the current document to the console via the dom4j XMLWriter */
	public void printDocumentSource() {
		// use pretty printing
		OutputFormat outformat = OutputFormat.createPrettyPrint();
		// use standard encoding, else: outformat.setEncoding(String encodingScheme)
		try {
			XMLWriter writer = new XMLWriter(System.out);
			writer.write(this.xmlDocument);
			writer.flush();
			// go to the next line
			System.out.println("\n");
		} catch (Exception e) {
			System.out.println("[Error] Exception printing the document, wrong encoding scheme?");
			e.printStackTrace();
		}
	}

	/** prints the names and ids of all scripts found in the document */
	public void getScriptNames() {
		XPath xpathSelector = DocumentHelper.createXPath("//script");

		List results = xpathSelector.selectNodes(xmlDocument);

		Element curElement;
		Attribute tempAttribute;
		String tempString=new String();

		for (Iterator elementIterator=results.iterator();elementIterator.hasNext();) {
			curElement = (Element)elementIterator.next();
			System.out.println("Element found");
			System.out.println(" Name: " + curElement.getName());
			try {
				tempAttribute=curElement.attribute("id");
				tempString=tempAttribute.getValue();
			} catch (Exception e) {
				try {
					tempAttribute=curElement.attribute("name");
					tempString=tempAttribute.getValue();
				} catch (Exception e2) {
					System.out.println("[Error] Script without id or name");
				}
			}
			System.out.println("  " + tempString);
			scriptNames.addElement((Object)tempString);
		}
	}

	/** prints every element from the scriptNames vector */
	public void printVector() {
		for (int counter=0;counter<scriptNames.size();counter++) {
			System.out.println((String)scriptNames.elementAt(counter));
		}
	}

	/** prints (ATM) the names of all elements with an given attribute value
	 *
	 * @param attributeValue value that the desired attributes should be
	 */
	public void getAttributeElement(String attributeValue) {
		XPath xpathSelector = DocumentHelper.createXPath("//*[@*='#:" + attributeValue + "'] | //*[@*='bws:" + attributeValue + "']");

		// XPath-Ausdruck: //*[@*='#:scriptid'] | //*[@*='bws:scriptid']

		List results = xpathSelector.selectNodes(xmlDocument);
		List elementAttributes;

		Element curElement;
		Attribute curAttribute;
		String tempString;

		for (Iterator elementIterator=results.iterator();elementIterator.hasNext();) {
			curElement = (Element)elementIterator.next();
			elementAttributes=curElement.attributes();
			System.out.println("Element found, name: " + curElement.getName());
			// curElement.setName("foundElement" + curElement.getName());
			for (Iterator attributeIterator=elementAttributes.iterator();attributeIterator.hasNext();) {
				curAttribute=(Attribute)attributeIterator.next();
				System.out.println(attributeValue + "-" + curAttribute.getValue() + "-" + curAttribute.getValue().equals("#:" + attributeValue));
				if ((curAttribute.getValue().equals("#:" + attributeValue)) || (curAttribute.getValue().equals("bws:" + attributeValue))) {
					System.out.println("--> " + curAttribute.getValue());
					curAttribute.setValue("javascript:bwsexec(" + attributeValue + ")");
				}
			}
		}
	}

	/** rewrites all script calls of the form #:script_id and bws:script_id to the corresponding javascript/
	  * liveconnect calls
	  */
	public void rewriteScriptCalls() {
		String curName;

		for (int scriptCounter=0;scriptCounter<scriptNames.size();scriptCounter++) {
			curName=(String)scriptNames.elementAt(scriptCounter);
			getAttributeElement(curName);
		}
	}
}