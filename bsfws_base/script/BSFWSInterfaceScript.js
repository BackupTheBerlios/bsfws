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
** V0.1final        -         Handlers are installed, forwarding to BSF is not implemented
**                          yet
** V0.2alpha          -         Removing duplicates from Handler included
** V0.2final        -         some errors removed (global replacement necessary due to
**                        unduplicated array)
** V0.3alpha.1         -        testing forwarding to bsf
** V0.3alpha.2  -         actual forwarding
** V0.3beta.1        -         forwarding works
** V0.3beta.2        -         some code 'cleaning', renamed to installScriptingHandlers
** V0.3rc.1        @ 2002-12-20
**                -         separated removeDuplicates from installScriptingHandlers-
**                            function,
**                -         renamed some array to clarify their use or make them more
**                            generic (i.e. replace 'rexx' with 'script')
**                -         extended changelog to contain dates of change
**                -         changed layout of the comment/information part
** V0.3rc.2 @ 2003-01-20
**                -        renamed installScriptingHandlers() to initBSFWS()
**                -        removed parameter from initBSFWS(), document body is now obtained
**                        with document.getElementById("").innerHTML
**                -         separated buildHandler() from initBSFWS()
**                -        added multi-language capability to buildHandler()
**                -        some code 'cleaning' to improve understandability
**                -        changed filename to "BSFWSInterfaceScript.js"
** V0.3final = V0.30 @ 2003-01-21
**                -        some debugging
**                    -           new numbering of versions
** V0.31         @ 2003-01-23
**                -        added getLanguage() function to enable parsing of
**                        used languages from the html-document
**                -        created new global variable tempArray to enable passing
**                        of arrays between functions possible on IE, rewrote
**                        array passing accordingly
** V0.32        @ 2003-02-03
**                -        modified initBSFWS, now script handlers for all
**                        languages found by getLanguages are installed (all
**                        scripts are passed to the rexx engine at the moment,
**                        this will be changed soon)
** V0.33        @ 2003-04-01
**                -        first version working on IE, atm different versions are
**                        necessary for Mozilla and IE
**                -        added </object>-replace code to stop IE forgeting parameters
**                -        some changes to <object> tag to make it work on IE
**                -        added getBrowserType for central differenciation between
**                        different browsers
**                -        handler scripts are not working on internet explorer
**
*******************************************************************************
**
** Planned improvements - Release criteria:
** ----------------------------------------
**
**  - support further BSF languages (0.4final)
**  - support Opera (as soon as innerHTML works there)
**  - read used languages from a used MIME-types (x-bsf/x-lang)
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
** tobias.specht@student.uni-augsburg.de
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
var browserType;

function initBSFWS() {

        // determine used browser:
        //         0 = mozilla/gecko
        //        1 = internet explorer
        //         -1 = not identified
        browserType=getBrowserType();

        code=document.getElementById('the_body').innerHTML;

        // getUsedLanguages
        usedLanguages=new Array();
        getLanguages();
        usedLanguages=tempArray;

        // get script references
        scriptReferencesMultiple=new Array();
        scriptReferencesCurrent=new Array();
        // for each language
        for (getReferencesCounter=0;getReferencesCounter<usedLanguages.length;getReferencesCounter++) {

                // browser specific code necessary, IE forgets double-quotes!
                if (browserType==0) {
                        tempRegExString = '"' + usedLanguages[getReferencesCounter] + ':\\w+"';
                }

                if (browserType==1) {
                        tempRegExString = usedLanguages[getReferencesCounter] + ':\\w+';
                }

                tempRegEx=new RegExp(tempRegExString,"g");
                // get script references from code
                scriptReferencesCurrent=code.match(tempRegEx);
                if (scriptReferencesCurrent!=null) {
                        scriptReferencesMultiple=scriptReferencesMultiple.concat(scriptReferencesCurrent);
                }
        }

        // remove duplicates from script references
        tempArray=scriptReferencesMultiple;
        removeDuplicates();
        scriptReferences=tempArray;

        functionnames=new Array();

        // replace for with foreach from scriptReferences
        // must be done for each language
        for (counter=0;counter<scriptReferences.length;counter++) {
                curstring=scriptReferences[counter];
                // curstring="language:script"
                // to consider: internet explorer forgets double-quotes!
                // not necessary on mozilla, browser specific code

                // "6","5" must be replaced by language.length for scripting
                // language independance,
                // browser specific code, mozilla is 6,curstring.length-1
                if (browserType==0) {
                        functionname=curstring.substring(6,curstring.length-1);
                } else if (browserType==1) {
                        functionname=curstring.substring(5,curstring.length);
                }

                // functionname=script
                // names of functions must not contain any brackets

                // soll rexx direkt ausgeführt werden, wäre es notwendig,
                // routinen/methodenaufrufe eindeutig zu kennzeichnen!

// different with dynamic handler-calling
//                replacementstring='javascript:' + functionname + '()';

// dynamic version
                replacementstring="javascript:callBSF('" + functionname + "')";

                theRegEx=new RegExp(curstring,"g");
                code=code.replace(theRegEx,replacementstring);


/*
 *                // JavaScript handlers are built with buildHandler (expects engine and functionname);
 *                if (browserType==0) {
 *                        buildHandler("rx", functionname);
 *                } else if (browserType==1) {
 *                        code=buildHandler("rx", functionname);
 *                }
 */
        }

        // rewriting the document and initializing BSF
        // no more language dependant code from here

        // internet explorer "forgets" <param code="" ...> as well as "scriptable" and "mayscript"
        // workaround: replace "</object>" with "<param ...></object>" with an regular expression
        if (browserType==1) {
                endObjectRegExp=new RegExp("</OBJECT>","g");
                code=code.replace(endObjectRegExp,'<param name="code" value="bsfWSInterfaceApplet.class"><param name="mayscript" value="true"><param name="scriptable" value="true"></object>');
        }

        document.getElementById('the_body').innerHTML=code;
        //alert(document.getElementById('the_body').innerHTML);
        initBSF();
}

