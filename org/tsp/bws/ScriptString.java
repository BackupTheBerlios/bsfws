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
	
	// set a debug level for this class
	private static int debugLevel=1;

    /** checks validity of the string and parses it */
    public ScriptString(String passedString) {
		// it would be nice if the constructor checked for duplicate
		// =,( and ) and throw an exception if these occur
		// start indices probably should be +1

		if (debugLevel>0) {
			System.out.println("[ScriptString.constructor] entered constructur, passedString: " + passedString);
		}

		// if no return value is specified (no = in passed String)
    	if (passedString.indexOf("=")<0) {
	    	// if no parameters are given (no ( in passed String))
	    	if (passedString.indexOf("(")<0) {
				scriptId=passedString.trim();

				// if debug
				System.out.println("[ScriptString.constructor] no retval, no args, scriptId: " + scriptId);

	    	} else {
			// parameters are given
				scriptId=passedString.substring(0,passedString.indexOf("("));
				scriptId=scriptId.trim();

				// if debug				
				System.out.println("[ScriptString.constructor] no retval, args, scriptId: " + scriptId);

				this.parseParameters();
		    }
		} else {
		    // any parameters?
	    	if (passedString.indexOf("(")<0) {
				// no parameters
				scriptId=passedString.substring(passedString.indexOf("="),passedString.length());
				scriptId=scriptId.trim();

				// if debug				
				System.out.println("[ScriptString.constructor] retval, no args, scriptId: " + scriptId);
	    	} else {
				// parameters
				scriptId=passedString.substring(passedString.indexOf("="),passedString.indexOf("("));
				scriptId=scriptId.trim();

				// if debug				
				System.out.println("[ScriptString.constructor] retval, args, scriptId: " + scriptId);

				this.parseParameters();
		    }
		}
		scriptString=passedString;
    }

    private void parseParameters() {
    	// line doesn't work yet
//		String parametersString=scriptString.substring(scriptString.indexOf("("),scriptString.indexOf(")"));
		// now split with parametersString.splite(",");
		// return a String array

    }

    /** returns the script id of the referenced script */
    public String getScriptId() {
	return scriptId;
    }
}
