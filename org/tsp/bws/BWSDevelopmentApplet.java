/******************************************************************************
 * Class BWSDevelopmentApplet.java
 * 2003-12-02 by Tobias Specht
*******************************************************************************
 * Test applet for Java DOM communication and the JSNode class. Demonstrates
 * manipulating a in-browser document using JSNode and LiveConnect.
*******************************************************************************
 *
 * Changelog
 * ---------
 *
 * V0.1   @ 2003-12-02
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

// import standard java classes
import java.lang.*;
import java.applet.*;
import java.security.*;

// import liveconnect
import netscape.javascript.*;

/**
 * Test applet for Java DOM communication and the JSNode class. Demonstrates
 * manipulating a in-browser document using JSNode and LiveConnect.
 *
 * @author Tobias Specht
 * @version 1.0
 * @deprecated Only for testing purposes, should not be used anymore.
 */
public class BWSDevelopmentApplet extends Applet {
   // browser window the applet is embedded in
   protected JSObject jsWindow;

   // current document
   private JSObject jsDocument;

   // node used for temporary storage
   private JSNode currentNode;

   /* debug level,
    * 0 = don't debug, almost no output
    * 1 = standard debug
    */
   private int debugLevel=1;

   /** 
    * Applet standard constructor + debug message. 
    */
   public BWSDevelopmentApplet() {
      if (debugLevel>0) {
	  System.out.println("[bwsDevelopmentApplet constructor] applet object created...");
      }
   }

   /** 
    * Overwriten start method, calls getWindow() and tests several of JSNode's methods.
    */
   public void start() {
      // obtain the applets window
      jsWindow=JSObject.getWindow(this);
      if (debugLevel > 0) {
	 System.out.println("[getWindow] window: " + jsWindow.toString());
      }

      JSObject jsDocument=(JSObject)jsWindow.getMember("document");
      if (debugLevel > 0) {
	 System.out.println("[getWindow] document: " + jsDocument.toString());
      }

      JSNode headNode=new JSNode(jsWindow,"testhead");
      JSNode bodyNode=new JSNode(jsWindow,"theBody");
      JSNode dieListe=new JSNode(jsWindow,"dieListe");
      JSNode span=new JSNode(jsWindow,"sp2");

      JSNode newHeading=bodyNode.createElement("h2");
      JSNode headingText=bodyNode.createTextNode("die zweite überschrift");

      // this is not a debug message! it tests if JSNode.hasChildNodes() works
      // headingText does not hava any child nodes, answer should be 'false'
      System.out.println("[main] headingText has child nodes: " + headingText.hasChildNodes());

      newHeading.appendChild(headingText);

      bodyNode.insertBefore(newHeading,span);

      //bodyNode.appendChild(newHeading);

      span.appendData(0,"--append");
      span.deleteData(0,2,5);
      dieListe.removeChild(0);
      dieListe.removeChild(1);

      // again not a debug message! dieListe should have childNodes -> answer should be 'true'
      System.out.println("[main] dieListe has child nodes: " + dieListe.hasChildNodes());

      bodyNode.setAttribute("bgColor","#00ff00");
      bodyNode.setStyleAttribute("color","red");
      headNode.setStyleAttribute("backgroundColor","#88ff88");
      headNode.setStyleAttribute("border","1px solid #555555");
      bodyNode.setStyleAttribute("fontFamily","verdana");

      // two more test (=not debug) messages
      System.out.println("[main] body - color attribute: " + bodyNode.getAttribute("bgColor"));
      System.out.println("[main] body - color attribute node: " + bodyNode.getAttributeNode("bgColor").getNode().getMember("value"));

      headNode.replaceChild("Replaced",0);
      headNode.replaceData(2,7,"---");

      headNode.insertData(0,2,"(TEST)");
   }

   /** 
    * Obtains the document window and references it in jsWindow.
    */
   public void getWindow() {
      jsWindow=JSObject.getWindow(this);
      if (debugLevel>0) {
	 System.out.println("[getWindow] window: " + jsWindow.toString());
      }
   }
}