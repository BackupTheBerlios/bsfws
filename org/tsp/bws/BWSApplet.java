/******************************************************************************
 * Class BWSApplet.java
 * 2003-11-15 by Tobias Specht
*******************************************************************************
 * This applet handles DOM->BSF communication, passes scripts from the document
 * to their respective scripting engines and provides methods scripting
 * languages can use to access and manipulate DOM objects
*******************************************************************************
 *
 * Changelog
 * ---------
 *
 * V0.1   @ 2003-12-15
 *
*******************************************************************************
 *
 * Planned improvements
 * --------------------
 *
 * - multi-scripting-engine support (done, give engine via type string)
 * - use apply instead of eval
 * - parse script invokation for arguments and return value
 * - execute code using BSFManager (instead of BSFEngine)?
 *
 * see also bws wiki:
 *   http://openfacts.berlios.de/index-en.phtml?title=BSFWebScripting
 *
*******************************************************************************
 *
 * Licencing Information
 * ---------------------
 *
 * Copyright (C) 2002-2003 Tobias Specht
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * The GNU General Public License is also available on the Web:
 * http://www.gnu.org/copyleft/gpl.html
 *
*******************************************************************************
 *
 * Contact information
 * -------------------
 *
 * For further information on this script mail me at:
 *
 *     tobi@mail.berlios.de
 *
 * The most recent version of this file is available from
 *
 *     http://bsfws.berlios.de/
 *
******************************************************************************/

package org.tsp.bws;

// import some standard java classes
import java.lang.*;
import java.applet.*;
import java.util.*;
import java.security.*;

// the bsf
import com.ibm.bsf.*;

// and liveconnect
import netscape.javascript.*;

public class BWSApplet extends Applet {
   // the bsf manager
   private BSFManager mgr;

   // object for the browser window
   private JSObject jsWindow;

   // an int for setting the debug leve
   // 0 = no debug output
   // 1 = normal debug
   // 2 = additional messages
   private static int debugLevel=5;

   /** the standard constructor, nothing special */
   public BWSApplet() {
     if (debugLevel>0) {
       System.out.println("[BWSApplet.constructor] applet object created ...");
     }
   }

   /** init creates a BSFManager, obtains the applet window and registers
    * the applet and java.System.out to the bsf registry
    */
   public void init() {
      // first the manager is created
      mgr=new BSFManager();
      if (debugLevel>0) {
		System.out.println("[BWSApplet.init] BSFManager instantiated: "+mgr);
      }

      // register applet and java.System
      mgr.registerBean("BWSApplet",this);
      mgr.registerBean("SystemOut",System.out);

      // get the window and register it
      jsWindow=JSObject.getWindow(this);
      if (debugLevel>0) {
	  	System.out.println("[BWSApplet.init] got a window: " + jsWindow);
      }
      mgr.registerBean("DocumentWindow",jsWindow);
   }

