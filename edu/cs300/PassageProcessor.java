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
	
	public static void main(String[] args) throws Exception {
                /* Read a series of passage file names from passages.txt */
		//System.out.println("PASSAGE PROCESSOR: Started");
                ArrayList<String> passageList = readFileList(FILE_PATH);
		int passageCount = passageList.size();

		String[][] passages = new String[passageCount][];
		for (int i = 0; i < passageCount; i++) {
			String passageName = passageList.get(i);
			ArrayList<String> passage = readFile(FILE_PATH2+passageName);
			passages[i] = new String[passage.size()];
			passages[i] = passage.toArray(passages[i]);
		}

		ArrayBlockingQueue[] workers = new ArrayBlockingQueue[passageCount];
		ArrayBlockingQueue[] results = new ArrayBlockingQueue[passageCount];
		ExecutorService executor = Executors.newCachedThreadPool();
		for (int p = 0; p < passageCount; p++) {
			workers[p] = new ArrayBlockingQueue(100);
			results[p] = new ArrayBlockingQueue(100);
			executor.execute(new Worker(passages[p], passageList.get(p), p, workers[p], results[p]));
		}

		int r = 0;
		SearchRequest req = new SearchRequest();
		while (req.getRequestID() != 0) {
			/* Read each prefix request from the System V ipc queue */
			req = new SearchRequest();
			try {
			while (req.getRequestID() == -1) {
				req = new MessageJNI().readPrefixRequestMsg();
				Thread.sleep(100);
			}
			if (req.getRequestID() == 0) break;
			} catch(Exception e) {System.out.println(e);}

			int p = 0;
			while (p < passageCount) {
				try {
				workers[p].put(req.getRequestPrefix());	
				if (r > 0) executor.execute(new Worker(passages[p], passageList.get(p), p, workers[p], results[p]));
				executor.execute(new PassageProcessorResponse(req.getRequestID(), req.getRequestPrefix(), p+1, passageList.get(p), passageCount, results[p]));
				} catch (InterruptedException e) {e.printStackTrace();}
				p++;
			}
			r++;
		}
		executor.shutdown();
		System.out.println("Terminating ...");
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
}
