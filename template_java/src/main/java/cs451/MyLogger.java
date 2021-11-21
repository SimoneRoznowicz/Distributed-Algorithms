package cs451;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;


public class MyLogger {
	ConcurrentHashMap<String,String> logs = new ConcurrentHashMap<String,String>();
	ConcurrentHashMap<String,String> logs_ack_set = new ConcurrentHashMap<String,String>(); 
	//ConcurrentHashMap<String,String> new_logs_ack_set = new ConcurrentHashMap<String,String>();
	HashSet<String> set_missing = new HashSet<String>();
	ArrayList<ConcurrentHashMap<String,String>> maps_ack = new ArrayList<ConcurrentHashMap<String,String>>();
	List<HashSet<String>> sets_missing = Collections.synchronizedList(new ArrayList<HashSet<String>>());
	private int tot_number_messages;
	private Parser parser;
	private String outputPath;
	private int hostsNumber;
	private int valid;
	private boolean endd;
	long start;
	long end;
	
	
	public MyLogger(Parser parser, int tot_number_messages) {
        System.out.println("start time " + start);
		
		if(sets_missing == null) {
			System.out.println("SET_MISSING E' NULL");
		}
		System.out.println("TOT NUM MESSAGES == " + tot_number_messages);
		this.parser=parser;
		this.outputPath=parser.output();
		this.tot_number_messages=tot_number_messages;
		hostsNumber = parser.hosts().size();
		//sets_missing = new ArrayList<HashSet<String>>();
		//maps_ack = new ArrayList<ConcurrentHashMap<String,String>>();
		endd=true;
		for (int i=0; i<hostsNumber; i++) {
			HashSet<String> set = new HashSet<String>();
			ConcurrentHashMap<String,String> map = new ConcurrentHashMap<String,String>();
			maps_ack.add(map);
			sets_missing.add(set);
		}
		System.out.println("dimensione2 " + sets_missing.size());
		int myId=parser.myId();
		//initialize set_missing with all the elements to be sent
		for (int i=0; i<tot_number_messages; i++) {
			for (Host host : parser.hosts()) {
				if(host.getId() != myId) {
					int b=i+1;		//id di un host che deve mandare il msg, numero del messaggio che deve inviare --> es. 1 43
					String content=host.getId() + " " + host.getId() + " " + b;
					//System.out.println("Content!!!!===== " + content);
					sets_missing.get(parser.myId()-1).add(content);	
				}
			}
		}
	}
	
	public void add_set_missing(int IDOriginalSender, int myId, int message) {
		//array of set_missing
		//arr[OriginalHostId-1] contains set_missing always used: I send my messages to all the other processes.
		//arr[i-1] contains at the beginning an empty set: every time Ireceive a message from process i, I put the messages here 
		//(as I need to broadcast every message I receive). At the end, if all processes are correct, I will have an array of i 
		//elements, each of them containing the same amount of messages
		String missing_content=myId + " " + IDOriginalSender + " " + message;
		//System.out.println("missing_content" + missing_content);
		sets_missing.get(IDOriginalSender-1).add(missing_content);			//es. 2 43 The information IDOriginalSender is indexOf the set in the array + 1
	}
	
	public void add(String log) {
		logs.put(log,log);
		//System.out.println("logs.keyset() ========= \n" + logs.keySet());
		if(endd==true && logs.keySet().size()==hostsNumber*tot_number_messages) {
			//System.out.println("logs.keySet().size() == " + logs.keySet().size());.
	        
			System.out.println("\n*** RECEIVED ALL MESSAGES ***\n");
			//end = System.currentTimeMillis();
			//System.out.println("end time " + end);
			//float seconds=(end-start)/1000.0f;
	        
	        //System.out.println("DELTA TIME: " + (System.currentTimeMillis()-start)/1000.0f);
			endd=false;
		}
	}
	
	public void addAck(int IDOriginalSender, String logAck) {
		//if(!logs_ack_set.contains(logAck)) {
			//new_logs_ack_set.put(logAck,logAck);
		//}
		maps_ack.get(IDOriginalSender-1).put(logAck,logAck);			//1 2 int the receiver, 2 if the process is the sender
	}
	public int getSize() {
		return logs_ack_set.size();
	}
	
	public List<HashSet<String>> check() {
		/*if(valid==7) {
			valid=0;
		}
		else {valid++;}*/
		//System.out.println("dimensione1 " + sets_missing.get(0).size());
		//System.out.println("dimensione2 " + sets_missing.get(1).size());
		//System.out.println("dimensione3 " + sets_missing.get(2).size());
		//System.out.println("dimensione4 " + sets_missing.get(3).size());
		for(int i=0; i<sets_missing.size();i++) {
			Iterator <String> iter = maps_ack.get(i).keySet().iterator();
		    while (iter.hasNext()) {
		    	sets_missing.get(i).remove(iter.next());
		    }
		}
	    //new_logs_ack_set.clear();
		return sets_missing;
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