   /** reads a script from the docuemnt and passes it to the bsf engine
    * specified in the scripts type attribute.
    * @param scriptString String containing scriptId, returnValue key and parameters keys
    * @param domObject object that is used in script evaluation (usually <em>this</em>)
    */
   public void executeScript(String scriptString, JSObject domObject) {
	 if (debugLevel>0) {
	 	System.out.println("[BWSApplet.executeScript] got this domObject: " + domObject);
	 }

     // create a ScriptString
     ScriptString currentScriptString=new ScriptString(scriptString);

     // obtain script id from the script string
     String scriptId=currentScriptString.getScriptId();

	 // load the scripting engine
	 BSFEngine evalEngine=this.loadScriptingEngine(scriptId);

	 // lookup script from the given id
	 JSNode scriptContainer=getNode(scriptId);
	 String script=scriptContainer.getInnerHTML();

	 if (debugLevel>0) {
	   System.out.println("[BWSApplet.executeScript] script code");
	   System.out.println(script);
	   System.out.println("[BWSApplet.executeScript] script code end");
     }

	// get parameters
	String[] paramArray=currentScriptString.getParameters();

	Vector argumentVector=evaluateParameters(paramArray,domObject);

	// parameters must be accessible in scripts under know references
	// to keep scriptcalls easy (parameter nameing unnecessary)
	// these names are predefined as argument+number, eg. argument1, argument2, ...
	// starting with argument0
	Vector namesVector=new Vector();
	
	int nrOfArguments=argumentVector.size();
    for (int argumentCounter=0;argumentCounter<nrOfArguments;argumentCounter++) {
      String nextArgumentName="argument" + String.valueOf(argumentCounter);
      if (debugLevel>1) {
      	System.out.println("[BWSApplet.executeScript] names vector gets this string: " + nextArgumentName);
      }
      namesVector.add(nextArgumentName);
    }

    // the return value of the apply function will be stored to the bsf registry
    // under the key specified in the ScriptString
    // get this return value key from ScriptString:
	String returnKey=currentScriptString.getRetKey();
      
	// execute script
	try {
	  // for parameter use and returning values, use apply()
	  // retVal=evalEngine.apply()
	  /* method prototype for apply()
	   * public Object apply(String source,
       *                     int lineNo,
       *                     int columnNo,
       *                     Object funcBody,
       *                     Vector namesVec,
       *                     Vector argsVec)
       *    throws org.apache.bsf.BSFException
       * 
       * script code must be in 'funcBody'
       * namesVec contains the names of the arguments passed
       * argsVec contains the values
       */
	   
	   // the script is executed, return value referenced by applyReturnedObject
	   Object applyReturnedObject=evalEngine.apply("",0,0,script,namesVector,argumentVector);
	   
	   if (debugLevel>0) {
	   	 System.out.println("[BWSApplet.executeScript] apply returned the following object: " + applyReturnedObject);
	   }
	   
	   // the returned object now gets referenced in the bsf registry be the specified
	   // key (returnKey) (from where it is accessible by any bsf script)
	   if (applyReturnedObject!=null) {
	     mgr.registerBean(returnKey,applyReturnedObject);
	   }
	 } catch (Exception e) {
	    System.out.println("[BWSApplet-executeScript] exception while trying to execute");
	 }

   }

   /** reads a script from the docuemnt and passes it to the bsf engine
    * specified in the scripts type attribute.
    * @param scriptString String containing scriptId, returnValue key and parameters keys
    */
   // code execution could probably also be done by the BSFManager, i.e.
   // without explicitly loading a scripting engine
   public void executeScript(String scriptString) {
     // create a ScriptString
     ScriptString currentScriptString=new ScriptString(scriptString);

     // obtain script id from the script string
     String scriptId=currentScriptString.getScriptId();

     // load scripting engine
	 BSFEngine evalEngine=this.loadScriptingEngine(scriptId);

	 if (debugLevel>0) {
	   System.out.println("[BWSApplet.executeScript] got this engine: " + evalEngine);
	 }

	 // lookup script from the given id
	 JSNode scriptContainer=getNode(scriptId);
	 String script=scriptContainer.getInnerHTML();

	 System.out.println("[BWSApplet-executeScript] script code");
	 System.out.println(script);
	 System.out.println("[BWSApplet-executeScript] script code end");

	 // get parameters
     String[] paramArray=currentScriptString.getParameters();
     Vector argumentVector=evaluateParameters(paramArray);

     // execute script
	 try {
	   evalEngine.eval("",0,0,script);
	 } catch (Exception e) {
	   System.out.println("[BWSApplet-executeScript] exception while trying to execute");
	 }
   }

   /** returns the html/xml node with the specified id
    * @param nodeId html/xml id attribute of the desired node
    */
   public JSNode getNode(String nodeId) {
      JSNode tmpJSNode=new JSNode(jsWindow,nodeId);
      return tmpJSNode;
   }

    /** reades the type attribute of the script and parses it for the scripting engine
     * @scriptId id of the script tag
     */
    public String getScriptingEngine(String scriptId) {
		JSNode tmpJSNode=new JSNode(jsWindow,scriptId);
		String typeString=tmpJSNode.getAttribute("type");

		// get the relevant substring from the typestring
		// typstring is bsf/engine
		String engineString=typeString.substring(typeString.indexOf("/")+1,typeString.length());

		return engineString;
    }

