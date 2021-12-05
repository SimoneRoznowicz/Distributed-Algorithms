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
	HashSet<String> set_missing = new HashSet<String>();
	ArrayList<ConcurrentHashMap<String,String>> maps_ack = new ArrayList<ConcurrentHashMap<String,String>>();
	List<HashSet<String>> sets_missing = Collections.synchronizedList(new ArrayList<HashSet<String>>());
	List<Integer> list_clock;
	List<List<Integer>> list;
	
	private Parser parser;
	private String outputPath;
	private int hostsNumber;
	private int valid;
	private boolean endd;
	long start;
	long end;
	private int tot_number_messages;
	int myId;
	
	ArrayList<Integer>[] listd;
	
	
	public MyLogger(Parser parser, List<List<Integer>> list) {
		System.out.println("TOT NUM MESSAGES == " + tot_number_messages);
		this.parser=parser;
		this.outputPath=parser.output();
		this.list = list;
		list_clock = list.get(1);
		hostsNumber = parser.hosts().size();
		myId=parser.myId();
		tot_number_messages = list_clock.get(0);
		System.out.println("TOT NUM MESSAGES == " + tot_number_messages);
		endd=true;
		
		for (int i=0; i<hostsNumber; i++) {
			HashSet<String> set = new HashSet<String>();
			ConcurrentHashMap<String,String> map = new ConcurrentHashMap<String,String>();
			maps_ack.add(map);
			sets_missing.add(set);
		}

		//initialize set_missing with all the elements to be sent
		for (int i=0; i<tot_number_messages; i++) {
			for (Host host : parser.hosts()) {
				if(host.getId() != myId) {
					int b=i+1;		//id di un host che deve mandare il msg, numero del messaggio che deve inviare --> es. 1 43
					String content=host.getId() + " " + myId + " " + b;
					sets_missing.get(parser.myId()-1).add(content);	
				}
			}
		}
	}
	
	public void update_list_clock(int id) {		//the receiver updates the value in a cell everytime a message arrives (id is the id of the sender)
		list_clock.set(id-1,list_clock.get(id-1)+1);
	}
	
	public List<List<Integer>> get_list_clock(){
		return list_clock;
	}
	
	synchronized public void add_set_missing(int IDOriginalSender, int myId, int message) {
		for (Host host : parser.hosts()) {
			if(host.getId() != myId) {
				String missing_content=host.getId() + " " + IDOriginalSender + " " + message;
				sets_missing.get(IDOriginalSender-1).add(missing_content);			//es. 2 43 The information IDOriginalSender is indexOf the set in the array + 1
			}
		}
	}
	
	public void add(String log) {
		logs.put(log,log);
		if(endd==true && logs.keySet().size()==hostsNumber*tot_number_messages) {
			System.out.println("\n*** RECEIVED ALL MESSAGES ***\n");
			endd=false;
		}
	}
	
	public void addAck(int IDOriginalSender, String logAck) {
		maps_ack.get(IDOriginalSender-1).put(logAck,logAck);			//1 2 int the receiver, 2 if the process is the sender
	}
	public int getSize() {
		return logs_ack_set.size();
	}
	
	public List<HashSet<String>> check() {
		for(int i=0; i<sets_missing.size();i++) {
			Iterator <String> iter = maps_ack.get(i).keySet().iterator();
		    while (iter.hasNext()) {
		    	sets_missing.get(i).remove(iter.next());
		    }
		}
		return sets_missing;
	}
	
	public void writeOutput() {
		System.out.println("*** NUMBER OF LOGS *** == " + logs.size());
		
		ArrayList<Integer> listb = new ArrayList<Integer>();
		listd = new ArrayList[hostsNumber];
		ArrayList<Integer> arrl = null;
		for(int i=0;i<listd.length;i++) {
			listd[i] = new ArrayList<Integer>();
		}	

		try(BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputPath))) {
			Iterator <String> iter = logs.keySet().iterator();
			while(iter.hasNext()) {
				String a = iter.next();
				if(a.charAt(0)=='b') {
					int bNum = Integer.valueOf(a.substring(2,a.indexOf('\n')));
					listb.add(bNum);
				}
				else if(a.charAt(0)=='d') {
					Scanner scan = new Scanner(a.substring(2));
					String c= a.substring(2);
					int dFirstNum = Integer.valueOf(c.substring(0,c.indexOf(' ')));
					int dSecondNum = Integer.valueOf(c.substring(c.indexOf(" ")+1,c.indexOf('\n')));
					if(listd!=null) {
						listd[dFirstNum-1].add(dSecondNum);
					}
				}
				else{
					fileWriter.write(a);
				}
			}
			Collections.sort(listb);
			for(int num : listb) {
				fileWriter.write("b " + num + "\n");
			}

			for(int i=0;i<listd.length;i++) {
				Collections.sort(listd[i]);
				for(Integer element : listd[i]) {
					int x=i+1;
					fileWriter.write("d " + x + " " + element + "\n");
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}