package cs451;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;


public class MyLogger {
	ConcurrentHashMap<String,String> logs = new ConcurrentHashMap<String,String>();
	ConcurrentHashMap<String,String> logs_ack_set = new ConcurrentHashMap<String,String>(); 
	HashSet<String> set_missing = new HashSet<String>();
	ArrayList<ConcurrentHashMap<String,String>> maps_ack = new ArrayList<ConcurrentHashMap<String,String>>();
	List<ConcurrentHashMap<String,String>> sets_missing = Collections.synchronizedList(new ArrayList<ConcurrentHashMap<String,String>>());
	List<Integer> list_clock;
	List<List<Integer>> list;
	ConcurrentLinkedQueue<Integer> queue= new ConcurrentLinkedQueue<Integer>();
	
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
			sets_missing.add(map);
		}

		//initialize set_missing with all the elements to be sent
		for (int i=0; i<tot_number_messages; i++) {
			for (Host host : parser.hosts()) {
				if(host.getId() != myId) {
					int b=i+1;		//id di un host che deve mandare il msg, numero del messaggio che deve inviare --> es. 1 43
					String content=host.getId() + " " + myId + " " + b;
					sets_missing.get(parser.myId()-1).put(content,"");	
				}
			}
		}
	}
	
	public void update_list_clock(int id) {		//the receiver updates the value in a cell everytime a message arrives (id is the id of the sender)
		list_clock.set(id-1,list_clock.get(id-1)+1);
	}
	synchronized void can_log() {
		List<Integer> list_clock_pending = new ArrayList<Integer>();
		bool canLog=true;
		for(int i=0;i<list_clock.size();i++) {
			//qui total causal broadcast. Se vuoi localized, guarda di confrontare solo l'entri che ti interessa
			
			//ottieni id di chi ti manda il messaggio
			//guarda da chi dipende quel process
			if(list_clock.get(i)<=list_clock_pending.get(i)) {
				canLog=false;
				break;
			}
		}
		return canLog;
	}
	
	public String get_list_clock(){
		//puoi semplificare con unsemplice for che stampa tutti gli elementi in una stringa separati da uno spazio0
		String a = list_clock.toString();
		a = a.substring(1,a.length()-1);
		StringBuilder str = new StringBuilder(a);
		for(int i=0;i<str.length();i++) {
			if(str.charAt(i)==',') {
				str.setCharAt(i, ' ');
			}
		}
		String string =str.toString();
		return string;    //example: string=="1 2 3"
	}
	
	synchronized public void add_set_missing(int IDOriginalSender, int myId, int message, String string_clock) {
		for (Host host : parser.hosts()) {
			if(host.getId() != myId) {
				String missing_content=host.getId() + " " + IDOriginalSender + " " + message;
				if(!sets_missing.get(IDOriginalSender-1).containsKey(missing_content)) {
					sets_missing.get(IDOriginalSender-1).put(missing_content,string_clock);			//es. 2 43 The information IDOriginalSender is indexOf the set in the array + 1
				}
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
	
	public List<ConcurrentHashMap<String,String>> check() {
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