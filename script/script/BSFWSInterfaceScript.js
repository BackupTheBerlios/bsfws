/******************************************************************************
** Script installScriptingHandlers.js
** 2002-11-23 by Tobias Specht
*******************************************************************************
** This script replaces Rexx function-calls in HTML-Document-Bodys with
** javascript handlers that forward the rexx code to a BSF Rexx interpreter
*******************************************************************************
**
** Changelog
** ---------
**
** V0.1final	- 	Handlers are installed, forwarding to BSF is not implemented
**		  			yet
** V0.2alpha  	- 	Removing duplicates from Handler included
** V0.2final	- 	some errors removed (global replacement necessary due to
**                	unduplicated array)
** V0.3alpha.1 	-	testing forwarding to bsf
** V0.3alpha.2  - 	actual forwarding
** V0.3beta.1	- 	forwarding works
** V0.3beta.2	- 	some code 'cleaning', renamed to installScriptingHandlers
** V0.3rc.1	@ 2002-12-20
**		- 	separated removeDuplicates from installScriptingHandlers-
**		    function,
**		- 	renamed some array to clarify their use or make them more
**		    generic (i.e. replace 'rexx' with 'script')
**		- 	extended changelog to contain dates of change
**		- 	changed layout of the comment/information part
** V0.3rc.2 @ 2003-01-20
**		-	renamed installScriptingHandlers() to initBSFWS()
**		-	removed parameter from initBSFWS(), document body is now obtained
**			with document.getElementById("").innerHTML
**		- 	separated buildHandler() from initBSFWS()
**		-	added multi-language capability to buildHandler()
**		-	some code 'cleaning' to improve understandability
**		-	changed filename to "BSFWSInterfaceScript.js"
**
*******************************************************************************
**
** Planned improvements - Release criteria:
** ----------------------------------------
**
**  - support further BSF languages (0.4final)
**  - split install scripting handlers into several functions (0.3final)
**  - support Opera (as soon as innerHTML works there)
**  - read used languages from a html-tag within the document (possible?)
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

function initBSFWS() {
	code=document.getElementById('the_body').innerHTML;

	// languages can be determined by all script types
	// languages are: x-bsf/x-language
	//						  ^^^^^^^^
	// match for languages: /"x-bsf/x-\w+"/g
	// then do a remove duplicates or resulting array
	// and search for each languages handlers individually

	scriptReferencesMultiple=code.match(/"rexx:\w+"/g);

	// remove duplicates from script references
	scriptReferences=removeDuplicates(scriptReferencesMultiple);
	functionnames=new Array();

	// replace for with foreach from scriptReferences
	// must be done for each language
	for (counter=0;counter<scriptReferences.length;counter++) {
		curstring=scriptReferences[counter];
		// curstring="language:script"

		// "6" must be replaced by language.length for scripting
		// language independance
		functionname=curstring.substring(6,curstring.length-1);

		// functionname=script
		// names of functions must not contain any brackets

		// soll rexx direkt ausgeführt werden, wäre es notwendig,
		// routinen/methodenaufrufe eindeutig zu kennzeichnen!

		replacementstring='javascript:' + functionname + '()';

		// replacementstring=javascript:script

		theRegEx=new RegExp(curstring,"g");
		code=code.replace(theRegEx,replacementstring);

		// JavaScript handlers are built with buildHandler (expects engine and functionname);

		buildHandler("rx", functionname);


/*		// from here on the rexx handler code is build
		// first the solution for mozilla/gecko
		// handler scripts are registered with new Function()

		if (navigator.userAgent.indexOf("Gecko")>0) {
		  	functioncode="try {";
			functioncode+="callrx(document.getElementById(\'" + functionname + "\').innerHTML);";
			functioncode+="} catch (e) {";
			functioncode+="alert(e);";
			functioncode+="}";

			completeFunction=functionname + '=new Function("' + functioncode + '")';

			try { eval(completeFunction); } catch (e) { alert(e) }
		}

		// now Internet Explorer: registring the handler scripts must be done with <script> tags
		if (navigator.userAgent.indexOf("MSIE")>0) {
			code+='<script language="javascript" type="text/javascript" DEFER>\n';
			code+='function ' + functionname + '() {\n';
			code+="callrx(document.getElementById(\'" + functionname + "\').innerHTML);\n";
			code+='}';
			code+="</script>";
		}
	}
*/
	}

	// rewriting the document and initializing BSF
	// no more language dependant code from here
	document.getElementById('the_body').innerHTML=code;
	initBSF();
}

// build and install handler function code
function buildHandler(engine, funcName) {
	// first: 	browser gateway
	// --> mozilla-based via 'new Function()'
	if (navigator.userAgent.indexOf("Gecko")>0) {
		funcCode="try {";
		funcCode+="call" + engine + "(document.getElementById(\'" + funcName + "\').innerHTML);";
		funcCode+="} catch (e) {";
		funcCode+="alert(e);";
		funcCode+="}";

		completeFunction=funcName + '=new Function("' + funcCode + '")';

		try { eval(completeFunction); } catch (e) { alert (e); }
	}

	// --> internet explorer via 'script DEFER'
	if (navigator.userAgent.indexOf("MSIE")>0) {
		funcCode+='<script language="javascript" type="text/javascript" DEFER>\n';
		funcCode+='function ' + funcName + '() {\n';
		funcCode+="call" + engine + "(document.getElementById(\'" + functionname + "\').innerHTML;\n";
		funcCode+='}';
		funcCode+="</script>";
	}

	// --> add further browsers here ;)
}


function initBSF() {
	bsfInterfaceApplet=document.getElementById('bsfInterfaceApplet');
  	bsfInterfaceApplet.registerWindow(self);
	bsfInterfaceApplet.registerDocument(document);
	bsfInterfaceApplet.appletToBSFReg();
}

function callrx(rexxcode) {
	bsfInterfaceApplet.executeScript(rexxcode,"rx");
	// alert("hallo");
}

function removeDuplicates(duplicateArray) {
	// using a script from planetpdf.com
	// first, all elements are stored in a hash with var[value]=value
	// this automatically removes duplicates
	// declaration of singletonArray is necessary to enable using it as array
	singletonArray = new Array;

	for (counter=0;counter<duplicateArray.length;counter++) {
		singletonArray[duplicateArray[counter]]=duplicateArray[counter];
	}

	// second, the elements from the unduped hash are copied to
	// a new (numbered) array
	// finalArray of course must also be declared to use the array functionality
	var finalArray=new Array;

	for (var curIndex in singletonArray) {
		finalArray.push(singletonArray[curIndex]);
	}

	return finalArray;
}
