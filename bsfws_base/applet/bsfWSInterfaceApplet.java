/******************************************************************************
** Script BSFWSInterfaceApplet.java
** 2002-11-23 by Tobias Specht
*******************************************************************************
** This applet handles DOM->BSF communication, passes scripts from the document
** to their respective scripting engines and provides methods scripting
** languages can use to access and manipulate DOM objects
*******************************************************************************
**
** Changelog
** ---------
**
** V0.1   @ 2003-01-21
** V0.2   @ 2003-04-30
**        - scripting engine is determined by mime-type of the script which is
**          passed from javascript
** V0.3   @ 2003-05-20
**        - scripting engines are loaded just-in-time (only java based)
**        - arbitrary (bsf) scripting engines can be used
** V0.9	  @ 2003-06-13
**	  - all scripting engines (incl. jni based) are loaded jit
**	  - V1.0RC1
** V0.91  @ 2003-06-21
**        - some minor changes
** V0.92  @ 2003-06-25
**        - scripts are evaluated within a doPrivileged environment
**
*******************************************************************************
**
** Planned improvements - Release criteria:
** ----------------------------------------
**  - support more BSFEngines (all standard + orx for 1.0)
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
import java.applet.*;
import java.util.*;
import com.ibm.bsf.*;
// using the sun.plugin.javascript.JSObject does not work but results in a
// method not found error! use netscape.javascript.JSObject
import netscape.javascript.*;
import java.security.*;

public class bsfWSInterfaceApplet extends Applet {
    // the bsf manager
    private BSFManager mgr;

    // caching of engines is done by the bsf manager, not necessary
    // in the applet
    private JSObject jsWindow=null;
    private JSObject jsDocument=null;

    // standard constructor
    public bsfWSInterfaceApplet() {
	// everything moved to init()
    }
    
    // init code of the applet, creates a BSFManager and registers itself to
    // bsf registry
    public void init() {
	// create the BSFManager
	mgr=new BSFManager();
	System.out.println("--[bsfWSInterfaceApplet]--> BSFManager instantiated: "+mgr.toString());
	
	// register the applet to the BSFRegistry so it is available to scripting languages
	this.appletToBSFReg();
	
	// register the System.out object to the registry so scripting lanugages can write
	// to the Java console
	mgr.registerBean("SystemOut",System.out);
	
	System.out.println("--[init]--> applet inited successfully!");
    }

    // start is not really necessary and does nothing
    public void start() {
	System.out.println("--[start]--> start() called!");
    }
    
    // load scripting engines is deleted as this is done on-demand now
    // note: scripting engines are cached by the bsf manager, so
    //		 it is not necessary to care for duplicates in the applet
    
    // evaluate/execute script (i.e. pass script to its engine)
    // scriptcode is stored in 'script', language in 'lang'
    // lang must be specified as a mime type, e.g. x-bsf/x-rexx
    public void executeScript(String script, String lang) {
	// the scripting language to be loaded is read
	// directly form the 'lang' part in 'x-bsf/x-lang'
	// which is stored in the 'lang' parameter

	// first the actual language string is obtained from
	// the mime-type
	int langStartPos=lang.indexOf("/x-");
	
	String actLangString=lang.substring(langStartPos+3);
	
	// debug: print the engine that will be loaded
	System.out.println("--[executeScript]--> mime-type: " + lang);
	System.out.println("--[executeScript]--> trying to load [" + actLangString + "] engine");

	// declaring and loading the scripting engine
	// BSFEngine evalEngine;
	
	// language string must be final to be passed to doPrivileged()
	final String actLangStringPriv=actLangString;
	
	try {
	    BSFEngine evalEngine = (BSFEngine) AccessController.doPrivileged(new PrivilegedAction() {
		    public Object run() {
			// declaring and loading the scripting engine
			BSFEngine currentEngine;
			
			try {
			    currentEngine=mgr.loadScriptingEngine(actLangStringPriv);
			    System.out.println("--[executeScript]--> scripting engine loaded successfully");
			    return currentEngine;
			} catch (Exception e) {
			    System.out.println("--[executeScript]--> error loading scripting engine");
			    System.out.println("--[executeScript]--> probably non-existing language: " + actLangStringPriv);
			    e.printStackTrace();
			}
			return null;							
		    }
		});
	    
	    
	    // print the recieved scripts code for debugging purpose
	    System.out.println("--[executeScript]--> Script code start");
	    System.out.println("--[eS-scriptCode]--> " + script);
	    System.out.println("--[executeScript]--> Script code end");
	    
	    // now just pass the script to the loaded engine
	    final String scriptFinal=script;
	    final BSFEngine bsfEngineFinal=evalEngine;
	    
	    AccessController.doPrivileged(new PrivilegedAction() {
		    public Object run() {
			try {
			    bsfEngineFinal.eval("",0,0,scriptFinal);
			} catch (Exception e) {
			    System.out.println("--[executeScript]--> unknown error in scriptEval");
			    e.printStackTrace();
			}
			return null;
		    }
		});
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    // some BSFRegistry functions
    // Register the jsWindow, the bsf manager and the bsf rexx engine for access by the
    // applet, should probably be done in the applet.
    public void registerWindow(JSObject theWindow,BSFEngine jsBSFE,BSFManager jsBSFM) {
	jsWindow=theWindow;
	mgr=jsBSFM;
	//	rxEng=jsBSFE;
    }

    // register the window only (method overloading used)
    public void registerWindow(JSObject theWindow) {
	System.out.println("--[registerWindow]--> registring: "+ theWindow.toString());
	jsWindow=theWindow;
    }

    // register the document
    public void registerDocument(JSObject theDocument) {
	System.out.println("--[registerDocument]--> registring: "+ theDocument.toString());
	jsDocument=theDocument;
	// jsDocument property is 'lost' after initialization
	// storing it to the BSF registry to re-obtain it
	//mgr.registerBean("theDocument",jsDocument);
	System.out.println("--[registerDocument]--> checking jsDocument: " + jsDocument.toString());
	//System.out.println("--[registerDocument]--> registry document: " + mgr.lookupBean("theDocument"));
	//System.out.println("--[registerDocument]--> i am: " + this.toString());
    }
    
    // register the applet to the bsf
    public void appletToBSFReg() {
	mgr.registerBean("AppletObj",this);
    }

    // returns the BSFManager
    public BSFManager getBSFManager() {
	return mgr;
    }

    // returns the BSFRexxEngine
    //    public BSFEngine getRxEngine() {
    //	return rxEng;
    // }
// end of BSFRegistry methods

    
// DOM methods
    // evaluate the string jsString by an JSObject
    // that is, evaluate it as JavaScript string
    // probably will not be available very long
    public void jsEval(String jsString) {
	jsWindow.eval(jsString);
    }

    // getDOMObject
    // returns a DOM-JSObject retrieved from the DOM by id or name
    public JSObject getDOMObject(String objIdentifier) {

	// retrieving jsDocument from the BSFRegistry
	//	System.out.println("--[getDOMObject]--> registry document: " + mgr.lookupBean("theDocument"));
	//	jsDocument=(JSObject) mgr.lookupBean("theDocument");
	
	Object paramArray[]=new Object[1];
	paramArray[0]=objIdentifier;
	JSObject dOMObject=(JSObject) jsDocument.call("getElementById",paramArray);
	if (dOMObject==null) {
	    dOMObject=(JSObject) jsDocument.call("getElementByName",paramArray);
	}
	
	// debug
	System.out.println("--[getDOMObject]--> got object");
	// /debug
	
	return dOMObject;
    }

    // getValue
    // returns the value of a form input field
    public String getValue(String objIdentifier) {
	JSObject dOMObject=getDOMObject(objIdentifier);
	String value=(String) dOMObject.getMember("value");
	System.out.println("--[getValue]-->" +  value);
	return value;
    }

    // setValue
    // set the value of a form input field
    public void setValue(String objIdentifier, String value) {
	JSObject dOMObject=getDOMObject(objIdentifier);
	dOMObject.setMember("value",value);

	// debug
	System.out.println("--[setValue]--> value set");
	// /debug
    }

    // set innerHTML
    // testMethod for IE-TestHTML
    public void setInnerHTML(String objIdentifier, String value) {
	JSObject dOMObject=getDOMObject(objIdentifier);
	dOMObject.setMember("innerHTML",value);
    }

    // getParam
    // return the value of the specified parameter of a DOM object
    public String getParam(String objIdentifier, String paramIdentifier) {
	JSObject dOMObject=getDOMObject(objIdentifier);
	String value=(String) dOMObject.getMember(paramIdentifier);
	return value;
    }

    // setParamNode
    // set the value of the specified parameter of a DOM object using the node class
    public void setParamNode(String objIdentifier, String paramIdentifier, String value) {
	JSNode paramNode=new JSNode(jsWindow,objIdentifier);
	paramNode.setAttribute(paramIdentifier,value);
	//JSObject dOMObject=getDOMObject(objIdentifier);
	//dOMObject.setMember(paramIdentifier, value);
    }

    public void setParam(String objIdentifier, String paramIdentifier, String value) {
	JSObject dOMObject=getDOMObject(objIdentifier);
	dOMObject.setMember(paramIdentifier, value);
    }

    // all delete, engines are loaded on the fly
}
