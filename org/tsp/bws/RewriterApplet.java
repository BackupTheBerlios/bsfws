/******************************************************************************
** Class RewriterApplet
** 2002-11-23 by Tobias Specht
*******************************************************************************
** This applet rewrites a BWS document to a BWS/HTML document and writes it
** to a newly opened window.
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

// dom4j
import org.dom4j.io.XMLWriter;

// and liveconnect
import netscape.javascript.*;

public class RewriterApplet extends Applet {
  public void init() {}
  public void start() {}
  public void stop() {}
  public void destroy() {}

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
    paramObjects[0]=rewrittenString;
    newDocu.call("write",paramObjects);
    newDocu.call("close",paramObjects);
  }
}