  private BSFEngine loadScriptingEngine(String scriptId) {
     // going to an inner class, all variables that shall be accessible
     // must be declared final

    // store script language (=scripting engine) to a final var to
    // be able to use it in doPrivileged
    final String scriptingEngine=this.getScriptingEngine(scriptId);

    try {
      BSFEngine evalEngine = (BSFEngine) AccessController.doPrivileged(new PrivilegedAction() {

        public Object run() {
          BSFEngine currentEngine;

          try {
            currentEngine=mgr.loadScriptingEngine(scriptingEngine);
		    if (debugLevel>0) {
              System.out.println("[BWSApplet.loadScriptingEngine] scripting engine loaded successfully");
              System.out.println("[BWSApplet.loadScriptingEngine]  +- engine: " + currentEngine);
            }
            return currentEngine;
          } catch (Exception e) {
            System.out.println("[BWSApplet.loadScriptingEngine] loading engine failed!");
            e.printStackTrace();
          }
          return null;
        }
      });
      return evalEngine;
    }  catch (Exception e) {
      System.out.println("[BWSApplet.loadScriptingEngine] exception at ?");
    }
    return null;
  }

  private Vector evaluateParameters(String[] paramArray, JSObject thisObject) {
  	if (debugLevel>1) {
  	  System.out.println("[BWSApplet.evaluateParameters] just entered evaluateParameters");
  	}

	if (debugLevel>0) {
	  System.out.println("[BWSApplet.evaluateParameters] got this DOM object: " + thisObject.toString());
	}

    Vector paramVector=evaluateParameters(paramArray);

    // check every element in the Vector if it equals 'this'
    for (int paramCounter=0;paramCounter<paramVector.size();paramCounter++) {
      try {
        String tempString=(String) paramVector.elementAt(paramCounter);

        if (debugLevel>0) {
          System.out.println("[BWSApplet.evaluateParameters] got this from the Vector: " + tempString);
        }

        if (tempString.equals("this")) {
          // replace 'this' with the JSObject handed over
          System.out.println("[BWSApplet.evaluateParameters] found 'this', replacing with: " + thisObject);
          paramVector.setElementAt(thisObject,paramCounter);
        }
      } catch (Exception e) {
      	System.out.println("[BWSApplet.evaluateParameters] caught an exception, object probably wasn't a String, ignoring object");
      }
    }

    return paramVector;
  }

  private Vector evaluateParameters(String[] paramArray) {
  	if (debugLevel>1) {
  	  System.out.println("[BWSApplet.evaluateParameters] just entered evaluateParameters");
  	}

  	if (debugLevel>0) {
  	  System.out.println("[BWSApplet.evaluateParameters] paramArray has #" + paramArray.length + "# Elements");
  	}

  	// create a new vector the size of the current array
  	Vector paramVector=new Vector(paramArray.length);

  	// fill the vector with n nulls
  	for (int paramCounter=0;paramCounter<paramArray.length;paramCounter++) {
  	  paramVector.add(null);
  	}

  	for (int paramCounter=0;paramCounter<paramArray.length;paramCounter++) {
 	  String currentString=paramArray[paramCounter];

 	  if (debugLevel>1) {
 	    System.out.println("[BWSApplet.evaluateParameters] current string: " + currentString);
 	  }

 	  // if the current string is enclosed in quotation marks, strip the quotation marks and use it as String
  	  if ((currentString.indexOf("\"")==0) && (currentString.lastIndexOf("\"")==currentString.length())) {
  	    String paramString=currentString.substring(1,currentString.length()-1);
  	    System.out.println("[BWSApplet.evaluateParameters] parameter string: " + paramString);
  	    paramVector.setElementAt(paramString,paramCounter);
  	  } else {
  	  	// try to lookup bean in the bsf registry
  	  	Object tmpObject=(Object)this.mgr.lookupBean(currentString);

        if (debugLevel>0) {
          System.out.println("[BWSApplet.evaluateParameters] registry object: " + tmpObject);
        }

		// if the object is != null, put it on the array
		if (tmpObject!=null) {
		  if (debugLevel>1) {
		    System.out.println("[BWSApplet.evaluateParameters] +- object was != null -> Vector");
		  }
          paramVector.setElementAt(tmpObject,paramCounter);
        } else {
	  	  JSNode tempNode=this.getNode(currentString);

	  	  // if the string references an existing node, use it
	  	  if (tempNode.getNode()!=null) {
	  	    System.out.println("[BWSApplet.evaluateParameters] +- found a JSNode -> Vector");
	  	    System.out.println("[BWSApplet.evaluateParameters] +- JSNode was: " + tempNode.getNode().toString());
	  	    paramVector.setElementAt(tempNode,paramCounter);
	  	  } else {
	  	    // else use the string
	  	    paramVector.setElementAt(currentString,paramCounter);
	  	  }
  	    }
  	  }
  	}

  	return paramVector;
  }
}
