/******************************************************************************
** Script BSFWSInterfaceScript.js
** 2002-11-23 by Tobias Specht
*******************************************************************************
** This script replaces Rexx function-calls in HTML-Document-Bodys with
** javascript handlers that forward the rexx code to a BSF Rexx interpreter
*******************************************************************************
**
** Changelog
** ---------
**
** V0.1final    -       Handlers are installed, forwarding to BSF is not implemented
**                      yet
** V0.2alpha    -       Removing duplicates from Handler included
** V0.2final    -       some errors removed (global replacement necessary due to
**                      unduplicated array)
** V0.3alpha.1  -       testing forwarding to bsf
** V0.3alpha.2  -       actual forwarding
** V0.3beta.1   -       forwarding works
** V0.3beta.2   -       some code 'cleaning', renamed to installScriptingHandlers
** V0.3rc.1     @ 2002-12-20
**              -       separated removeDuplicates from installScriptingHandlers-
**                      function,
**              -       renamed some array to clarify their use or make them more
**                      generic (i.e. replace 'rexx' with 'script')
**              -       extended changelog to contain dates of change
**              -       changed layout of the comment/information part
** V0.3rc.2      @ 2003-01-20
**              -       renamed installScriptingHandlers() to initBSFWS()
**              -       removed parameter from initBSFWS(), document body is now obtained
**                      with document.getElementById("").innerHTML
**              -       separated buildHandler() from initBSFWS()
**              -       added multi-language capability to buildHandler()
**              -       some code 'cleaning' to improve understandability
**              -       changed filename to "BSFWSInterfaceScript.js"
** V0.3final = V0.30 @ 2003-01-21
**              -       some debugging
**              -       new numbering of versions
** V0.31        @ 2003-01-23
**              -       added getLanguage() function to enable parsing of
**                      used languages from the html-document
**              -       created new global variable tempArray to enable passing
**                      of arrays between functions possible on IE, rewrote
**                      array passing accordingly
** V0.32        @ 2003-02-03
**              -       modified initBSFWS, now script handlers for all
**                      languages found by getLanguages are installed (all
**                      scripts are passed to the rexx engine at the moment,
**                      this will be changed soon)
** V0.33        @ 2003-04-01
**              -       first version working on IE, atm different versions are
**                      necessary for Mozilla and IE
**              -       added </object>-replace code to stop IE forgeting parameters
**              -       some changes to <object> tag to make it work on IE
**              -       added getBrowserType for central differenciation between
**                      different browsers
**              -       handler scripts are not working on internet explorer
** V0.4         @ 2003-04-30
**		-	BWS is now almost scripting language independent 
**			(almost:all scripts should atm be linked as 
**			        rexx:script_id, java applet does not load any
**				engines but bsf4rexx)
**		-	used scripting language is read from the script-tags
**			mime-type (not from the link as was the case before 0.4,
**			the mime-type is forwarded to the applet that determines
**			to which engine the script code should be forwarded)
**		-	script works with MSIE, Mozilla-based browsers (Mozilla 1.4
**			and Netscape 7 tested)
** V0.5		@ 2003-05-07, changes contributed by Rony G. Flatscher
**	 	-	id's for body and html tags are not necessary anymore
**		-	html entities used in scripts are decoded (necessary
**			for mozilla/netscape
** V0.51	@ 2003-05-08
**		-	!(no id necessary for the applet anymore)
** V0.52	@ 2003-05-25
** 		-	applet id still necessary
**		-	is 1.0RC1
** V0.53	@ 2003-06-21
**		-	fixed a major bug in executeScript() that broke BWS
**		-	is 1.0RC2
** V0.6		@ 2003-06-25
**		-	scripts can now be called from the initBSFWS function
**			by passing the script name as initBSFWS parameter
**			e.g. initBSFWS("hello_world");
**
*******************************************************************************
**
** Planned improvements
** --------------------
**  - support Opera (as soon as document.getElementById works there)
**
*******************************************************************************
**
** Licensing Information
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
** tobi@mail.berlios.de
**
*******************************************************************************
**
** Global variable
** ---------------
**
** Array tempArray
**
**        this is a dirty workaround for the internet explorer/jscript as it
**        doesn't support passing an array from one function to another (this,
**        of course, is not a problem with mozilla ;)
**        instead of passing the array it is stored in the tempArray where it
**        can be accessed from all functions :(
**
******************************************************************************/

