/******************************************************************************
** Class RewriterApplet
** 2002-11-23 by Tobias Specht
*******************************************************************************
 * This applet rewrites a BWS document to a BWS/HTML document and writes it
 * to a newly opened window.
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

// java standard classes
import java.applet.*;
import java.io.*;

// dom4j
import org.dom4j.io.XMLWriter;

// and liveconnect
import netscape.javascript.*;


/**
 * This applet rewrites a BWS document to a BWS/HTML document and writes it
 * to a newly opened window. The applet version of {@link org.tsp.bws.BWS2XHTML}.
 * Requires dom4j installed.
 *
 * @author Tobias Specht
 * @version 1.0
 */
public class RewriterApplet extends Applet {
  /** 
   * No changes to the method of {@link java.applet.Applet}.
   */
  public void init() {}
  /** 
   * No changes to the method of {@link java.applet.Applet}.
   */  
  public void start() {}
  /** 
   * No changes to the method of {@link java.applet.Applet}.
   */
  public void stop() {}
  /** 
   * No changes to the method of {@link java.applet.Applet}.
   */
  public void destroy() {}

  /** 
   * Read and transform the document directly.
   * Reads the document from the specified URL (available from
   * the HTML input field with the id <tt>filename</tt>), transform it
   * to a BWS/HTML document and open it in a new browser window.
   */
  public void rewrite() {
    JSObject myWindow=JSObject.getWindow(this);

    JSNode filenameNode=new JSNode(myWindow,"filename");

    System.out.println(filenameNode);

    String filename=filenameNode.getAttribute("value");
    BWSDocument docToRewrite=new BWSDocument();
    try {
      docToRewrite.readDocumentFromURL(filename);
    } catch (Exception e) {
      e.printStackTrace();
    }
    docToRewrite.getScriptNames();
    docToRewrite.rewriteScriptCalls();
    docToRewrite.appendApplet();
    String rewrittenString=docToRewrite.getDocument();

    // for a first test, just open a window
    Object[] paramObjects=new Object[2];
    paramObjects[0]="about:blank";
    paramObjects[1]="bwsWindow";
    JSObject newWindow=(JSObject)myWindow.call("open",paramObjects);
    JSObject newDocu=(JSObject)newWindow.getMember("document");
//    System.out.println(newDocu);
	paramObjects[0]="text/html";
	paramObjects[1]=null;
    newDocu.call("open",paramObjects);
    paramObjects=new Object[1];
    //delete the xml declaration
    rewrittenString=rewrittenString.substring(rewrittenString.indexOf("\n")+1);

	//replace / in tags with \/
	rewrittenString=rewrittenString.replaceAll("</","<\\/");
	rewrittenString=rewrittenString.replaceAll("/>"," \\/>");
    //replace &apos; with '
    paramObjects[0]=rewrittenString.replaceAll("&apos;","'");


    System.out.println((String)paramObjects[0]);
    newDocu.call("write",paramObjects);
    newDocu.call("close",paramObjects);
  }
  
  /** 
   * Read and transform the document using a file.
   * Use a file for temporary saving it. Requires permission to read
   * <tt>deployment.user.tmpdir</tt> system property as well as
   * permission to create a file in this directory.
   */
  public void rewriteFile() {
    JSObject myWindow=JSObject.getWindow(this);

    JSNode filenameNode=new JSNode(myWindow,"filename");

    System.out.println(filenameNode);

    String filename=filenameNode.getAttribute("value");
    BWSDocument docToRewrite=new BWSDocument();
    try {
      docToRewrite.readDocumentFromURL(filename);
    } catch (Exception e) {
      e.printStackTrace();
    }
    docToRewrite.getScriptNames();
    docToRewrite.rewriteScriptCalls();
    docToRewrite.appendApplet();
    String rewrittenString=docToRewrite.getDocument();

	//insert space before ending /
	rewrittenString=rewrittenString.replaceAll("/>"," />");
    //replace &apos; with '
    rewrittenString=rewrittenString.replaceAll("&apos;","'");

	String documentURL="about:blank";
	//obtain tmp path
	try {
	  String tmpPath=System.getProperty("deployment.user.tmpdir");
	  File dir = new File(tmpPath);
	  File tmpFile = new File(dir, "theBWSDocument.html");

	  try {
	  	FileOutputStream outStream = new FileOutputStream(tmpFile);
	  	outStream.write(rewrittenString.getBytes());
	  } catch (Exception e) {
	  	e.printStackTrace();
	  }

      documentURL = tmpFile.toURL().toString();
	} catch (Exception e) {
	  e.printStackTrace();
	}

	Object[] paramObjects=new Object[2];
    paramObjects[0]=documentURL;
    paramObjects[1]="bwsWindow";
    JSObject newWindow=(JSObject)myWindow.call("open",paramObjects);
    JSObject newDocu=(JSObject)newWindow.getMember("document");
// for a first test, just open a window
//    Object[] paramObjects=new Object[2];
//    paramObjects[0]="about:blank";
//    paramObjects[1]="bwsWindow";
//    JSObject newWindow=(JSObject)myWindow.call("open",paramObjects);
//    JSObject newDocu=(JSObject)newWindow.getMember("document");
//    System.out.println(newDocu);
/*	paramObjects[0]="text/html";
	paramObjects[1]=null;
    newDocu.call("open",paramObjects);
    paramObjects=new Object[1];
    //delete the xml declaration
    rewrittenString=rewrittenString.substring(rewrittenString.indexOf("\n")+1);

	//replace / in tags with \/
//	rewrittenString=rewrittenString.replaceAll("</","<\\/");
    System.out.println((String)paramObjects[0]);
    newDocu.call("write",paramObjects);
    newDocu.call("close",paramObjects);*/
  }



}