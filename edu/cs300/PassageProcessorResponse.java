package edu.cs300;
import java.util.concurrent.*;

class PassageProcessorResponse extends Thread {
	
	static { System.loadLibrary("system5msg"); }
	
	ArrayBlockingQueue result;
	int prefixID;
	String prefix;
	int passageIndex;
	String passageName;
	String longestWord;
	int passageCount;
	int wordPresent;

	public PassageProcessorResponse(int prefixID, String prefix, int passageIndex, String passageName, int passageCount, ArrayBlockingQueue result) {
		this.prefixID = prefixID;
		this.prefix = prefix;
		this.passageCount = passageCount;
		this.passageIndex = passageIndex;
		this.passageName = passageName;
		this.result = result;
	}
	
	public void run() {
		try {
			while (result.isEmpty()) Thread.sleep(100);
			this.longestWord = result.take().toString();
			this.wordPresent = (int) result.take();
			sendMessage();
		}
		catch(InterruptedException e) { System.out.println(e.getMessage()); }
	}

	private void sendMessage() throws InterruptedException {
		new MessageJNI().writeLongestWordResponseMsg(prefixID, prefix, passageIndex, passageName, longestWord, passageCount, wordPresent);
	}
}

	