var tempArray;
tempArray=new Array();
var browserType=null;
var theBWSApplet;

function initBSFWS(execThisScript) {
  	theBWSApplet=document.getElementById("bsfWSInterfaceApplet");

        // determine used browser:
        //         0 = mozilla/gecko
        //         1 = internet explorer
        //         -1 = not identified
	browserType=getBrowserType();
	
      	code=document.getElementsByTagName('body')[0].innerHTML;    // ---rgf, 2003-05-03

	// now all bsf scripts are detected and their calls in the document 
	// (bsf:script and #:script) are replaced with calls to
	// callBSF('scriptid')

	// all script tages are stored in an array
	theScriptTags=document.getElementsByTagName("script");
	
	// for each element of the script tag array
	for (theScriptsCounter=0;theScriptsCounter<theScriptTags.length;theScriptsCounter++) {
		// the script type (MIME type) is determined and compared to x-bsf
		// if it is a bsf script (i.e. if type contains x-bsf)
		if (theScriptTags[theScriptsCounter].type.indexOf("x-bsf")>-1) {
			// the script id is obtained (a shortcut is defined)
			curScrId=theScriptTags[theScriptsCounter].id;
			
                	replacementstring="javascript:callBSF('" + curScrId + "')";

			// all code of the document matching 'bsf:scriptid' is replaced with
			// 'callBSF('scriptid') via Regular Expression
			currentReference="bsf:" + curScrId;
			theRegEx=new RegExp(currentReference,"g");
			code=code.replace(theRegEx,replacementstring);

			// the same is done for '#:scriptid'
                        currentReference="#:" + curScrId;
			theRegEx=new RegExp(currentReference,"g");
			code=code.replace(theRegEx,replacementstring);
		}
	}

        // rewriting the document and initializing BSF
        // no more language dependant code from here

        // internet explorer "forgets" <param code="" ...> as well as "scriptable" and "mayscript"
        // workaround: replace "</object>" with "<param ...></object>" with an regular expression
        if (browserType==1) {
                endObjectRegExp=new RegExp("</OBJECT>","g");
                code=code.replace(endObjectRegExp,'<param name="code" value="bsfWSInterfaceApplet.class"><param name="mayscript" value="true"><param name="scriptable" value="true"></object>');
        }

        document.getElementsByTagName('body')[0].innerHTML=code;    // ---rgf, 2003-05-03

/*
 *        var oldNode=document.getElementsByTagName('body')[0];
 *       var oNewNode = document.createElement("body");
 *       oNewNode.innerHTML = code;
 *       oldNode.replaceNode(oNewNode);
 *       alert ("newCode=["+code+"]");
 */

// alert("initBSFWS() after setting body to new code...");

// alert ("before calling initBSF() ..."+new Date());
//        window.setTimeout("eval(initBSF())", 3000);

        initBSF();

	if (execThisScript!="") {
		alert("Trying to exec " + execThisScript);
		callBSF(execThisScript);
	}
}

function initBSF() {
        theBWSApplet.registerWindow(self);
        theBWSApplet.registerDocument(document);
}

function removeDuplicates(duplicateArray) {
        // using a script from planetpdf.com
        // first, all elements are stored in a hash with var[value]=value
        // this automatically removes duplicates
        // declaration of singletonArray is necessary to enable using it as array
        var duplicateArray;
        duplicateArray=new Array();
        duplicateArray=tempArray;

        singletonArray = new Array();

        for (counter=0;counter<duplicateArray.length;counter++) {
                singletonArray[duplicateArray[counter]]=duplicateArray[counter];
        }

        // second, the elements from the unduped hash are copied to
        // a new (numbered) array
        // finalArray of course must also be declared to use the array functionality
        var finalArray=new Array();

        for (var curIndex in singletonArray) {
                finalArray.push(singletonArray[curIndex]);
        }

        tempArray=finalArray;
}

