/******************************************************************************
** Class BWSDocument.java
** 2003-11-24 by Tobias Specht
*******************************************************************************
** This class represents a BWS-XHTML document for parsing and rewriting
** purpose. It provides methods to read an BWS conform XHTML document from
** an URL and rewrite it as an standard JavaScript XHTML document that can
** be interpreted by a standard compliant web browser. It uses dom4j for
** DOM manipulation, so if you want to compile it, make sure you've got the
** dom4j classes in your CLASSPATH.
*******************************************************************************
**
** Changelog
** ---------
**
** V0.1   @ 2003-11-24
**
*******************************************************************************
 *
 * - rewriting code must be changed to enable argument passing, cf. bws wiki
 *   http://openfacts.berlios.de/index-en.phtml?title=BSFWebScripting
 * - allow printing only the final xml document (for redirection)
 *
*******************************************************************************
**
** Licencing Information
** ---------------------
**
** Copyright (C) 2002-2003 Tobias Specht
**
** This program is free software; you can redistribute it and/or
** modify it under the terms of the GNU General Public License
** as published by the Free Software Foundation; either version 2
** of the License, or (at your option) any later version.
**
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU General Public License
** along with this program; if not, write to the Free Software
** Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
**
** The GNU General Public License is also available on the Web:
** http://www.gnu.org/copyleft/gpl.html
**
*******************************************************************************
**
** Contact information
** -------------------
**
** For further information on this script mail me at:
**
**     tobi@mail.berlios.de
**
** The most recent version of this file is available from
**
**     http://bsfws.berlios.de/
**
******************************************************************************/

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

/**
 * This class represents a BWS-XHTML document for parsing and rewriting
 * purpose. It provides methods to read an BWS conform XHTML document from
 * an URL and rewrite it as an standard JavaScript XHTML document that can
 * be interpreted by a standard compliant web browser. It uses dom4j for
 * DOM manipulation, so if you want to compile it, make sure you've got the
 * dom4j classes in your CLASSPATH.
 *
 * @author Tobias Specht
 * @version 1.0
 */
public class BWSDocument {
	/** internal representation of the non-bws'd xml document */
	private Document xmlDocument;
	/** list of all bws scripts */
	private Vector scriptNames=new Vector();
	/** debug variable, 0=no debug except rewriten code, 1=some messages, 2=more messages */
    private char debug=0;

	/** 
	 * Loads a document from an URL and parses it to a dom4j XML document.
	 *
	 * @param documentURLString URL of the document to be rewriten.
	 * @throws DocumentException occurs when building the document fails.
	 */
	public void readDocumentFromURL(String documentURLString) throws DocumentException {
		URL documentURL;
		try {
			documentURL=new URL(documentURLString);
			SAXReader xmlReader = new SAXReader();
			this.xmlDocument = xmlReader.read(documentURL);
			if (debug>1) {
			  System.out.println(xmlDocument.toString());
			}
		} catch (Exception e) {
            System.err.println("[Error] Exception creating URL, probably malformed URL");
			e.printStackTrace();
		}
	}

	/** 
	 * Prints the current document to the console via the dom4j XMLWriter 
	 */
	public void printDocumentSource() {
		// use pretty printing
		OutputFormat outformat=new OutputFormat();// = OutputFormat.createPrettyPrint();
//		outformat.setEnconding();
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

	/** 
	 * Get the complete document as a string .
	 *
	 * @return A String containing the XML document.
	 */
	public String getDocument() {
	  return xmlDocument.asXML();
	}

	/**
	 * Prints the names and ids of all scripts found in the document. 
	 */
	public void getScriptNames() {
		XPath xpathSelector = DocumentHelper.createXPath("//script");

		List results = xpathSelector.selectNodes(xmlDocument);

		Element curElement;
		Attribute tempAttribute;
		String tempString=new String();

		for (Iterator elementIterator=results.iterator();elementIterator.hasNext();) {
			curElement = (Element)elementIterator.next();
			if (debug>0) {
              System.out.println("Element found");
              System.out.println(" Name: " + curElement.getName());
            }
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
			if (debug>1) {
			  System.out.println("  " + tempString);
			}
			scriptNames.addElement((Object)tempString);
		}
	}

	/** 
	 * Prints every element from the scriptNames vector. 
	 */
	public void printVector() {
		for (int counter=0;counter<scriptNames.size();counter++) {
			System.out.println((String)scriptNames.elementAt(counter));
		}
	}

	/** 
	 * Get and rewrite all calls to a specific BWS occuring in the document.
	 *
	 * @param attributeValue the id of the script that is searched for.
	 */
	public void getAttributeElement(String attributeValue) {
        XPath xpathSelector = DocumentHelper.createXPath("//@*");

		List results = xpathSelector.selectNodes(xmlDocument);
		List elementAttributes;

		Attribute curAttribute;

		for (Iterator attributeIterator=results.iterator();attributeIterator.hasNext();) {
		  curAttribute = (Attribute)attributeIterator.next();
		  if (debug>0) {
		    System.out.println(attributeValue + "-" + curAttribute.getValue() + "-" + curAttribute.getValue().equals("#:" + attributeValue));
		  }
		  if ((curAttribute.getValue().startsWith("bws:") || curAttribute.getValue().startsWith("#:")) && 	       curAttribute.getValue().indexOf(attributeValue)>0) {
			//curAttribute.setValue("document.getElementById('BWSApplet').executeScript('" + attributeValue + "',this)");
			//System.out.println("#:"  + curAttribute.getValue().indexOf(":"));
		    curAttribute.setValue("document.getElementById('BWSApplet').executeScript('" + curAttribute.getValue().substring(curAttribute.getValue().indexOf(":")+1) + "',this)");
		    //curAttribute.setValue("javascript:bwsexec(" + attributeValue + ")");
		  }
		}

	}

	/** 
	 * Rewrites the BWS script calls of all elements. Script calls are of the form #:script_id and bws:script_id
	 * and are rewritten to the corresponding javascript/liveconnect calls using the {@link org.tsp.bws.BWSDocument#getAttributeElement} method.
	 *
	 * @see org.tsp.bws.BWSDocument#getAttributeElement
	 */
	public void rewriteScriptCalls() {
		String curName;

		for (int scriptCounter=0;scriptCounter<scriptNames.size();scriptCounter++) {
			curName=(String)scriptNames.elementAt(scriptCounter);
			getAttributeElement(curName);
		}
	}

	/** 
	 * Inserts the <tt>applet</tt> tag 
	 */
	public void appendApplet() {
	  XPath xpathSelector = DocumentHelper.createXPath("/html/body");
      List results = xpathSelector.selectNodes(xmlDocument);

	  // body should only occur once per document!
	  if (results.size()>1) {
	  	System.err.println("[BWSDocument.appendApplet] Warning! More than one <body> found, using the first!");
	  }
	  // use the first element returned
	  Element bodyElement=(Element)results.get(0);
	  bodyElement.addElement("applet")
	    .addAttribute("code","org.tsp.bws.BWSApplet")
	    .addAttribute("id","BWSApplet")
	    .addAttribute("width","0")
	    .addAttribute("height","0")
	    .addAttribute("mayscript","true");


	}

	/** 
	 * Rewrite the complete document and append the applet. Equivalent to
	 * <tt>getScriptNames()</tt>, <tt>rewriteScriptCalls()</tt> and
	 * <tt>appendApplet()</tt>.
	 */
	public void rewriteDocument() {
	  this.getScriptNames();
	  this.rewriteScriptCalls();
	  this.appendApplet();
	}
}