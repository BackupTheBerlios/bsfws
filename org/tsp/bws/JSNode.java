/******************************************************************************
** Class JSNode.java
** 2002-11-23 by Tobias Specht
*******************************************************************************
** This class capsules the DOM-node object provided by the Mozilla JavaScript
** engine
*******************************************************************************
 *
 * Changelog
 * ---------
 *
 * V0.1   @ 2003-01-21
 * V0.2   @ 2003-12-12
 *   Nearly complete rewrite, JSNode should no work without problems with
 *   any DOM/LiveConnect compliant browser
 *
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

/** 
 * This class capsules the DOM-node object provided by the JavaScript/ECMAScript
 * engine.
 * All core methods defined in the DOM (Core) Level 1 Standard as methods of DOM node
 * are available. Additionally, the methods of the <tt>CharacterData</tt> and the
 * <tt>Element</tt> interfaces are implemented except the <tt>Element</tt>'s
 * <tt>getElementsByTagName()</tt> and <tt>normalize()</tt> methods.
 *
 * @author Tobias Specht
 * @version 1.0
 */
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

	/**
	 * Creates a new JSNode reference to the indicated DOM node.
	 *
	 * @param window the window the DOM node resides in.
	 * @param nodeRef the <tt>id</tt> of the node.
	 */
    public JSNode(JSObject window, String nodeRef) {
		htmlId=nodeRef;
		Object[] callArgs=new Object[1];
		callArgs[0]=nodeRef;
		jsWindow=window;
		// 'document' is needed here!
		//JSObject
		jsDocument=(JSObject)jsWindow.getMember("document");
		if (debug==1) {
			System.out.println("[JSNode.constructor] document: " + jsDocument);
		}
		node=(JSObject) jsDocument.call("getElementById",callArgs);

		if (debug==1) {
			System.out.println("[JSNode.constructor] New JSNode created: "+ this.toString());
		}
    }

	/**
	 * Creates a new JSNode in the specified window and from an existing {@link JSObject} node.
	 *
	 * @param window the base window of the new JSNode.
	 * @param existingNode the node that shall be constructed in this window.
	 */
	public JSNode(JSObject window, JSObject existingNode) {
		jsWindow=window;
		jsDocument=(JSObject)jsWindow.getMember("document");
		node=existingNode;
	}

	/**
	 * Creates a new JSNode in the specified window and from an existing node, also
	 * sets the <tt>id</tt> attribute so the element can be accessed easily later.
	 *
	 * @param window the base window of the new JSNode.
	 * @param existingNode the node that shall be constructed in this window.
	 * @param id the id under which the node shall be available in the dom.
	 */
	public JSNode(JSObject window, JSObject existingNode, String id) {
		jsWindow=window;
		jsDocument=(JSObject)jsWindow.getMember("document");
		existingNode.setMember("id",id);
		node=existingNode;
	}

	/**
	 * Overwritten destructor, prints a 'notice of destruction' to the standard out.
	 */
	protected void finalize() {
		System.out.println("[JSNode.finalize] deleting reference to this node: " + node.toString());
	}

	/**
	 * Creates a JSNode from an existing JavaScript/JSObject node.
	 *
	 * @param newNode the pre-existing node.
	 * @return the newly created JSNode.
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
	
	/**
	 * Returns the JSObject underlying this JSNode.
	 *
	 * @return the underlying node as JSObject.
	 */
	public JSObject getNode() {
		return node;
	}

	/**
	 * Returns this node's parent window.
	 *
	 * @return the parent window as JSObject.
	 */
    private JSObject getWindow() {
		// get the window the node exists in
		return jsWindow;
    }

	/**
	 * Returns this node's parent document.
	 *
	 * @return the parent document as JSObject.
	 */
    private JSObject getDocument() {
		// get the document the node exists in
		System.out.println(jsWindow.toString());
		JSObject theDocument=(JSObject)jsWindow.getMember("document");
		System.out.println(theDocument.toString());
		return theDocument;
    }

	/** 
	 * Creates a attribute node of the specified type and with the specified value.
	 *
	 * @param attributeType type of the attribute.
	 * @param attributeValue value the attribute will be set to.
	 * @return a JSNode referencing the attribute.
	 */
	public JSNode createAttribute(String attributeType, String attributeValue) {
		callArgs1[0]=attributeType;
		JSObject tempNode=(JSObject)jsDocument.call("createAttribute",callArgs1);
		callArgs1[0]=attributeValue;
		tempNode.setMember("nodeValue",callArgs1);
		JSNode theNode=new JSNode(jsWindow,tempNode);
		return theNode;
	}

	/** 
	 * Creates a HTML element (for example <tt>&lt;h1&gt;</tt>).
	 *
	 * @param elementType type of the element.
	 * @return a JSNode referencing this element.
	 */
	public JSNode createElement(String elementType) {
		callArgs1[0]=elementType;
		JSObject tempNode=(JSObject)jsDocument.call("createElement",callArgs1);
		JSNode theNode=new JSNode(jsWindow,tempNode);
		return theNode;
	}

	/** 
	 * Creates a text node (text between html nodes).
	 *
	 * @param elementText the text
	 * @return a JSNode referencing the TextNode.
	 */
	public JSNode createTextNode(String elementText) {
		callArgs1[0]=elementText;
		JSObject tempNode=(JSObject)jsDocument.call("createTextNode",callArgs1);
		JSNode theNode=new JSNode(jsWindow,tempNode);
		return theNode;
	}

	/** 
	 * Returns the <tt>id</tt> attribute of the Object.
	 *
	 * @return The <tt>id</tt> attribute of the underlying DOM node.
	 */
    public String getIdentifier() {
		return this.getAttribute("id");
    }

    /** 
     * Appends a child to the current node as the last child.
     *
     * @param childNode the node to be appended.
     * @return Integer 0.
     */
    public int appendChild(JSNode childNode) {
		Object[] callArgs=new Object[1];
		callArgs[0]=childNode.getNode();
//		JSObject textNode=(JSObject) getDocument().call("createTextNode",callArgs);
//		callArgs[0]=textNode;
		node.call("appendChild",callArgs);
		return 0;
    }

	/** 
	 * Append the string to the end of the character data of the node. DOM (Core) Level 1 conforming implementation.
	 *
	 * @param appendString the <tt>String</tt> to be appended.
	 */
	public void appendData(String appendString) {
		callArgs1[0]=appendString;
		node.call("appendData",callArgs1);
	}

	/** 
	 * Appends data to the specified child.
	 *
	 * @param childId append to the n'th child.
	 * @param data the data to be appended.
	 * @return Integer 0.
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

	/** 
	 * Creates a clone of the current node.
	 *
	 * @param sub specifies if the nodes children shall be cloned or if only the node itself is cloned.
	 * @return a reference too the clone.
	 */
    public JSNode cloneNode(boolean sub) {
//		Object[] callArgs=new Object[1];
		callArgs1[0]=new Boolean(sub);
//		callArgs[0]=new Boolean(sub);
		JSObject newNode=(JSObject)node.call("cloneNode",callArgs1);
		JSNode newJSNode=this.getJSNode(newNode);
		return newJSNode;
    }

	/** 
	 * Remove a range of characters from the node. DOM (Core) Level 1 conforming implementation.
	 *
	 * @param offset The offset from which to remove characters.
	 * @param count The number of characters to delete.
	 */
	public void deleteData(int offset, int count) {
		callArgs2[0]=new Integer(offset);
		callArgs2[1]=new Integer(count);
		node.call("deleteData",callArgs2);
	}

	/** 
	 * Deletes part of the data in the specified child of the current node.
	 * 
	 * @param childId delete data from the childNumber'th child.
	 * @param startPosition delete data from this position.
	 * @param number delete as many characters.
	 * @return Integer 0.
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

	/** 
	 * Returns the value of the specified attribute.
	 * 
	 * @param attributeName the name of the attribute whose value shall be returned.
	 * @return the value of the specified attribute as <tt>String</tt>.
	 */
    public String getAttribute(String attributeName) {
		Object[] callArgs=new Object[1];
		callArgs[0]=attributeName;
//		String attribute=(String)node.call("getAttribute",callArgs);
// alternative try with getMemeber
		String attribute=(String)node.getMember(attributeName);
		if (debug>0) {
			System.out.println("[JSNode.getAttribute] got this attribute: " + attribute);
		};
		return attribute;
    }

	/** 
	 * Returns the specified attribute of the node as a node.
	 *
	 * @param attributeName the attribute that shall be returned.
	 * @return the specified attribute as a JSNode.
	 */
    public JSNode getAttributeNode(String attributeName) {
		Object[] callArgs=new Object[1];
		callArgs[0]=attributeName;
		JSObject attributeNode=(JSObject)node.call("getAttributeNode",callArgs);
		JSNode attributeJSNode=this.getJSNode(attributeNode);
		return attributeJSNode;
    }

    /** 
     * Check if the node has got any child nodes.
     *
     * @return <tt>true</tt> if the node has child nodes, else <tt>false</tt>.
     */
    public boolean hasChildNodes() {
		Object[] callArgs=new Object[0];
		Boolean ergebnis=(Boolean) node.call("hasChildNodes",callArgs);
		return ergebnis.booleanValue();
    }

	/**
	 * Inserts a child node in front of another node.
	 *
	 * @param newNode the node that will be inserted
	 * @param positionNode the node before which the new node will be inserted
	 * @return Integer 0.
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

	/** 
	 * Inserts data at the specified character offset. DOM (Core) Level 1 conforming implementation.
	 *
	 * @param offset the offset of the <tt>String</tt> to be inserted.
	 * @param insertString the <tt>String</tt> to be inserted.
     */
    public void insertData(int offset, String insertString) {
    	callArgs2[0]=new Integer(offset);
    	callArgs2[1]=insertString;
    	node.call("insertData",callArgs2);
    }

	/** 
	 * Inserts string data in a nodes child starting from a given position.
	 *
	 * @param childId number of the child where the data will be inserted.
	 * @param position position at which the string will be inserted.
	 * @param data String to be inserted.
	 * @return Integer 0.
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

	/** 
	 * Removes an attribute (sets it to null) from a DOM node.
	 *
	 * @param attributeName name of the attribute to be removed.
	 * @return Integer 0.
	 */
    public int removeAttribute(String attributeName) {
		if (debug==1) {
		   	System.out.println("[removeAttribute] entered");
		}
    	callArgs1[0]=attributeName;
		node.call("removeAttribute",callArgs1);
		return 0;
    }

    /** 
     * Removes and returns the specified attribute; DOM (Core) Level 1 conforming implementation.
	 *
     * @param oldAttr The node to remove from the attribute list.
     * @return the removed attribute as a JSNode.
     */
    public JSNode removeAttributeNode(JSNode oldAttr) {
    	callArgs1[0]=oldAttr.getNode();
    	JSObject returnedAttr=(JSObject)node.call("removeAttributeNode",callArgs1);
    	JSNode toBeReturned=new JSNode(jsWindow,returnedAttr);
    	return toBeReturned;
    }

	/** 
	 * Removes an attribute node. This is of limited use as on microsoft internet explorer
	 * as attributes are not numbered in order of appearance.
	 *
	 * @param attributeIdentifier number of the attribute to be removed. This is either
	 *	 	  	the position of the attribute specified in the HTML tag (on Mozilla, Opera, etc.)
	 *			or the position of the attribute in all attributes theoretically available for
	 *			the attribute's element (on Internet Explorer)
	 * @return Integer 0.
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

    /** 
     * Removes a child from the current node and returns it,
     * DOM (Core) Level 1 implementation.
     *
     * @param refNode the node to be removed.
     * @return The removed child as a JSNode.
     */
    public JSNode removeChild(JSNode refNode) {
    	callArgs1[0]=refNode.getNode();
    	JSObject deletedNode=(JSObject)node.call("removeChild",callArgs1);
    	JSNode toBeReturned=new JSNode(jsWindow,deletedNode);
    	return toBeReturned;
    }


	/** Removes a dom node from the tree, nodes are counted differently on ie and mozilla.
	 *
	 * @param Number of the child to be removed.
	 * @param Integer 0.
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

	/** 
	 * Replaces a child elements text.
	 *
	 * @param newNodeText The new text.
	 * @param oldNodeIdentifier number of the node that shall be accessed.
	 * @return Integer 0.
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

    /** 
     * Replaces the child node <tt>oldChild</tt> with the node <tt>newChild</tt>
     * and returns the <tt>oldChild</tt>. Conforms to the DOM (Core) Level 1 specification
     *
     * @param newChild the child that replaces oldChild
     * @param oldChild the child that will be replaced and returned, must be a child
     *		of this node
     * @return The node that was replaced.
     */
    public JSNode replaceChild(JSNode newChild, JSNode oldChild) {
		callArgs2[0]=newChild.getNode();
		callArgs2[1]=oldChild.getNode();
		JSObject oldNode=(JSObject)node.call("replaceChild",callArgs2);
		JSNode toBeReturned=new JSNode(jsWindow,oldNode);
		return toBeReturned;
    }


	/** 
	 * Replaces the first child elements data, example: in <tt>&lt;h1 id="h1id"&gt;text&lt;/h1&gt;</tt>,
	 * the specified part of <tt>text</tt> is replaced. DOM (Core) Level 1 conforming implementation.
	 *
	 * @param start the first character that will be replaced.
	 * @param value the last character that will be replaced.
	 * @param newData the string that replaces the specified part of the original string.
	 */
    public void replaceData(int start, int length, String newData) {
		Object[] callArgs=new Object[3];
		callArgs[0]=new Integer(start);
		callArgs[1]=new Integer(length);
		callArgs[2]=newData;

//		JSObject childElement=(JSObject)node.getMember("firstChild");
		node.call("replaceData",callArgs);

		if (debug==1) {
			System.out.println("[replaceData] current node: " + this.toString());
			//System.out.println("[replaceData] evalString: " + evalString);
		}
//		return 0;
    }

	/** 
	 * Sets the given attribute to the given value using the JavaScript node's setAttribute method.
	 * If the attribute does not exist, it is created. This method does not work with all attributes 
	 * on MSIE (for example changing an elements style does not work).
	 *
	 * @param attribute name of the attribute to be set.
	 * @param value the attribute shall be set to.
	 * @return Integer 0.
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

	/**
	 * Sets the value of a style attribute.
	 *
	 * @param styleAttribute the name of the attribute.
	 * @param value the value to be set.
	 * @return Integer 0.
	 */
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

    /** 
     * Adds a new attribute. If an attribute with that name is already present in the element, it is replaced by the new one.
	 *
     * @param newAttr the node to be added to the attribute list.
     * @return the node that was set.
     */
    public JSNode setAttributeNode(JSNode newAttr) {
    	callArgs1[0]=newAttr;
    	JSObject oldAttribute=(JSObject)node.call("setAttributeNode",callArgs1);
    	JSNode toBeReturned=new JSNode(jsWindow, oldAttribute);
    	return toBeReturned;
    }

	/** 
	 * Sets the given attribute to the given value using the JavaScript node's setAttributeNode method.
	 * If the attribute does not exist, it is created.
	 * This method does not work with all attributes on MSIE (for example changing an elements style does not work).
	 *
	 * @param attribute name of the attribute to be set.
	 * @param value the attribute shall be set to.
	 * @return Integer 0.
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

	/* DOCUMENT IT
		This method does not seem to be necessary, it is not different from setStyleAttribute!
	 /
	public int setStyleAttributeNode(String styleAttribute, String value) {
		callArgs1[0]=styleAttribute;

		JSObject attributeNode=(JSObject)node.getMember("style");

		if (debug>0) {
		   System.out.println("[setStyleAttributeNode] attributeNode: " + attributeNode);
		}

		attributeNode.setMember(styleAttribute,value);

		if (debug>0) {
			System.out.println("[setStyleAttributeNode] attributeNode: " + attributeNode + attributeNode.getMember(styleAttribute));
			System.out.println("[setStyleAttributeNode] attributeNode value: " + attributeNode.getMember(styleAttribute));
		}

		return 0;
	}*/

	/** 
	 * Extracs a range of data from the Node, return this String.
	 *
	 * @param offset Start offset of substring to extract.
	 * @param count The number of characters to extract.
	 * @return the extracted String.
	 */
	public String substringData(int offset, int count) {
		String fullData=this.getData();
		if ((offset>fullData.length()) || (count<=0)) {
			return null;
		}
		if (count+offset>fullData.length()) {
			count=fullData.length()-offset;
		}
		int endOffset=offset+count;
		String returnString=fullData.substring(offset,endOffset);
		return returnString;
	}

    /** 
     * Returns the html content of a tag
     * (the part between the opening tag and the closing tag (including html entities),
     * e.g. <div>this <em>is</em> the text</div>). This is a method not defined in
     * any DOM standard by is available on Internet Explorer and Mozilla and often
     * comes in handy.
     * 
     * @return The HTML content of the node.
     */
    public String getInnerHTML() {
    	String theInnerHTML=(String)node.getMember("innerHTML");
    	if (debug>0) {
    		System.out.println("[JSNode.getInnerHTML] innerHTML: " + theInnerHTML);
    	}
    	return theInnerHTML;
    }


