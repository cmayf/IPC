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
	  try {
		  while (prefixRequestArray.isEmpty()) Thread.sleep(100);
		  String prefix = (String)this.prefixRequestArray.take();
		  boolean found = this.textTrieTree.contains(prefix);

		  if (!found){
			  present = 0;
			  this.word = "----";
			  System.out.println("Worker-"+this.id+" "+this.passageName+":"+ prefix+" ==> not found ");
			  resultsOutputArray.put(word);
			  resultsOutputArray.put(present);
		  } else{
			  present = 1;
			  findWord(prefix);
			  System.out.println("Worker-"+this.id+" "+this.passageName+":"+ prefix+" ==> "+this.word);
			  resultsOutputArray.put(word);
			  resultsOutputArray.put(present);
		  }
	  } catch(InterruptedException e){
		  System.out.println(e.getMessage());
	  }
  }

  public void findWord(String prefix) throws InterruptedException {
	  this.word = textTrieTree.getLongestPrefixWord(prefix);
  }
}
