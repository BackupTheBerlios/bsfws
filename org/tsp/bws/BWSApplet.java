/******************************************************************************
** Class BWSApplet.java
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
** V0.1   @ 2003-12-15
**
*******************************************************************************
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
	
	// a boolean for switching debugging on/off
	boolean debug=true;
	
	/** the standard constructor, nothing special */
	public BWSApplet() {
		if (debug) {
			System.out.println("[BWSApplet constructor] applet object created ...");
		}
	}
	
	/** init creates a BSFManager, obtains the applet window and registers
	 * the applet and java.System.out to the bsf registry
	 */
	public void init() {
		// first the manager is created
		mgr=new BSFManager();
		if (debug) {
			System.out.println("[BWSApplet-init] BSFManager instantiated: "+mgr);
		}
		
		// register applet and java.System
		mgr.registerBean("BWSApplet",this);
		mgr.registerBean("SystemOut",System.out);
		
		// get the window and register it
		jsWindow=JSObject.getWindow(this);
		if (debug) {
			System.out.println("[BWSApplet-init] got a window: " + jsWindow);
		}
		mgr.registerBean("DocumentWindow",jsWindow);		
	}
	
	/** reads a script form the document and executes it
	 * at the moment, only rexx is used as scripting language
	 * Must be modified to read the script from dom (instead of getting
	 * it passed or from an url)
	 * @param scriptId id of the script tags wherein the script code is stored
	 * @param script the script
	 */
	public void executeScript(String script) {
		// going to an inner class, all variables that shall be accessible
		// must be declared final
		
		try {
			BSFEngine evalEngine = (BSFEngine) AccessController.doPrivileged(new PrivilegedAction() {
				public Object run() {
					BSFEngine currentEngine;
					
					try {
						currentEngine=mgr.loadScriptingEngine("rexx");
						System.out.println("[BWSApplet-executeScript] scripting engine loaded successfully");
						return currentEngine;
					} catch (Exception e) {
						System.out.println("[BWSApplet-executeScript] loading engine failed!");
						e.printStackTrace();
					}
					return null;
				}
			});
			
			// obtain the script
			//JSNode scriptContainer=getNode(scriptId);
			//String script=scriptContainer.getInnerText();
			
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
	
	public JSNode getNode(String nodeId) {
		JSNode tmpJSNode=new JSNode(jsWindow,nodeId);
		return tmpJSNode;
	}	
}