function getLanguages() {
/* old version of this function, new see below
 *       // languages can be determined by all script types
 *       // languages are: x-bsf/x-language
 *       //                        ^^^^^^^^
 *       // match for languages: /"x-bsf\/x-\w+"/g
 *       // then do a remove duplicates or resulting array
 *       // and search for each languages handlers individually
 *
 *       // read complete document into a variable for parsing by the script
 *       //alert(theDocument);
 *       // theDocument=document.getElementById("the_document").innerHTML;
 *       theDocument=document.getElementsByTagName("html")[0].innerHTML;
 */

	// scripting languages are now determined by reading all script tags' type information

	// first an empty array is created
        usedLanguagesDup=new Array();

	// all script tages are stored in another array
	theScriptTags=document.getElementsByTagName("script");
	
	// for each element of the script tag array
	for (theScriptsCounter=0;theScriptsCounter<theScriptTags.length;theScriptsCounter++) {
		// the script type (MIME type) is determined and compared to x-bsf
		if (theScriptTags[theScriptsCounter].type.indexOf("x-bsf")>-1) {
			

			alert(theScriptTags[theScriptsCounter].type);
		}
	}

        // create an array where the used languages are stored


        // get the languages/mime-types from documents
        // alternative for only non-standardized mime-types, i.e. x-.../x-...
        usedLanguagesDup=theDocument.match(/"x-bsf\/x-\w+"|type=x-bsf\/x-\w+(\W|\s)/g);

        // alternative for any x-bsf mime-tye
        //        usedLanguagesDup=theDocument.match(/"x-bsf\/(x-\w+"|\w+")/g);

        // remove duplicates
        tempArray=usedLanguagesDup;
        removeDuplicates();
        usedLanguagesSing=tempArray;

        // strip ("x-bsf/x-) and (")
        for (counter=0;counter<usedLanguagesSing.length;counter++) {
                position=usedLanguagesSing[counter].indexOf("x-bsf");
                usedLanguagesSing[counter]=usedLanguagesSing[counter].substring(position+8,usedLanguagesSing[counter].length-1);
        }

        // return array
        tempArray=usedLanguagesSing;
}

// central browser 'gateway'
function getBrowserType() {
        if (navigator.userAgent.indexOf("Gecko")>0) {
                // mozilla/gecko based browser, return 0
                return 0;
        } else if (navigator.userAgent.indexOf("MSIE")>0) {
                // microsoft internet explorer, return 1
                return 1;
// bad news: opera doesn't work because document.getElementById does
// 		return an 'undefined' object
//        } else if (navigator.userAgent.indexOf("Opera")>0) {
//                // test if mozilla-style js works
//                return 0;
// ----
// use 'mozilla-style' for konqueror
	} else if (navigator.userAgent.indexOf("Konqueror")>0) {
		return 0;
        } else {
                // invalid browser type (could be opera, konqueror, ...)
                return -1;
        }
}

// dynamic replacement for calling bsf-functions
function callBSF(idToCall) {
	scriptTag=document.getElementById(idToCall);

        code=scriptTag.innerHTML;   // seems Netscape turns "<", ">", "&" into entities!

        if (browserType!=1) {
           code=decodeEntities(code);
        }
        // alert (new Date()+" code: "+code);

	theBWSApplet.executeScript(code,scriptTag.type);
}

function decodeEntities (text) {          // ---rgf, 2003-05-05
       re=/&gt;/g;
       text=text.replace(re, ">");
       re=/&lt\;/g;
       text=text.replace(re, "<");
       re=/&amp\;/g;
       text=text.replace(re, "&");
       return text;
}

// not used yet (and not working yet)
// this will be used to obtain the bsfWSInterfaceApplet independent from its id
/*function getApplet() {
 *	documentApplets=document.getElementsByTagName('object');
 *	numberOfApplets=(documentApplets.length);
 *	for (theAppletCounter=0;theAppletCounter<numberOfApplets;theAppletCounter++) {
 *			alert("found the following applet:" + documentApplets[theAppletCounter].code);
 *		if (documentApplets[theAppletCounter].code.indexOf("bsfWSInterfaceApplet")>-1) {
 *
 *			return documentApplets[theAppletCounter];
 *		}
 *	}
 *	alert("necessary applet (bsfWSInterfaceApplet.class) not found! BWS will not work!");
 *	return null;	
 *}
 */
