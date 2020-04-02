package CtCILibrary;

import java.util.ArrayList;


/* Implements a trie. We store the input list of words in tries so
 * that we can efficiently find words with a given prefix. 
 */ 
public class Trie
{
    // The root of this trie.
    private TrieNode root;

    // variable for longest word  w/ given prefix
    private String lw;

    // Trie alphabet
    public static char[] alpha = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};

    /* Takes a list of strings as an argument, and constructs a trie that stores these strings. */
    public Trie(ArrayList<String> list) {
        root = new TrieNode();
        for (String word : list) {
            root.addWord(word);
        }
    }  
    

    /* Takes a list of strings as an argument, and constructs a trie that stores these strings. */    
    public Trie(String[] list) {
        root = new TrieNode();
        for (String word : list) {
            root.addWord(word);
	    //System.out.println("added -> "+word);
        }
    }    

    /* Checks whether this trie contains a string with the prefix passed
     * in as argument.
     */
    public boolean contains(String prefix, boolean exact) {
        TrieNode lastNode = root;
        int i = 0;
        for (i = 0; i < prefix.length(); i++) {
            lastNode = lastNode.getChild(prefix.charAt(i));
            if (lastNode == null) {
                return false;	 
            }
        }
        return !exact || lastNode.terminates();
    }
    
    public boolean contains(String prefix) {
    	return contains(prefix, false);
    }
    
    public TrieNode getRoot() {
    	return root;
    }
	
    /* Functions pertaining to longest word w/ given prefix */
    private void setLongestWord(String w) {
	    this.lw = w;
    }

    private String getLongestWord() {
	    return lw;
    }

    private String recBuildWord(TrieNode node, String c, int index) {
	    if (node.terminates()) return c;
	    // Find all possible children
	    boolean multipleNodesMatch = false;
	    ArrayList<Integer> nodes = new ArrayList<Integer>();
	    while (index < alpha.length) {
		    if (node.getChild(alpha[index]) != null) nodes.add(index);
		    index++;
	    }
	    // Find longest word from possible children
	    String ret = "";
	    for (Integer nodeIndex : nodes) {
		    TrieNode currNode = node.getChild(alpha[nodeIndex]);
		    String currChar = String.valueOf(alpha[nodeIndex]);
		    String tmp = c + recBuildWord(currNode, currChar, 0);
		    if (tmp.length() >= ret.length()) ret = tmp;
	    }
	    return ret;
    }

    public String getLongestPrefixWord(String prefix) {
	    // navigate to root of prefix
	    TrieNode prefixNode = root;
	    int i = 0;
	    for (i = 0; i < prefix.length(); i++) {
		    prefixNode = prefixNode.getChild(prefix.charAt(i));
	    }
	    setLongestWord(recBuildWord(prefixNode, prefix, 0));
	    return getLongestWord();
    }
}
