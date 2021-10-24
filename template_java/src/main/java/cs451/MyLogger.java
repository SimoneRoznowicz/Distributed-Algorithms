package cs451;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class MyLogger {
	Set<String> logs = new HashSet<String>();
	Set<String> logs_ack_set = new HashSet<String>(); 
	private int tot_number_messages;
	private Parser parser;
	private String outputPath;
	private int hostsNumber;
	
	public MyLogger(Parser parser, int tot_number_messages) {
		this.parser=parser;
		this.outputPath=parser.output();
		this.tot_number_messages=tot_number_messages;
		hostsNumber = parser.hosts().size();
	}
	
	public void add(String log) {
		logs.add(log);
	}
	
	public void addAck(String logAck) {
		logs_ack_set.add(logAck);			//1 2 int the receiver, 2 if the process is the sender
	}
	
	public ArrayList<String> check() {
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> list_missing = new ArrayList<String>();
		for(String s : logs_ack_set) {
			list.add(s);
		}
		System.out.println("list == " + list);
		System.out.println("list_missing.size() == " + list_missing.size());
		System.out.println("logs_ack_set == " + logs_ack_set);

		for(int i=0; i<tot_number_messages; i++) {
			//System.out.println("elemento ack numero " + i + " e' " + list.get(i));
			int b=i+1;
			if(!list.contains(b+"")) {
				//add to messages missing
				list_missing.add(b+"");
			}
		}
		return list_missing;
		/*ArrayList<int>[] arrayOfList = new ArrayList[hostNumber];
		//String[][] arrays = new String[hostNumber][];
		//for (int i=0; i<logs_ack_set.size(); i++) {
		Iterator iter = logs_ack_set.iterator();
		while (iter.hasNext()) {
			Scanner s = new Scanner(iter.next());
			int processId = s.nextInt();
			int messageNumber = s.nextInt();
			for (int j=0; j<hostsNumber; j++) {
				if (processId == (j+1)) {
					arrayOfList[j].add(messageNumber);   	//lista j corrisponde a ID j+1
				}
			}
		}
		
		for (Host host: parser.hosts()) {
			for (int i=0; i<logs_ack_set.size(); i++) {
				Scanner s = new Scanner(s);
				int processId = s.nextInt();
				int messageNumber = s.nextInt();
				if(!contains(i+1)) {
					
				}
			}
		}*/
	}
	
	public void writeOutput() {
		System.out.print("*** NUMBER OF LOGS *** == " + this.logs.size());
		try(BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputPath))) {
			for(String log : logs) {
				fileWriter.write(log);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		/*String secondPath = "../example/output/fileout.txt";
		try(BufferedWriter fileWriter = new BufferedWriter(new FileWriter(secondPath))) {
			for(String log : logs) {
				fileWriter.write(log);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}*/
	}
}
