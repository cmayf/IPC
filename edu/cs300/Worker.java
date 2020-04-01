package edu.cs300;
import CtCILibrary.*;
import java.util.concurrent.*;

class Worker extends Thread{

  Trie textTrieTree;
  ArrayBlockingQueue prefixRequestArray;
  ArrayBlockingQueue resultsOutputArray;
  int id;

  String passageName;
  //int passageCount;
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
	  //this.passageCount = pcount;
	  this.present = -1;
  }

  public void run() {
	  System.out.println("Worker-"+this.id+" ("+this.passageName+") thread started ...");
	  //while (true){
	  try {
		  String prefix=(String)this.prefixRequestArray.take();
		  int prefixID = (int)this.prefixRequestArray.take();
		  boolean found = this.textTrieTree.contains(prefix);

		  if (!found){
			  present = 0;
			  this.word = "----";
			  System.out.println("Worker-"+this.id+" "+this.passageName+":"+ prefix+" ==> not found ");
			  //resultsOutputArray.put(passageName+":"+prefix+" not found");
			  resultsOutputArray.put(word);
			  resultsOutputArray.put(present);
		  } else{
			  present = 1;
			  this.word = textTrieTree.getLongestPrefixWord(prefix);
			  System.out.println("Worker-"+this.id+" "+this.passageName+":"+ prefix+" ==> "+this.word);
			  //resultsOutputArray.put(passageName+":"+prefix+" found ");
			  resultsOutputArray.put(word);
			  resultsOutputArray.put(present);
		  }
		  //new PassageProcessor().sendResponseMsg(prefixID, prefix, this.id, passageName, this.word, this.passageCount, this.present);
	  } catch(InterruptedException e){
		  System.out.println(e.getMessage());
	  }
  }

/* 
  public boolean wordPresent() throws InterruptedException {
	  String prefix = (String)this.prefixRequestArray.take();
	  boolean found = this.textTrieTree.contains(prefix);
	  return found;
  }
/*
  public String getWord() throws InterruptedException {
	  String prefix = (String)this.prefixRequestArray.take();
	  return textTrieTree.getLongestPrefixWord(prefix);
  }
*/
}
