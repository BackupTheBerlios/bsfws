package org.tsp.bws;

public class bwsRewriter {
	/** reads a document from an URL and rewrites it as an bws document */

	/** main method
	 * @param args String array of command line arguments
	 */
	public static void main(String args[]) {
		bwsDocument docToRewrite=new bwsDocument();
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