// build and install handler function code, probably not necessary anymore
function buildHandler(engine, funcName) {
        // first:         browser gateway
        // --> mozilla-based via 'new Function()'
        if (browserType==0) {
                funcCode="try {";
                funcCode+="call" + engine + "(document.getElementById(\'" + funcName + "\').innerHTML);";
                funcCode+="} catch (e) {";
                //funcCode+="alert(e);";
                funcCode+="}";

                completeFunction=funcName + '=new Function("' + funcCode + '")';

                try { eval(completeFunction); } catch (e) { alert (e); }
        }

        // --> internet explorer via 'script DEFER'
        if (browserType==1) {
                funcCode='<script language="javascript" type="text/javascript" DEFER id="' + funcName + '_js">\n';
                funcCodeCore='function ' + funcName + '() {\n';
                funcCodeCore+="call" + engine + "(document.getElementById(\'" + functionname + "\').innerHTML);\n";
                funcCodeCore+='}';
                funcCode=funcCode + funcCodeCore + "</script>";
                code=code +  funcCode /*+ code*/;
                eval(funcCodeCore);
                return code;
        }

        // --> internet exploer via 'node'-Object
        if (browserType==2) {
                // use document.createElement to create a script
                theScript=document.createElement("script");

                // create a 'textNode' containing the script
                funcCode='function ' + funcName + '() {';
                funcCode+="call" + engine + "(document.getElementById(\'" + functionname + "\').innerHTML);";
                funcCode+='}\n';

        }


        // --> add further browsers here ;)
}


function initBSF() {
        if (navigator.userAgent.indexOf("Gecko")>0) {
                bsfWSInterfaceApplet=document.getElementById('bsfWSInterfaceApplet');
        }

          bsfWSInterfaceApplet.registerWindow(self);
        bsfWSInterfaceApplet.registerDocument(document);
//        bsfWSInterfaceApplet.appletToBSFReg();
}

function callrx(rexxcode) {
        bsfWSInterfaceApplet.executeScript(rexxcode,"rx");
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
        // languages can be determined by all script types
        // languages are: x-bsf/x-language
        //                          ^^^^^^^^
        // match for languages: /"x-bsf\/x-\w+"/g
        // then do a remove duplicates or resulting array
        // and search for each languages handlers individually

        // read complete document into a variable for parsing by the script
        theDocument=document.getElementById("the_document");
        //alert(theDocument);
        theDocument=document.getElementById("the_document").innerHTML;

        // create an array where the used languages are stored
        usedLanguagesDup=new Array();

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
        //alert("hi");
        //alert(navigator.userAgent);
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
        //alert(document.getElementById(idToCall).innerHTML);
        eval(callrx(document.getElementById(idToCall).innerHTML));
}
