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
    // engine objects for the different languages
    /*    private BSFEngine rxEng=null; // rexx
     *private BSFEngine jsEng=null; // javascript/rhino
     */
    private JSObject jsWindow=null;
    private JSObject jsDocument=null;
    //  private Hashtable jsObjects;

    // standard constructor, loads the appropriate engine
    public bsfWSInterfaceApplet() {
	
	// necessary in this applet?
	// at least not atm
	//	jsObjects=new Hashtable();
	
	// create the BSFManager
	mgr=new BSFManager();
	System.out.println("--[bsfWSInterfaceApplet]--> BSFManager instantiated: "+mgr.toString());
	
	// loading the scripting engine from js throws a security exception, so this has
	// to be done here.

	// this CAN NOT be a solution as it would be necessary to load ALL scripting
	// engines here
	//this.loadScriptingEngine("rx");

	// appletToBSFReg moved to init()
    }
    
    // init() could be necessary for IE support, don't know yet, remove if not
    // nope, isn't necessary -> commented out
    public void init() {
	// register the applet to the BSFRegistry so it is available to scripting languages
	this.appletToBSFReg();

	// register the System.out object to the registry so scripting lanugages can write
	// to the Java console          	
	mgr.registerBean("SystemOut",System.out);
	
	System.out.println("--[init]--> init() called successfully!");
    }

    // start() could also be necessary
    public void start() {
	System.out.println("--[start]--> start() called!");
    }

    // load a scripting engine identified by lang
    // will *probably* be private (and moved down ;)
    // allowed strings:
    //   rexx: rx(3654), rexx(3497075), orx(not yet looked up)
    //   javascript: js(3401), javascript(188995949)
/*    public void loadScriptingEngine(String lang) {
 *	System.out.println("--[loadScriptingEngine]--> Loading scripting engine [" + lang + "]");
 *
 *	// the switch statement is based on the hashCode values of the used strings
 *	// allowed strings set at method header
 *	switch (lang.hashCode()) {
 *	case 3654:
 *	case 3497075:loadRxEngine(); break;
 *	case 3401:
 *	case 188995949:loadJSEngine(); break;
 *	default:System.out.println(lang + " is not a known engine!"); break;
 *	}
 *	System.out.println("--[loadScriptingEngine]--> [" + lang + "] engine available @" + rxEng.toString());
 *   }
 */

    // evaluate/execute script (i.e. pass script to its engine)
    // scriptcode is stored in 'script', language in 'lang'
    // allowed engines:
    //      rx,rexx,orx   = bsf4rexx scripting engine
    //      js,javascript = javascript engine (rhino)
    //      nrx,netrexx   = netrexx (not yet available)
    //      rb,ruby       = jRuby engine (not yet available)
    // --> more to come
    // mime-types from browser are the better alternative as
    // they can be passed directly
    // --> other types will be removed
    //      x-bsf/x-rexx   = bsf4rexx (-809114057)
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
	//BSFEngine currentEngine;

	final String actLangStringPriv=actLangString;
//	final String scriptPriv=script;
	
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
			return null;							}
		});  	


		// print the recieved scripts code for debugging purpose
		System.out.println("--[executeScript]--> Script code start");
    		System.out.println("--[eS-scriptCode]--> " + script);
    		System.out.println("--[executeScript]--> Script code end");
    
		// now just pass the script to the loaded engine
		try {
			evalEngine.eval("",0,0,script);
    		} catch (Exception e) {
			System.out.println("--[executeScript]--> unknown error in scriptEval");
			e.printStackTrace();
    		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	// Method code below is not necessary any more
/*
 *	// switch statement for eval/exec necessary
 *	try {
 *	    // use scripting engine similar to loadScriptingEngine
 *	    // ie switch case
 * switch (lang.hashCode()) {
 *	    case 3654:
 *	    case 3497075:
 *	    case -809114057:	{
 *		// load rexx engine if not available
 *		System.out.println("--[executeScript]--> language: " + lang + " - hashCode: " + lang.hashCode());
 *		BSFEngine rxEng=mgr.loadScriptingEngine("rexx");
 *		//		System.out.println(rxEng.eval("",0,0,"parse version a;b=bsfversion();c=bsf('Version');return a b c"));
 *		
 *		// explicitly loading BSFFunctions seems to be necessary on linux
 *		rxEng.eval("",0,0,"if rxfuncquery(\"BSF\") then\n do \n call rxFuncAdd \"BsfLoadFuncs\", \"BSF4Rexx\", \"BsdLoadFuncs\" \n call BsfLoadFuncs \n end");
 *		rxEng.eval("",0,0,script);
 *	    };
 *		break;
 *		
 *		/** insert other scripting engines here
 *		    case 3401:jsEng.eval(??);
 *		    case 188995949:jsEng.eval(??);
 *		
 *	    }
 *	} catch (Exception e) {
 *	    e.printStackTrace();
 *	}
 */  
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
	Object paramArray[]=new Object[1];
	paramArray[0]=objIdentifier;
	JSObject dOMObject=(JSObject) jsDocument.call("getElementById",paramArray);
	if (dOMObject==null) {
	    dOMObject=(JSObject) jsDocument.call("getElementByName",paramArray);
	}

	// debug
	System.out.println("--[getDOMObject]-->got document");
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

   
// all commented out, engines are loaded on the fly 
// here comes the load scripting engines part -----------------------------------------------------
// each engine's got its own private method, engines are ONLY loaded when they are not available
// (if-statement in method line 1), scripting engines are loaded with bsfmanager.loadScriptingEngine
// and stored in their respective properties (rxEng,jsEng,rbEng)
/*    private void loadRxEngine() {
 *	if (rxEng==null) {
 *	    try {
 *		System.out.println("--[loadRxEngine]--> Trying to load rexx engine");
 *		rxEng=mgr.loadScriptingEngine("rexx");
 *
 *		//		System.out.println(rxEng.eval("",0,0,"parse version a;b=bsfversion();c=bsf('Version');return 1 a b c"));
 *		//		System.out.println(rxEng.eval("",0,0,"parse version a;b=bsfversion();c=bsf('Version');return 2 a b c"));
 *		// System.out.println(rxEng.toString());
 *		//		rxEng=mgr.loadScriptingEngine("rexx");
 *		//		System.out.println(rxEng.toString());
 *		//		System.out.println(rxEng.eval("",0,0,"parse version a;b=bsfversion();c=bsf('Version');return 3 a b c"));
 *		System.out.println("--[loadRxEngine]-->" + rxEng.toString());
 *	    } catch (Exception e) {
 *		System.out.println("--[loadRxEngine]--> Loading scripting engine [rexx] failed! Error message:");
 *		e.printStackTrace();
 *	    }
 *	} else {
 *	    System.out.println("--[loadRxEngine]--> Rexx engine is available @ " + rxEng.toString());
 *	}
 *   }
 *
 *   private void loadJSEngine() {
 *	if (jsEng==null) {
 *	    try {
 *		System.out.println("--[loadJSEngine]--> Trying to load javascript engine");
 *		jsEng=mgr.loadScriptingEngine("javascript");
 *	    } catch (Exception e) {
 *		System.out.println("--[loadJSEngine]--> Error loading engine! Stack trace:");
 *		e.printStackTrace();
 *	    }
 *	}
 *   }
 * // end of load scripting engines!------------------------------------------------------------------
 */
}
