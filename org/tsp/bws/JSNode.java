/******************************************************************************
** Class JSNode.java
** 2002-11-23 by Tobias Specht
*******************************************************************************
** This class capsules the DOM-node object provided by the Mozilla JavaScript
** engine
*******************************************************************************
**
** Changelog
** ---------
**
** V0.1   @ 2003-01-21
 * V0.2   @ 2003-12-12
 *   Nearly complete rewrite, JSNode should no work without problems with
 *   any DOM/LiveConnect compliant browser
**
*******************************************************************************
**
** Planned improvements - Release criteria:
** ----------------------------------------
**  - none atm
**
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

import java.lang.*;
import netscape.javascript.*;

// it must be secured that all created notes get their ID-Attribute set!
// so that they can be referenced via document.getElementById()

public class JSNode {// extends JSObject {
    private JSObject node;
    private JSObject jsWindow;
	private JSObject jsDocument;
	// set debug mode
	private int debug=1;

    // callArgs Array are used for storing the arguments of JSObject.call() calls
    // callArgs1 is used when one parameter is necessary, callArgs2 is used for
    // two parameters and so on
    private Object[] callArgs1=new Object[1];
    private Object[] callArgs2=new Object[2];

	private String htmlId;

    public JSNode(JSObject window, String nodeRef) {
		htmlId=nodeRef;
		Object[] callArgs=new Object[1];
		callArgs[0]=nodeRef;
		jsWindow=window;
		// 'document' is needed here!
		//JSObject
		jsDocument=(JSObject)jsWindow.getMember("document");
		if (debug==1) {
			System.out.println("[JSNode constructor] document: " + jsDocument);
		}
		node=(JSObject) jsDocument.call("getElementById",callArgs);
	//	callArgs1=new Object[1];
	//	callArgs2=new Object[2];
		if (debug==1) {
			System.out.println("[JSNode constructor] New JSNode created: "+ this.toString());
		}
    }

	/**
	 * creates a new JSNode in the specified window and from an existing node
	 * @param window the base window of the new JSNode
	 * @param existingNode the node that shall be constructed in this window
	 */
	public JSNode(JSObject window, JSObject existingNode) {
		jsWindow=window;
		jsDocument=(JSObject)jsWindow.getMember("document");
		node=existingNode;
	}

	/**
	 * creates a new JSNode in the specified window and from an existing node, also
	 * sets the <tt>id</tt> attribute so the element can be accessed easily later
	 * @param window the base window of the new JSNode
	 * @param existingNode the node that shall be constructed in this window
	 * @param id the id under which the node shall be available in the dom
	 */
	public JSNode(JSObject window, JSObject existingNode, String id) {
		jsWindow=window;
		jsDocument=(JSObject)jsWindow.getMember("document");
		existingNode.setMember("id",id);
		node=existingNode;
	}

	/** creates a JSNode from an existing JavaScript/JSObject node
	 * @param newNode the pre-existing node
	 */
    public JSNode getJSNode(JSObject newNode) {
		JSNode tempNode;
		try {
			tempNode=new JSNode(jsWindow,newNode,(String)newNode.getMember("id"));
		} catch (Exception e) {
			System.out.println("[getJSNode] no id attribute found, creating without id!");
			tempNode=new JSNode(jsWindow,newNode);
		}
		return tempNode;
    }

	public JSObject getNode() {
		return node;
	}

    private JSObject getWindow() {
		// get the window the node exists in
		return jsWindow;
    }

    private JSObject getDocument() {
		// get the document the node exists in
		System.out.println(jsWindow.toString());
		JSObject theDocument=(JSObject)jsWindow.getMember("document");
		System.out.println(theDocument.toString());
		return theDocument;
    }

	/** creates a attribute node of the specified type and with the specified value
	 * @param attributeType type of the attribute
	 * @param attributeValue value the attribute will be set to
	 */
	public JSNode createAttribute(String attributeType, String attributeValue) {
		callArgs1[0]=attributeType;
		JSObject tempNode=(JSObject)jsDocument.call("createAttribute",callArgs1);
		callArgs1[0]=attributeValue;
		tempNode.setMember("nodeValue",callArgs1);
		JSNode theNode=new JSNode(jsWindow,tempNode);
		return theNode;
	}

	/** creates a HTML element (for example <tt>&lt;h1&gt;</tt>)
	 * @param elementType type of the element
	 */
	public JSNode createElement(String elementType) {
		callArgs1[0]=elementType;
		JSObject tempNode=(JSObject)jsDocument.call("createElement",callArgs1);
		JSNode theNode=new JSNode(jsWindow,tempNode);
		return theNode;
	}

	/** creates a text node (text between html nodes)
	 * @param elementText the text
	 */
	public JSNode createTextNode(String elementText) {
		callArgs1[0]=elementText;
		JSObject tempNode=(JSObject)jsDocument.call("createTextNode",callArgs1);
		JSNode theNode=new JSNode(jsWindow,tempNode);
		return theNode;
	}

	/** returns the <tt>id</tt> attribute of the object */
    public String getIdentifier() {
		return this.getAttribute("id");
    }

    /** appends a child to the current node as the last child
     * @param childNode the node to be appended
     */
    public int appendChild(JSNode childNode) {
		Object[] callArgs=new Object[1];
		callArgs[0]=childNode.getNode();
//		JSObject textNode=(JSObject) getDocument().call("createTextNode",callArgs);
//		callArgs[0]=textNode;
		node.call("appendChild",callArgs);
		return 0;
    }

	/** appends data to the specified child
	 * @param childId append to the n'th child
	 * @param data the data to be appended
	 */
    public int appendData(int childId, String data) {
		if (debug==1) {
		   	System.out.println("[appendData] entered, node is " + node);
		}

		callArgs1[0]=htmlId;
		JSObject tempNode=(JSObject) getDocument().call("getElementById",callArgs1);

		if (!(tempNode.equals(node))) {
			System.out.println("[appendData] corrected node!");
			node=tempNode;
		}

		JSObject childNodes=(JSObject)node.getMember("childNodes");

		if (debug==1) {
		   	System.out.println("[appendData] childNodes: " + childNodes);
		}

		JSObject desiredNode=(JSObject)childNodes.getSlot(childId);

		if (debug==1) {
		   	System.out.println("[appendData] node cast to: " + desiredNode);
		}

		callArgs1[0]=data;
		desiredNode.call("appendData",callArgs1);
		return 0;
    }

	/** creates a clone of the current node
	 * @param sub specifies if the nodes children shall be cloned or if only the node itself is cloned
	 */
    public JSNode cloneNode(boolean sub) {
		Object[] callArgs=new Object[1];
		callArgs[0]=new Boolean(sub);
		JSObject newNode=(JSObject)node.call("cloneNode",callArgs);
		JSNode newJSNode=this.getJSNode(newNode);
		return newJSNode;
    }

	/** deletes part of the data in the specified child of the current node
	 * @param childId delete data from the childNumber'th child
	 * @param startPosition delete data from this position
	 * @param number delete as many characters
	 */
    public int deleteData(int childId, int startPosition, int number) {
		if (debug==1) {
		   	System.out.println("[deleteData] entered");
		}

		JSObject childNodes=(JSObject)node.getMember("childNodes");

		if (debug==1) {
		   	System.out.println("[deleteData] childNodes: " + childNodes);
		}

		JSObject desiredNode=(JSObject)childNodes.getSlot(childId);

		if (debug==1) {
		   	System.out.println("[deleteData] node cast to: " + desiredNode);
		}

		callArgs2[0]=new Integer(startPosition);
		callArgs2[1]=new Integer(number);
		desiredNode.call("deleteData",callArgs2);
		return 0;
    }

	/** returns the value of the specified attribute
	 * @param attributeName the name of the attribute whose value shall be returned
	 */
    public String getAttribute(String attributeName) {
		Object[] callArgs=new Object[1];
		callArgs[0]=attributeName;
		String attribute=(String)node.call("getAttribute",callArgs);
		return attribute;
    }

	/** returns the specified attribute of the node as a node IMPLEMENTATION????
	 * @param attributeName the attribute that shall be returned
	 */
    public JSNode getAttributeNode(String attributeName) {
		Object[] callArgs=new Object[1];
		callArgs[0]=attributeName;
		JSObject attributeNode=(JSObject)node.call("getAttributeNode",callArgs);
		JSNode attributeJSNode=this.getJSNode(attributeNode);
		return attributeJSNode;
    }

    /** returns if the node has any child nodes */
    public boolean hasChildNodes() {
		Object[] callArgs=new Object[0];
		Boolean ergebnis=(Boolean) node.call("hasChildNodes",callArgs);
		return ergebnis.booleanValue();
    }

	/** inserts a child node in front of another node
	 * @param newNode the node that will be inserted
	 * @param positionNode the node before which the new node will be inserted
	 */
	public int insertBefore(JSNode newNode, JSNode positionNode) {
		JSObject jsNewNode=newNode.getNode();
		JSObject jsPositionNode=positionNode.getNode();

		if (debug==1) {
			System.out.println("[insertBefore] got the JSObjects: " + jsNewNode + "/" + jsPositionNode);
		}

		callArgs2[0]=jsNewNode;
		callArgs2[1]=jsPositionNode;

		if (debug==1) {
			System.out.println("[insertBefore] callArgs2 ready: " + callArgs2);
		}

		node.call("insertBefore",callArgs2);
		return 0;
	}
    /*
    public int insertBefore(String newNodeText, String childIdentifier) {
		callArgs1[0]=newNodeText;
		JSObject newNode=(JSObject) getDocument().call("createTextNode",callArgs1);
		Object[] callArgs=new Object[2];
		callArgs[0]=newNode;
		Object[] subCallArgs=new Object[1];
		subCallArgs[0]=childIdentifier;
		callArgs[1]=(JSObject) getDocument().call("getElementById",subCallArgs);
		node.call("insertBefore",callArgs);
		return 0;
    }
	*/

	/** inserts string data in a nodes child starting from a given position
	 *
	 * @param childId number of the child where the data will be inserted
	 * @param position position at which the string will be inserted
	 * @param data String to be inserted
	 */
    public int insertData(int childId, int position, String data) {
		if (debug==1) {
		   	System.out.println("[insertData] entered");
		}

		JSObject childNodes=(JSObject)node.getMember("childNodes");

		if (debug==1) {
		   	System.out.println("[insertData] childNodes: " + childNodes);
		}

		JSObject desiredNode=(JSObject)childNodes.getSlot(childId);

		if (debug==1) {
		   	System.out.println("[insertData] node cast to: " + desiredNode);
		}

		callArgs2[0]=new Integer(position);
		callArgs2[1]=data;
		desiredNode.call("insertData",callArgs2);
		return 0;
    }

	/** removes an attribute (sets it to null)
	 *
	 * @param attributeName name of the attribute to be removed
	 */
    public int removeAttribute(String attributeName) {
		if (debug==1) {
		   	System.out.println("[removeAttribute] entered");
		}
    	callArgs1[0]=attributeName;
		node.call("removeAttribute",callArgs1);
		return 0;
    }

	/** removes an attribute node. This is of limited use as on microsoft internet explorer
	 *  as attributes are not numbered in order of appearance.
	 *
	 * @param attributeIdentifier number of the identifier that is required
	 */
    public int removeAttributeNode(int attributeIdentifier) {
    	JSObject attributesNode=(JSObject)node.getMember("attributes");

    	if (debug==1) {
    		System.out.println("[removeAttributeNode] attributes: " + attributesNode);
    	}

    	JSObject desiredNode=(JSObject)attributesNode.getSlot(attributeIdentifier);

    	if (debug==1) {
    		System.out.println("[removeAttributeNode] attribute: " + desiredNode);
    	}

		callArgs1[0]=desiredNode;

    	node.call("removeAttributeNode",callArgs1);
/*		callArgs1[0]=nodeIdentifier;
		callArgs1[0]=(JSObject) getDocument().call("document.getElementById",callArgs1);
		node.call("removeAttributeNode",callArgs1);*/
		return 0;
    }

	/** removes a dom node from the tree, nodes are counted differently on ie and mozilla
	 *
	 * @param number of the child to be removed
	 */
    public int removeChild(int childIdentifier) {
		if (debug==1) {
			System.out.println("[removeChild] entered removeChild()");
		}

		JSObject childNodes=(JSObject)node.getMember("childNodes");

		if (debug==1) {
			System.out.println("[removeChild] got node array: " + childNodes);
		}

		JSObject toBeRemoved=(JSObject)childNodes.getSlot(childIdentifier);

		if (debug==1) {
			System.out.println("[removeChild] childNode: " + toBeRemoved);
		}

		callArgs1[0]=toBeRemoved;
		node.call("removeChild",callArgs1);
		return 0;
    }

	/** replaces the first child elements text
	 *
	 * @param newNodeText The new text
	 * @param oldNodeIdentifier number of the node that shall be accessed
	 */
    public int replaceChild(String newNodeText, int oldNodeIdentifier) {
		callArgs1[0]=newNodeText;
		JSObject newNode=(JSObject)jsDocument.call("createTextNode",callArgs1);
		JSObject childNodes=(JSObject)node.getMember("childNodes");
		JSObject childNode=(JSObject)childNodes.getSlot(oldNodeIdentifier);
		callArgs2[0]=newNode;
		callArgs2[1]=childNode;

		node.call("replaceChild",callArgs2);
		return 0;
    }

	/** replaces the first child elements data, example: in <tt>&lt;h1 id="h1id"&gt;text&lt;/h1&gt;</tt>, the specified part of <tt>text</tt> is replaced.
	 *
	 * @param start the first character that will be replaced
	 * @param value the last character that will be replaced
	 * @param newData the string that replaces the specified part of the original string
	 */
    public int replaceData(int start, int length, String newData) {
		Object[] callArgs=new Object[3];
		callArgs[0]=new Integer(start);
		callArgs[1]=new Integer(length);
		callArgs[2]=newData;

		// child?
		JSObject childElement=(JSObject)node.getMember("firstChild");
		childElement.call("replaceData",callArgs);

		if (debug==1) {
			System.out.println("[replaceData] current node: " + this.toString());
			//System.out.println("[replaceData] evalString: " + evalString);
		}
		return 0;
    }

	/** Sets the given attribute to the given value using the JavaScript node's setAttribute method.
	 *  If the attribute does not exist, it is created.
	 *	This method does not work with all attributes on MSIE (for example changing an elements style does not work).
	 *
	 * @param attribute name of the attribute to be set
	 * @param value the attribute shall be set to
	 */
    public int setAttribute(String attribute, String value) {
		callArgs2[0]=attribute;
		callArgs2[1]=value;

		node.call("setAttribute",callArgs2);

		if (debug==1) {
			System.out.println("[setAttribute] Attribute: " + attribute + ", Value: " + value);
		}
		return 0;
    }

    public int setStyleAttribute(String styleAttribute, String value) {
    	if (debug==1) {
    		System.out.println("[setStyleAttribute] entered ...");
    	}

    	JSObject styleNode=(JSObject)node.getMember("style");

    	if (debug==1) {
    		System.out.println("[setStyleAttribute] Style node: " + styleNode);
    	}

    	styleNode.setMember(styleAttribute,value);

    	if (debug==1) {
    		System.out.println("[setStyleAttribute] Style node (member set): " + styleNode);
    	}

    	callArgs1[0]=styleNode;

    	if (debug==1) {
    		System.out.println("[setStyleAttribute] params: " + styleAttribute + "/" + value);
    	    System.out.println("[setStyleAttribute] Call args: " + callArgs1);
    		System.out.println("[setStyleAttribute] returning ... node: " + node);
    	}

    	return 0;
    }

	/** Sets the given attribute to the given value using the JavaScript node's setAttributeNode method.
	 *  If the attribute does not exist, it is created.
	 *	This method does not work with all attributes on MSIE (for example changing an elements style does not work).
	 *
	 * @param attribute name of the attribute to be set
	 * @param value the attribute shall be set to
	 */
    public int setAttributeNode(String attribute,String value) {
		callArgs1[0]=attribute;
		if (debug==1) {
			System.out.println("[setAttributeNode] :" + jsDocument);
		}
		JSObject attributeNode=(JSObject)jsDocument.call("createAttribute",callArgs1);
		attributeNode.setMember("nodeValue",value);

		callArgs1[0]=attributeNode;
		node.call("setAttributeNode",callArgs1);

		if (debug==1) {
			System.out.println("[setAttributeNode] Attribute: " + attribute + ", value: " + value);
		}
		return 0;
    }

	/** DOCUMENT IT
		This method does not seem to be necessary, it is not different from setStyleAttribute!
	 */
	public int setStyleAttributeNode(String styleAttribute, String value) {
		callArgs1[0]=styleAttribute;

		JSObject attributeNode=(JSObject)node.getMember("style");

		if (debug==1) {
		   System.out.println("[setStyleAttributeNode] attributeNode: " + attributeNode);
		}

		attributeNode.setMember(styleAttribute,value);

		if (debug==1) {
			System.out.println("[setStyleAttributeNode] attributeNode: " + attributeNode + attributeNode.getMember(styleAttribute));
		}

//		callArgs1[0]=attributeNode;
//		attributeNode.call("setAttributeNode",callArgs1);

		if (debug==1) {
			System.out.println("[setStyleAttributeNode] attributeNode value: " + attributeNode.getMember(styleAttribute));
		}

		return 0;
	}

	/** DOCUMENT IT */
    public String getData() {
		//    	callArgs2[0]=new String("getAttribute");
    	callArgs1[0]=new String("data");
    	String nodeData=(String)node.call("getAttribute",callArgs1);
    	System.out.println("[getData] string: " + nodeData);
    	return nodeData;
    }

    /** DOCUMENT IT */
    public String getInnerHTML() {
    	String theInnerText=(String)node.getMember("innerHTML");
    	if (debug>0) {
    		System.out.println("[JSNode-getInnerHTML] innerHTML: " + theInnerText);
    	}
    	return theInnerText;
    }

    /** Requiered to enable extension of JSObject

    public void setSlot(int index,Object value) {
    	System.out.println("setSlot() not implemented");
    }

    /** Requiered to enable extension of JSObject

    public Object getSlot(int index) {
    	return new Object();
    }

    /** Requiered to enable extension of JSObject

    public void removeMember(String name) {
    }
    */
}