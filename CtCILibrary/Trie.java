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

    /* Preorder traversal of trie */
    public void preorder(TrieNode node, int i) {
	    if (node == null) return;
	    System.out.print(node.getChar()+" ");
	    preorder(node.getChild(alpha[i]), i+1);
    }


    /* Functions pertaining to longest word w/ given prefix */
    private void setLongestWord(String w) {
	    this.lw = w;
    }

    private String getLongestWord() {
	    return lw;
    }
    
    private String buildWord(TrieNode node, String prefix) {
	    return recBuildWord(node, prefix);
    }

    private String recBuildWord(TrieNode node, String c) {
	    if (node.terminates()) return c;
	    // find next letter
	    int i;
	    for (i = 0; i < alpha.length; i++) {
	    	if (node.getChild(alpha[i]) != null) break;
	    }
	    TrieNode nextNode = node.getChild(alpha[i]);
	    String nextChar = String.valueOf(nextNode.getChar());
	    return c + recBuildWord(nextNode, nextChar);
    }

    public String getLongestPrefixWord(String prefix) {
	    setLongestWord(prefix);
	    TrieNode lastNode = root;
	    int i = 0;
	    for (i = 0; i < prefix.length(); i++) {
		    lastNode = lastNode.getChild(prefix.charAt(i));
	    }
	    
	    TrieNode currNode = lastNode;
	    for (i = 0; i < alpha.length; i++) {
		    String lwcand = buildWord(currNode, prefix);
		    if (lwcand.length() > getLongestWord().length()) {
			    setLongestWord(lwcand);
		    }
	    }

	    return getLongestWord();
    }
}
