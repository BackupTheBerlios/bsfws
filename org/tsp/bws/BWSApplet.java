/******************************************************************************
 * Class BWSApplet.java
 * 2002-11-23 by Tobias Specht
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
 * - multi-scripting-engine support
 * - use apply instead of eval
 * - parse script invokation for arguments and return value
 * - execute code using BSFManager (instead of BSFEngine)
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
   int debugLevel=1;
	
   /** the standard constructor, nothing special */
   public BWSApplet() {
      if (debugLevel>0) {
	 System.out.println("[BWSApplet constructor] applet object created ...");
      }
   }
   
   /** init creates a BSFManager, obtains the applet window and registers
    * the applet and java.System.out to the bsf registry
    */
   public void init() {
      // first the manager is created
      mgr=new BSFManager();
      if (debugLevel>0) {
	 System.out.println("[BWSApplet-init] BSFManager instantiated: "+mgr);
      }
      
      // register applet and java.System
      mgr.registerBean("BWSApplet",this);
      mgr.registerBean("SystemOut",System.out);
      
      // get the window and register it
      jsWindow=JSObject.getWindow(this);
      if (debugLevel>0) {
	 System.out.println("[BWSApplet-init] got a window: " + jsWindow);
      }
      mgr.registerBean("DocumentWindow",jsWindow);		
   }
   
   /** reads a script form the document and executes it
    * at the moment, only rexx is used as scripting language
    * Must be modified to read the script from dom (instead of getting
    * it passed or from an url)
    * @param scriptId id of the script tags wherein the script code is stored
    * @param script the string specifing script id, return values and arguments
    */
   // code execution could probably also be done by the BSFManager, i.e.
   // without explicitly loading a scripting engine
   public void executeScript(String scriptString) {
      // going to an inner class, all variables that shall be accessible
      // must be declared final

       // create a ScriptString
       ScriptString currentScriptString=new ScriptString(scriptString);
       
       // obtain script id from the script string
       String scriptId=currentScriptString.getScriptId();

      // store script language (=scripting engine) to a final var to
      // be able to use it in doPrivileged
      final String scriptingEngine=this.getScriptingEngine(scriptId);

      try {
	 BSFEngine evalEngine = (BSFEngine) AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
	       BSFEngine currentEngine;
	       
	       try {

		   // use scriptingEngine var
		   // currentEngine=mgr.loadScriptingEngine("rexx");
		   currentEngine=mgr.loadScriptingEngine(scriptingEngine);
		   System.out.println("[BWSApplet-executeScript] scripting engine loaded successfully");
		   return currentEngine;
	       } catch (Exception e) {
		   System.out.println("[BWSApplet-executeScript] loading engine failed!");
		   e.printStackTrace();
	       }
	       return null;
	    }
	 });
	 
	 // lookup script from the given id
	 //JSNode scriptContainer=getNode(scriptId);
	 //String script=scriptContainer.getInnerHTML();
	 String script=scriptId;
	 
	 System.out.println("[BWSApplet-executeScript] script code");
	 System.out.println(script);
	 System.out.println("[BWSApplet-executeScript] script code end");
	 
	 // execute script
	 try {
	    evalEngine.eval("",0,0,script);
	 } catch (Exception e) {
	    System.out.println("[BWSApplet-executeScript] exception while trying to execute");
	 }
      } catch (Exception e) {
	 System.out.println("[BWSApplet-executeScript] exception at ?");
      }
   }
   
   /** returns the html/xml node with the specified id
    * @param nodeId html/xml id attribute of the desired node
    */ 
   public JSNode getNode(String nodeId) {
      JSNode tmpJSNode=new JSNode(jsWindow,nodeId);
      return tmpJSNode;
   }

    /* THIS METHOD IS NOT USED ANY MORE BUT EXTERNALISED TO ScriptString CLASS
     * WILL BE DELETED ON NEXT REVISION
     */
    public String parseScriptString(String scriptString) {
	return "Hallo";
    }

    /** reades the type attribute of the script and parses it for the scripting engine 
     * @scriptId id of the script tag
     */
    public String getScriptingEngine(String scriptId) {
	JSNode tmpJSNode=new JSNode(jsWindow,scriptId);
	String typeString=tmpJSNode.getAttribute("type");

	

	// get the relevant substring from the typestring
	return typeString;
    }
}
