/*********************************************************************
 * A class for handling bws script strings (strings passed from html
 * that are used to invoke bws scripts)
 *********************************************************************
 * the following schemas are allowed for script invokation:
 *   scriptId
 *   returnValue=scriptId
 *   scriptId(parameter,parameter,...)
 *   returnValue=scriptId(parameter,parameter,...)
 ********************************************************************/

package org.tsp.bws;

import java.lang.String;

class ScriptString {
    // class internal representation of the string
    private String scriptString;
    
    // the id of the script referenced by this script string
    private String scriptId;

    /** checks validity of the string and parses it */
    public ScriptString(String passedString) {
	// it would be nice if the constructor checked for duplicate
	// =,( and ) and throw an exception if these occur
	// start indices probably should be +1

	// if no return value is specified (no = in passed String)
    	if (passedString.indexOf("=")<0) {
	    // if no parameters are given (no ( in passed String))
	    if (passedString.indexOf("(")<0) {
		scriptId=passedString.trim();
	    } else { 
		// parameters are given
		scriptId=passedString.substring(0,passedString.indexOf("("));
		scriptId=scriptId.trim();
		this.parseParameters();
	    }
	} else {
	    // any parameters?
	    if (passedString.indexOf("(")<0) {
		// no parameters
		scriptId=passedString.substring(passedString.indexOf("="),passedString.length());
		scriptId=scriptId.trim();
	    } else {
		// parameters
		scriptId=passedString.substring(passedString.indexOf("="),passedString.indexOf("("));
		scriptId=scriptId.trim();
		this.parseParameters();
	    }
	}
	scriptString=passedString;
    }

    private void parseParameters() {
	String parametersString=scriptString.substring(scriptString.indexOf("("),scriptString.indexOf(")"));
	// now split with parametersString.splite(",");
	// return a String array

    }

    /** returns the script id of the referenced script */
    public String getScriptId() {
	return scriptId;
    }
}
