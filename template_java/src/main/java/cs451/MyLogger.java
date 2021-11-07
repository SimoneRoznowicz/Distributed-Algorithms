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
	ConcurrentHashMap<String,String> set_missing = new ConcurrentHashMap<String,String>();

	private int tot_number_messages;
	private Parser parser;
	private String outputPath;
	private int hostsNumber;
	private int valid;
	
	public MyLogger(Parser parser, int tot_number_messages) {
		this.parser=parser;
		this.outputPath=parser.output();
		this.tot_number_messages=tot_number_messages;
		hostsNumber = parser.hosts().size();
		valid=0;
		//initialize set_missing with all the elements to be sent
		for(int i=0; i<tot_number_messages; i++) {
			int b=i+1;
			set_missing.put(b+"",b+"");
		}
	}
	
	public String add(String log) {
		return logs.put(log,log);
	}
	
	public void addAck(String logAck) {
		//if(!logs_ack_set.contains(logAck)) {
			//new_logs_ack_set.put(logAck,logAck);
		//}
		logs_ack_set.put(logAck,logAck);			//1 2 int the receiver, 2 if the process is the sender
	}
	//public int getSize() {
	//	return new_logs_ack_set.size();
	//}
	
	public ConcurrentHashMap<String,String> check() {
		//for(String[] new_log : new_logs_ack_set) {
			//set_missing.remove(new_log);
		//}
		if(valid==7) {
			System.out.println("set_miss.size() == " + set_missing.size());
			//System.out.println("new_logs_ack_set.size() == " + new_logs_ack_set.size());
			System.out.println("logs_ack_set.size() == " + logs_ack_set.size()+"\n");
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
		System.out.println("*** ROUGH NUMBER OF LOGS *** == " + logs.size());
		try(BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputPath))) {
			Iterator <String> iter = logs.keySet().iterator();
			while(iter.hasNext()) {
				fileWriter.write(iter.next());
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}