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

import java.lang.*;
import netscape.javascript.*;

// it must be secured that all created notes get their ID-Attribute set!
// so that they can be referenced via document.getElementById()

class JSNode {
    private JSObject node;
    private JSObject jsWindow;

    // callArgs Array are used for storing the arguments of JSObject.call() calls
    // callArgs1 is used when one parameter is necessary, callArgs2 is used for 
    // two parameters and so on
    private Object[] callArgs1;
    private Object[] callArgs2;
    
    public JSNode(JSObject window, String nodeRef) {
	Object[] callArgs=new Object[1];
	callArgs[0]=nodeRef;
	jsWindow=window;
	// 'document' is needed here!
	node=(JSObject) getDocument().call("getElementById",callArgs);
	callArgs1=new Object[1];
	callArgs2=new Object[2];
    }

    public JSNode(JSObject newNode) {
	// obtain the newNods parent window
    }
    
    private JSObject getWindow() {
	// get the window the node exists in
	return jsWindow;
    }
    
    private JSObject getDocument() {
	// get the document the node exists in
	System.out.println(jsWindow.toString());
	JSObject theDocument=(JSObject) jsWindow.getMember("document");
	System.out.println(theDocument.toString());
	return theDocument;
    }
    
    public String getIdentifier() {
	return this.getAttribute("id");
    }
    
    // appends a child to the current node
    public int appendChild(String child) {
	Object[] callArgs=new Object[1];
	callArgs[0]=child;
	JSObject textNode=(JSObject) getDocument().call("createTextNode",callArgs);
	callArgs[0]=textNode;
	node.call("appendChild",callArgs);
	return 0;
    }
    
    public int appendData(String data) {
	Object[] callArgs=new Object[1];
	callArgs[0]=data;
	node.call("appendData",callArgs);
	return 0;
    }
    
    public JSNode cloneNode(boolean sub) {
	Object[] callArgs=new Object[1];
	callArgs[0]=new Boolean(sub);
	JSObject newNode=(JSObject) node.call("cloneNode",callArgs);
	JSNode newJSNode=new JSNode(newNode);
	return newJSNode;
    }
    
    public int deleteData(int startPosition, int number) {
	Object[] callArgs=new Object[2];
	callArgs[0]=new Integer(startPosition);
	callArgs[1]=new Integer(number);
	node.call("deleteData",callArgs);
	return 0;
    }
    
    public String getAttribute(String attributeName) {
	Object[] callArgs=new Object[1];
	callArgs[0]=attributeName;
	String attribute=(String) node.call("getAttribute",callArgs);
	return attribute;
    }
    
    public JSNode getAttributeNode(String attributeName) {
	Object[] callArgs=new Object[1];
	callArgs[0]=attributeName;
	JSObject attributeNode=(JSObject) node.call("getAttributeNode",callArgs);
	JSNode attributeJSNode=new JSNode(attributeNode);
	return attributeJSNode;
    }
    
    public boolean hasChildNodes() {
	Object[] callArgs=new Object[0];
	Boolean ergebnis=(Boolean) node.call("hasChildNodes",callArgs);
	return ergebnis.booleanValue();
    }
    
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
	
    public int insertData(int position, String data) {
	Object[] callArgs=new Object[2];
	callArgs[0]=new Integer(position);
	callArgs[1]=data;
	node.call("insertData",callArgs);
	return 0;
    }
	
    public int removeAttribute(String attributeName) {
	callArgs1[0]=attributeName;
	node.call("removeAttribute",callArgs1);
	return 0;
    }
	
    public int removeAttributeNode(String nodeIdentifier) {
	callArgs1[0]=nodeIdentifier;
	callArgs1[0]=(JSObject) getDocument().call("document.getElementById",callArgs1);
	node.call("removeAttributeNode",callArgs1);
	return 0;
    }
	
    public int removeChild(String childIdentifier) {
	callArgs1[0]=childIdentifier;
	callArgs1[0]=(JSObject) getDocument().call("document.getElementById",callArgs1);
	node.call("removeChild",callArgs1);
	return 0;
    }
	
    public int replaceChild(String newNodeText, String oldNodeIdentifier) {
	callArgs1[0]=newNodeText;
	JSObject newNode=(JSObject) getDocument().call("createTextNode",callArgs1);
	callArgs1[0]=oldNodeIdentifier;
	callArgs2[0]=newNode;
	callArgs2[1]=(JSObject) getDocument().call("getElementById",callArgs1);
	node.call("replaceChild",callArgs2);
	return 0;
    }
	
    public int replaceData(int start, int length, String newData) {
	Object[] callArgs=new Object[3];
	callArgs[0]=new Integer(start);
	callArgs[1]=new Integer(length);
	callArgs[2]=newData;
	node.call("replaceData",callArgs);
	return 0;
    }
	
    public int setAttribute(String attribute, String value) {
	callArgs2[0]=attribute;
	callArgs2[1]=value;
	node.call("setAttribute",callArgs2);
	return 0;
    }
	
    public int setAttributeNode(String attribute,String value) {
	callArgs1[0]=attribute;
	JSObject attributeNode=(JSObject) getDocument().call("createAttribute",callArgs1);
	attributeNode.setMember("value",value);
	callArgs1[0]=attributeNode;
	node.call("setAttributeNode",callArgs1);
	return 0;
    }
}
