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
**
*******************************************************************************
**
** Planned improvements - Release criteria:
** ----------------------------------------
**  - support more BSFEngines (all standart + orx for 1.0)
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
** tobias.specht@student.uni-augsburg.de
**
******************************************************************************/


import java.lang.*;
import java.applet.*;
import java.util.*;
import com.ibm.bsf.*;
import netscape.javascript.*;

public class bsfWSInterfaceApplet extends Applet {
    // the bsf manager
    private BSFManager mgr;

    // engine objects for the different languages
    private BSFEngine rxEng=null; // rexx
    private BSFEngine jsEng=null; // javascript/rhino

    private JSObject jsWindow=null;
    private JSObject jsDocument=null;
    private Hashtable jsObjects;
    
    // standard constructor, loads the appropriate engine
    public bsfWSInterfaceApplet() {
// debug
	System.out.println("---> bsfInterfaceApplet class successfully instantiated");
	
	// necessary in this applet? evaluate!
	jsObjects=new Hashtable();
	
	// create the BSFManager
	mgr=new BSFManager();
	System.out.println("---> BSFManager instantiated: "+mgr.toString());
	
	// loading the scripting engine from js throws a security exception, so this has
	// to be done here.

	// this CAN NOT be a solution as it would be necessary to load ALL scripting
	// engines here!
	this.loadScriptingEngine("rx");
    }
    
    // load a scripting engine identified by lang
    // will *probably* be private (and moved down ;)
    // allowed strings:
    //   rexx: rx(3654), rexx(3497075), orx(not yet looked up)
    //   javascript: js(3401), javascript(188995949)
    public void loadScriptingEngine(String lang) {
	System.out.println("---> Loading scripting engine [" + lang + "]");

	// the switch statement is based on the hashCode values of the used strings
	// allowed strings set at method header
	switch (lang.hashCode()) {
	case 3654:loadRxEngine(); break;
	case 3497075:loadRxEngine(); break;
	case 3401:loadJSEngine(); break;
	case 188995949:loadJSEngine(); break;
	default:System.out.println(lang + " is not a known engine!"); break;
	}
	System.out.println("---> [" + lang + "] engine available");
    }


    // evaluate/execute script (i.e. pass script to its engine)
    // scriptcode is stored in 'script', language in 'lang'
    // allowed engines:
    //      rx,rexx,orx   = bsf4rexx scripting engine
    //      js,javascript = javascript engine (rhino)
    //      nrx,netrexx   = netrexx (not yet available)
    //      rb,ruby       = jRuby engine (not yet available)
    // --> more to come
    public void executeScript(String script, String lang) {

	// if scripting engine is not loaded yet, load it
	// (if clause in loadLangEng method
	loadScriptingEngine(lang);
	System.out.println(">>> Skript <<<");
	System.out.println(script);
	System.out.println("<<< /Skript >>>");

	// switch statement for eval/exec necessary
	try {
	    // use scripting engine similar to loadScriptingEngine
	    // ie switch case
	    switch (lang.hashCode()) {
	    case 3654:rxEng.eval("",0,0,script);
	    case 3497075:rxEng.eval("",0,0,script);
		/**	    case 3401:jsEng.eval(??);
			    case 188995949:jsEng.eval(??);
		**/
		// rxEng.eval("",0,0,script);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
// some BSFRegistry functions
    // Register the jsWindow, the bsf manager and the bsf rexx engine for access by the
    // applet, should probably be done in the applet.
    public void registerWindow(JSObject theWindow,BSFEngine jsBSFE,BSFManager jsBSFM) {
	jsWindow=theWindow;
	mgr=jsBSFM;
	rxEng=jsBSFE;
    }

    // register the window only (method overloading used)
    public void registerWindow(JSObject theWindow) {
	jsWindow=theWindow;
    }

    // register the document
    public void registerDocument(JSObject theDocument) {
	jsDocument=theDocument;
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
    public BSFEngine getRxEngine() {
	return rxEng;
    }
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
	Object paramArray[]=new Object[1];
	paramArray[0]=objIdentifier;
	JSObject dOMObject=(JSObject) jsDocument.call("getElementById",paramArray);
	if (dOMObject==null) {
	    dOMObject=(JSObject) jsDocument.call("getElementByName",paramArray);
	}
	return dOMObject;
    }

    // getValue
    // returns the value of a form input field
    public String getValue(String objIdentifier) {
	JSObject dOMObject=getDOMObject(objIdentifier);
	String value=(String) dOMObject.getMember("value");
	System.out.println("->->->" +  value);
	return value;
    }

    // setValue
    // set the value of a form input field
    public void setValue(String objIdentifier, String value) {
	JSObject dOMObject=getDOMObject(objIdentifier);
	dOMObject.setMember("value",value);
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

// here comes the load scripting engines part -----------------------------------------------------
// each engine's got its own private method, engines are ONLY loaded when they are not available
// (if statment in method line 1), scripting engines are loaded with bsfmanager.loadScriptingEngine
// and stored in their respective properties (rxEng,jsEng,rbEng)
    private void loadRxEngine() {
 	if (rxEng==null) {
	    try {
		System.out.println("---> Trying to load rexx engine");
		rxEng=mgr.loadScriptingEngine("rexx");
		System.out.println("--->" + rxEng.toString());
	    } catch (Exception e) {
		System.out.println("---> Loading scripting engine [rexx] failed! Error message:");
		e.printStackTrace();
	    }
	}
    }

    private void loadJSEngine() {
	if (jsEng==null) {
	    try {
		System.out.println("---> Trying to load javascript engine");
		jsEng=mgr.loadScriptingEngine("javascript");
	    } catch (Exception e) {
		System.out.println("---> Error loading engine! Stack trace:");
		e.printStackTrace();
	    }
	}
    }
// end of load scripting engines!------------------------------------------------------------------

}
