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
/**
 * A class for handling bws script strings (strings passed from html
 * that are used to invoke bws scripts).
 * 
 * @author Tobias Specht
 * @version 1.0
 */
public class ScriptString {
    // class internal representation of the string
    //private String scriptString;

    // the id of the script referenced by this script string
    private String scriptId;

	// the key that references the value returned by the script
    private String returnValue;

	// string array of parameters used in the function call
	private String[] parameters;

	// set a debug level for this class
	private static int debugLevel=1;

    /** interprets the passed string */
    public ScriptString(String passedString) {
		this.interpretScriptString(passedString);
    }

    /** 
     * Checks validity of the string and parses it.
     *
     * @param passedString the script string to be interpreted.
     */
	public void interpretScriptString(String passedString) {
		// it would be nice if the constructor checked for duplicate
		// =,( and ) and throw an exception if these occur
		// start indices probably should be +1

		//scriptString=passedString;

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

				parameters=this.parseParameters(passedString);
		    }
		} else {
		    // any parameters?
	    	if (passedString.indexOf("(")<0) {
				// no parameters
				scriptId=passedString.substring(passedString.indexOf("=")+1,passedString.length());
				scriptId=scriptId.trim();

				// if debug
				System.out.println("[ScriptString.constructor] retval, no args, scriptId: " + scriptId);
	    	} else {
				// parameters
				scriptId=passedString.substring(passedString.indexOf("=")+1,passedString.indexOf("("));
				scriptId=scriptId.trim();

				// if debug
				System.out.println("[ScriptString.constructor] retval, args, scriptId: " + scriptId);

				parameters=this.parseParameters(passedString);
		    }

		    returnValue=passedString.substring(0,passedString.indexOf("="));
		    System.out.println("[ScriptString.constructor] retval: " + returnValue);
		}
		//scriptString=passedString;


	}

	/**
	 * Parse the parameters part of the script string to the individual parameter keys.
	 *
	 * @param passedString the script string which parameters shall be parsed.
	 * @return the individual keys that were parsed from the script string.
	 */
    protected String[] parseParameters(String passedString) {
		if (debugLevel>0) {
			System.out.println("[ScriptString.parseParameters] scriptString: " + passedString);
		}

		String parametersString=passedString.substring(passedString.indexOf("(")+1,passedString.indexOf(")"));

		if (debugLevel>0) {
			System.out.println("[ScriptString.parseParameters] parametersString: " + parametersString);
		}

		// now split with parametersString.split(",");
		String[] parametersArray=parametersString.split(",");

		for (int parametersCounter=0;parametersCounter<parametersArray.length;parametersCounter++) {

			parametersArray[parametersCounter]=parametersArray[parametersCounter].trim();

			if (debugLevel>0) {
				System.out.println("[ScriptString.parseParameters] +- parameter " + parametersCounter + ": " + parametersArray[parametersCounter]);
			}
		}

		// return a String array
		return parametersArray;
	}

    /** 
     * Returns the script id of the referenced script.
     */
    public String getScriptId() {
		return scriptId;
    }

	/**
	 * Returns a String array of all parameters specified in the script string. 
	 */
    public String[] getParameters() {
    	return parameters;
    }

    /** 
     * Returns the return value key specified in the script string (the part in front of =) 
     */
    public String getRetKey() {
    	return returnValue;
    }
}