/*********************************************************
 ***     methods for accessing DOM node attributes     ***
 *********************************************************
 * these are named get + CamelCase attribute name for
 *	obtaining the attribute and set + CamelCase attribute
 *	name for setting it: e.g. getNodeValue and
 *	setNodeValue for modifying nodeValue
 *********************************************************/

    /** 
     * Returns the value of an node/input field 
     *
     * @return The value of the node.
     */
    public String getNodeValue() {
      return (String) node.getMember("nodeValue");
    }

//	/* returns an array of the nodes children as <tt>JSNode</tt>s */
//	public JSNode[] getChildNodes() {
//	  node.getMember("childNodes");
//	}

    /** 
     * Returns the first child node of the current JSNode.
     *
     * @return the first child node.
     */
    public JSNode getFirstChild() {
      JSObject jsObjNode=(JSObject)node.getMember("firstChild");
      if (debug>0) {
        System.out.println("[JSNode.getFirstChild] first child is: " + jsObjNode);
      }
      JSNode theFirstChild=new JSNode(this.jsWindow,jsObjNode);
      if (debug>0) {
        System.out.println("[JSNode.getFirstChild] JSNode is: " + theFirstChild);
      }
      return theFirstChild;
    }

    /**
     * Retrieves the textual content of the node (must be a content/text node.
     *
     * @return the textual content of the node.
     */
    public String getData() {
        //      callArgs2[0]=new String("getAttribute");
//        callArgs1[0]=new String("data");
		System.out.println("[JSNode.getData] about to invoke getAttribute!");
// getAttribute seems not to work here!
//        String nodeData=(String)node.call("getAttribute",callArgs1);
		String nodeData=(String)node.getMember("data");
        System.out.println("[JSNode.getData] string: " + nodeData);
        return nodeData;
    }



/*********************************************************
 ***               event handler methods               ***
 *********************************************************
 * dom event handler attaching works with the
 * .attachEventListener method, the same is done here.
 * however, the implementation of this is _NOT_ DOM
 * conform, it uses node.setMember, which should
 * work with any up-to-date browser. The DOM conform
 * event handling does not work with any IE, cf.
 * http://www.quirksmode.org/dom/w3c_events.html#registration
 *********************************************************/

    /** Sets the specified event of the node to the specified
     * handler-
     *
     * @param event the event for which the event handler shall be
     *    set, e.g. <tt>onclick</tt>
     * @param handler the code that shall be run upon code execution
     */
    public void addEventListener(String event, String handler) {
      node.setMember(event,handler);
    }
 }