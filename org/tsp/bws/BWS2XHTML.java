/******************************************************************************
 * Class BWS2XTHML.java
 * 2003-12-27 by Tobias Specht
 ******************************************************************************
 * A small application that reades a BWS-XHTML document from an URL and
 * rewrites it to a browser-interpretable XHTML document. Uses BWSDocument.
 * Prints final document to the standard output
 *
 * Invokation: java BWSRewriter URLToRewrite
 *       e.g.: java BWSRewriter http://some.server.com/document.xhtml
 ******************************************************************************
 *
 * Changelog
 * ---------
 *
 * V0.1   @ 2003-12-27
 *
*******************************************************************************
 *
 * - Add option to write output directly to a file
 *
 ******************************************************************************
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
 ******************************************************************************
 *
 * Contact information
 * -------------------
 *
 * For further information on this class mail me at:
 *
 *     tobi@mail.berlios.de
 *
 * The most recent version of this file is available from
 *
 *     http://bsfws.berlios.de/
 *
 *****************************************************************************/

package org.tsp.bws;

/** 
 * This applications rewrites a BWS document to an BWS/HTML document.
 *
 * @author Tobias Specht
 * @version 1.0
 */
public class BWS2XHTML {
    /** reads a document from an URL and rewrites it as an bws document */

    /** 
     * Main method, calls the methods necessary for document rewriting and 
     * writes the document to the standard output from where it can be
     * redirected to a file.
	 *
     * @param args String array of command line arguments, first parameter
     * is the URL from which the document shall be read, second parameter
     * (optional) specifies to which file the output shall be written.
     * <i>(This is not implemented yet)</i>
     */
    public static void main(String args[]) {
	  char debug=0;
	  BWSDocument docToRewrite=new BWSDocument();


	try {
	    docToRewrite.readDocumentFromURL(args[0]);
	    // write to standard error
	    if (debug>0) {
	      System.err.println(docToRewrite.toString());
	    }
	} catch (Exception e) {
	    // write to standard error
	    System.out.println("[Error] Exception while building document");
	}
	docToRewrite.getScriptNames();
	docToRewrite.rewriteScriptCalls();

	// if a second parameter is given, write the output to a file
	// with the given name
	if (args.length>1) {
	    String fileName=args[1];
	    // open file and write (not implementet)
	} else {
	    // print file to stdOut
	    docToRewrite.printDocumentSource();
	}
    }
}
