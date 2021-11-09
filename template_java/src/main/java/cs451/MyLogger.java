package cs451;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.Iterator;


public class MyLogger {
	ConcurrentHashMap<String,String> logs = new ConcurrentHashMap<String,String>();
	ConcurrentHashMap<String,String> logs_ack_set = new ConcurrentHashMap<String,String>(); 
	//ConcurrentHashMap<String,String> new_logs_ack_set = new ConcurrentHashMap<String,String>();
	HashSet<String> set_missing = new HashSet<String>();

	private int tot_number_messages;
	private Parser parser;
	private String outputPath;
	private int hostsNumber;
	private int valid;
	private boolean end;
	
	public MyLogger(Parser parser, int tot_number_messages) {
		System.out.println("TOT NUM MESSAGES == " + tot_number_messages);
		this.parser=parser;
		this.outputPath=parser.output();
		this.tot_number_messages=tot_number_messages;
		hostsNumber = parser.hosts().size();
		valid=0;
		end=true;
		//initialize set_missing with all the elements to be sent
		for (int i=0; i<tot_number_messages; i++) {
			for (Host host : parser.hosts()) {
				if(host.getId() != parser.myId()) {
					int b=i+1;		//id di un host che deve mandare il msg, numero del messaggio che deve inviare --> es. 1 43
					set_missing.add(host.getId() + " " + b);	
				}
			}
		}
	}
	
	public void add(String log) {
		logs.put(log,log);
		//System.out.println("logs.keyset() ========= \n" + logs.keySet());
		if(end==true && logs.keySet().size()==hostsNumber*tot_number_messages) {
			//System.out.println("logs.keySet().size() == " + logs.keySet().size());.
			System.out.println("\n*** RECEIVED ALL MESSAGES ***\n");
			end=false;
		}
	}
	
	public void addAck(String logAck) {
		//if(!logs_ack_set.contains(logAck)) {
			//new_logs_ack_set.put(logAck,logAck);
		//}
		logs_ack_set.put(logAck,logAck);			//1 2 int the receiver, 2 if the process is the sender
	}
	public int getSize() {
		return logs_ack_set.size();
	}
	
	public HashSet<String> check() {
		if(valid==7) {
			valid=0;
		}
		else {valid++;}
		Iterator <String> iter = logs_ack_set.keySet().iterator();
	    while (iter.hasNext()) {
	    	set_missing.remove(iter.next());
	    }
	    //new_logs_ack_set.clear();
		return set_missing;
	}
	
	public void writeOutput() {
		System.out.println("*** NUMBER OF LOGS *** == " + logs.size());
		try(BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputPath))) {
			Iterator <String> iter = logs.keySet().iterator();
			while(iter.hasNext()) {
				fileWriter.write(iter.next());
			}
			//System.out.println("*** set_missing == " + set_missing + " ***");
			//System.out.println("*** logs_ack_set == " + logs_ack_set.keySet() + " ***");	
		}

		catch (IOException e) {
			e.printStackTrace();
		}
	}
}