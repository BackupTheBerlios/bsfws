/******************************************************************************
 * Class BWSRewriter.java
 * 2002-10-17 by Tobias Specht
*******************************************************************************
 * A small application that reades a BWS-XHTML document from an URL and 
 * rewrites it to a browser-interpretable XHTML document. Uses BWSDocument.
 * Prints final document an several status messages to standard out.
 * 
 * Invokation: java BWSRewriter URLToRewrite
 *       e.g.: java BWSRewriter http://some.server.com/document.xhtml
*******************************************************************************
 *
 * Changelog
 * ---------
 *
 * V0.1   @ 2003-10-17
 *
*******************************************************************************
 *
 * - Add option to output only the final document or write document to file
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
 * For further information on this class mail me at:
 *
 *     tobi@mail.berlios.de
 *
 * The most recent version of this file is available from
 *
 *     http://bsfws.berlios.de/
 *
******************************************************************************/

package org.tsp.bws;

public class BWSRewriter {
	/** reads a document from an URL and rewrites it as an bws document */

	/** main method
	 * @param args String array of command line arguments
	 */
	public static void main(String args[]) {
		BWSDocument docToRewrite=new BWSDocument();
		try {
			docToRewrite.readDocumentFromURL(args[0]);
			System.out.println(docToRewrite.toString());
		} catch (Exception e) {
			System.out.println("[Error] Exception while building document");
		}
		docToRewrite.printDocumentSource();
		docToRewrite.getScriptNames();
		docToRewrite.printVector();
		docToRewrite.rewriteScriptCalls();
		docToRewrite.printDocumentSource();
	}
}
