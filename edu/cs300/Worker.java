package edu.cs300;
import CtCILibrary.*;
import java.util.concurrent.*;

class Worker extends Thread{

  Trie textTrieTree;
  ArrayBlockingQueue prefixRequestArray;
  ArrayBlockingQueue resultsOutputArray;
  int id;

  String passageName;
  String word;
  int present;

 
  public Worker(String[] words, int id, ArrayBlockingQueue prefix, ArrayBlockingQueue results){
	  this.textTrieTree=new Trie(words);
	  this.prefixRequestArray=prefix;
	  this.resultsOutputArray=results;
	  this.id=id;

  }


  public Worker(String[] words, String pname, int id,ArrayBlockingQueue prefix, ArrayBlockingQueue results){
	  this.textTrieTree=new Trie(words);
	  this.prefixRequestArray=prefix;
	  this.resultsOutputArray=results;
	  this.id=id;
	  this.passageName = pname;
	  this.present = -1;
  }

  public void run() {
	  System.out.println("Worker-"+this.id+" ("+this.passageName+") thread started ...");
	  String prefix = "prefix";
	  String lastPrefix = "lastPrefix";
	  String exitPrefix = "   ";

	  while (true) {
		  if (prefix == exitPrefix) break;
		  try {
			  prefix = (String)this.prefixRequestArray.take();
			  if (prefix != lastPrefix) {
				  boolean found = this.textTrieTree.contains(prefix);
				  if (!found){
					  present = 0;
					  this.word = "----";
					  System.out.println("Worker-"+this.id+" "+this.passageName+":"+ prefix+" ==> not found ");
					  resultsOutputArray.put(word);
					  resultsOutputArray.put(present);
				  } else{
					  present = 1;
					  this.word = textTrieTree.getLongestPrefixWord(prefix);
					  System.out.println("Worker-"+this.id+" "+this.passageName+":"+ prefix+" ==> "+this.word);
					  resultsOutputArray.put(word);
					  resultsOutputArray.put(present);
				  }
				  lastPrefix = prefix;
			  }
		  } catch(InterruptedException e){ System.out.println(e.getMessage()); }
	  }
  }
}
