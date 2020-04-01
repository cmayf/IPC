/* Passage processor requirements
 * Written in Java with the main function in edu.cs300.PassageProcessor.java * Read passage file names from passages.txt in root directory (hardcode the name)
 * Read contents of each passages file in the root directory */

package edu.cs300;

import CtCILibrary.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.*;

public class PassageProcessor
{
		
	static { System.loadLibrary("system5msg"); }

	private static final String FILE_PATH = "/home/caleb/OS-Project/passages.txt";
	private static final String FILE_PATH2 = "/home/caleb/OS-Project/";
	
	//private static final String FILE_PATH = "/passagest.txt";
	public static void main(String[] args) throws Exception {
		//System.out.println("PASSAGE PROCESSOR: Started");
                /* Read a series of passage file names from passages.txt */
                ArrayList<String> passageList = readFileList(FILE_PATH);
		int passageCount = passageList.size();

		String[][] passages = new String[passageCount][];
		for (int i = 0; i < passageCount; i++) {
			String passageName = passageList.get(i);
			ArrayList<String> passage = readFile(FILE_PATH2+passageName);
			passages[i] = new String[passage.size()];
			passages[i] = passage.toArray(passages[i]);
		}

		int requestCount = 4;
		int resultCount = passageCount * requestCount;

		//System.out.println("PASSAGE PROCESSOR: Read prefix requests");
		SearchRequest[] requests = new SearchRequest[requestCount];
		for (int i = 0; i < requestCount; i++) {
			requests[i] = new SearchRequest();
		}
		int[] ids = new int[requestCount];
		String[] prefixes = new String[requestCount];
		Worker[] threads = new Worker[passageCount];
		ArrayBlockingQueue[] workers = new ArrayBlockingQueue[passageCount];
		ArrayBlockingQueue[] results = new ArrayBlockingQueue[resultCount];
		
		int r = 0;
		while (r < requestCount && requests[r].getRequestID() != 0) {
			/* Read each prefix request from the System V ipc queue */
			synchronized (PassageProcessor.class) {
			try {
			//System.out.println("PASSAGE PROCESSOR: request "+r+" started"); 
			//System.out.println("PASSAGE PROCESSOR: read prefix request msg attempt");
			while (requests[r].getRequestID() == -1) {
				requests[r] = new MessageJNI().readPrefixRequestMsg();
				Thread.sleep(1000);
			}
			if (requests[r].getRequestID() == 0) break;
			//System.out.println("__________________________________________________");
			ids[r] = requests[r].getRequestID();
			prefixes[r] = requests[r].getRequestPrefix();
			System.out.println(requests[r].toString());
			} catch(Exception e) {System.out.println(e);}

			/* Send requests to each worker */
			//System.out.println("PASSAGE PROCESSOR: Send requests to each worker");
			for (int p = 0; p < passageCount; p++) {
				workers[p] = new ArrayBlockingQueue(100);
				results[p+r] = new ArrayBlockingQueue(100);
				threads[p] = new Worker(passages[p], passageList.get(p), p, workers[p], results[p+r]);
				threads[p].start();
				try {
					workers[p].put(prefixes[r]);
					workers[p].put(ids[r]);
				} catch (InterruptedException e) {};
			}

			/* Retrieve responses from each worker */
			/* Send them back to the search manager via the system V queue */
			//System.out.println("PASSAGE PROCESSOR: Retrieve responses from each worker");
			for (int p = 0; p < passageCount; p++) {
				//int prefixID = ids[r];
				//String prefix = prefixes[r];
				//String passageName = passageList.get(p);
				//String longestWord = results[p+r].take().toString();
				//int present = (int) results[p+r].take();
				//sendResponseMsg(prefixID, prefix, p+1, passageName, longestWord, passageCount, present);
				sendResponseMsg(ids[r], prefixes[r], p+1, passageList.get(p), results[p+r].take().toString(), passageCount, (int) results[p+r].take());
				//System.out.println("PASSAGE PROCESSOR: Message sent");
			}
			r++;
			}
		}
		for (int p = 0; p < passageCount; p++) {
			try {
				threads[p].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//System.out.println("PASSAGE PROCESSOR: Exited");

	}

	private static void sendResponseMsg(int prefixID, String prefix, int passageIndex, String passageName, String longestWord, int pcount, int present) {
		if (present == 1) {
			new MessageJNI().writeLongestWordResponseMsg(prefixID, prefix, passageIndex, passageName, longestWord, pcount, present);
		}
		else new MessageJNI().writeLongestWordResponseMsg(prefixID, prefix, passageIndex, passageName, "----", pcount, present);
	}

	private static ArrayList<String> readFileList(String fileName) throws Exception {
		ArrayList<String> arrayList = new ArrayList<String>();
		File file = new File(fileName);
		Scanner scanner = new Scanner(file);	
		try {
			while (scanner.hasNextLine()) { 
				arrayList.add(scanner.nextLine());
			}
		} catch (Exception e) { 
			System.out.println("readFile error occured.");
			e.printStackTrace(); 
		}
		scanner.close();
		return arrayList;
	}

	private static ArrayList<String> readFile(String fileName) throws Exception {
		ArrayList<String> arrayList = new ArrayList<String>();
		File file = new File(fileName);
		Scanner scanner = new Scanner(file);
		String delim = "(\\W|\\s|\\d)";
		scanner.useDelimiter(delim);
		try {
			while (scanner.hasNext()) {
				String word = scanner.next();
				if (word.matches(".*\\w.*"))
					arrayList.add(word);
			}
		} catch (Exception e) {
			System.out.println("readFile error occured.");
			e.printStackTrace();
		}
		scanner.close();
		return arrayList;
	}


	//Declare systemV C methods
	//private static native String readStringMsg();
	//public static native SearchRequest readPrefixRequestMsg();
	//public static native void writeLongestWordResponseMsg();
}
