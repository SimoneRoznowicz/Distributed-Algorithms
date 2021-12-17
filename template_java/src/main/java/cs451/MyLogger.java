package cs451;

/*4 localhost 11004
5 localhost 11005
6 localhost 11006
7 localhost 11007
8 localhost 11008
9 localhost 11009
*/
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.Scanner;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;
import java.util.HashMap;


public class MyLogger {
	ConcurrentHashMap<String,String> logs = new ConcurrentHashMap<String,String>();
	ConcurrentHashMap<String,String> logs_ack_set = new ConcurrentHashMap<String,String>(); 
	HashSet<String> set_missing = new HashSet<String>();
	ArrayList<ConcurrentHashMap<String,String>> maps_ack = new ArrayList<ConcurrentHashMap<String,String>>();
	List<ConcurrentHashMap<String,String>> sets_missing = Collections.synchronizedList(new ArrayList<ConcurrentHashMap<String,String>>());
	List<Integer> my_list_clock = Collections.synchronizedList(new ArrayList<Integer>());
	List<List<Integer>> list;
	ConcurrentHashMap<String,String> map_store_log = new ConcurrentHashMap<String,String>(); 
	ConcurrentLinkedQueue<String> log_queue = new ConcurrentLinkedQueue<String>();
	
	private Parser parser;
	private String outputPath;
	private int hostsNumber;
	private int valid;
	private boolean endd;
	long start;
	long end;
	private int tot_number_messages;
	int myId;
	int broadcast;
	ArrayList<Integer>[] listd;
	
	
	public MyLogger(Parser parser, List<List<Integer>> list) {
		broadcast=0;
		this.parser=parser;
		this.outputPath=parser.output();
		this.list = list;
		hostsNumber = parser.hosts().size();
		myId=parser.myId();
		tot_number_messages = list.get(0).get(0);
		list.get(myId);
		for(int i=1;i<=hostsNumber;i++) {
			if(list.get(myId).contains(i)) {
				my_list_clock.add(0);
			}
			else {
				my_list_clock.add(0);		//era -1
			}
		}
		System.out.println("my_list_clock========================= " + my_list_clock);
		
		/*
		 * guarda, se sono host 1 allora guardo list.get(1)
		 * Poi, 
		 * 
		 * */
		
		System.out.println("TOT NUM MESSAGES **********************== " + tot_number_messages);
		endd=true;
		
		for (int i=0; i<hostsNumber; i++) {
			HashSet<String> set = new HashSet<String>();
			ConcurrentHashMap<String,String> map1 = new ConcurrentHashMap<String,String>();
			ConcurrentHashMap<String,String> map2 = new ConcurrentHashMap<String,String>();
			maps_ack.add(map1);
			sets_missing.add(map2);
		}

		//initialize set_missing with all the elements to be sent
		for (int i=0; i<tot_number_messages; i++) {
			for (Host host : parser.hosts()) {
				if(host.getId() != myId) {
					int b=i+1;		//id di un host che deve mandare il msg, numero del messaggio che deve inviare --> es. 1 43
					String content=host.getId() + " " + myId + " " + b;  //il tipo a cui mando il messaggio, il mio nome cosi' si sa che ho mandato io e il numero del messaggio
					sets_missing.get(parser.myId()-1).put(content,"");	
				}
			}
		}
		//System.out.println("THIS IS THE INITIAL SETS_MISSING: \n" + sets_missing + "\n\n");
	}
	
	
	
	public void logger_sender(List<String> list_payloads, InetAddress ip, int port, int myId, int receiverId, String outputPath) {
		int number_threads_send=1;
		ThreadPoolExecutor executor_send = (ThreadPoolExecutor) Executors.newFixedThreadPool(number_threads_send);
		//now check the list of messages which seem to be not arrived (until there are no messages left to be sent, keep sending the missing ones)
		//sets_missing = check();

		int myID = parser.myId();
		while(true) {
			check();
			synchronized (sets_missing) {
				for(int j=0;j<sets_missing.size();j++) {
			    	//System.out.println("CI SONO 3");
			    	ConcurrentHashMap<String,String> considered_map = (ConcurrentHashMap<String,String>)sets_missing.get(j);
			    	int y=0;
					int num_mess=1;
					int count_done_sent=0;
					String content="";
					boolean isFirst=true;
					port=-1;
					int size_this_set = considered_map.keySet().size();
					int num_really_sent_thisset=0;
					boolean redo=true;
					//while(redo==true) {
						//isFirst=true;
						for(String missing_msg : considered_map.keySet()) { //ho forzato a hashMap anche se sarebbe concurrent hash map						
							
							boolean isRebroadcast=false;
							int index=0;
							for(int i=0;i<missing_msg.length();i++) {
			    	    		if(missing_msg.charAt(i)=='|') {
			    	    			index=i;
			    	    			isRebroadcast=true;
			    	    			isFirst=true;
			    	    			break;
			    	    		}
			    	    	}
							if(isFirst==true) {
								int possibleport=11000+Integer.valueOf(missing_msg.substring(0,missing_msg.indexOf(" ")));
								//System.out.println("Porta precedente == " + port + "  Possibile porta ora == " + possibleport);
	
								if(possibleport==port) {
									//System.out.println("Si uguale quindi esco");
									count_done_sent++;
									continue;
								}
								
								port=possibleport;
								isFirst=false;
							}
							/*if(port!=11000+Integer.valueOf(missing_msg.substring(0,missing_msg.indexOf(" ")))) {
								count_done_sent++;
								System.out.println("Si uguale quindi esco");
								continue;
							}*/
							String string_clock=null;
							if(!isRebroadcast) {
								string_clock = get_string_my_clock();
								//System.out.println("---------------string-clock" + string_clock);
							}
							else 
								string_clock = considered_map.get(missing_msg);
							//piu' messaggi al colpo
							//$ separates one message from the other (ONE SPECE BEFORE THE END!)
							if(content=="")
								content = string_clock + "|" + myID + " " + missing_msg.substring(missing_msg.indexOf(" ")+1);
							else
								content = content + " $" + string_clock + "|" + myID + " " + missing_msg.substring(missing_msg.indexOf(" ")+1); //example  1 2 3|3 message $1 2 3|3 message     
							if(content.charAt(0)=='$') {
								//System.out.println("CONTENT     ===========      " + content);
							}
							y++;
							count_done_sent++;
							num_really_sent_thisset++;
							//System.out.println("num_really_sent_thisset " + num_really_sent_thisset + ", invece count_done_sent " + count_done_sent + ", invece size_this_set " + size_this_set);
							if(num_really_sent_thisset>=size_this_set) {
								//System.out.println("REDO FALSE ESCO DAL WHILE");
								redo=false;
							}
							//port=11000+Integer.valueOf(missing_msg.substring(0,missing_msg.indexOf(" ")));
							if(myID==2 && port==11001) {
								//System.out.println("X SONO P2 E STO MANDANDO UN MESSAGGIO A P1");
								if(y>=num_mess || count_done_sent>=size_this_set) {
									//System.out.println("X SONO P2 E STO MANDANDO UN MESSAGGIO A P1 E ORA DOVREI MANDARE");
								}
							}
							int w=j+1;
							//OCCHIO SE VUOI RIPROVARE CON TANTE DEVI TOGLIERE LA RIGA SOTTO
							port=11000+Integer.valueOf(missing_msg.substring(0,missing_msg.indexOf(" ")));
							//System.out.println("PORTA::::: " + port + ", SONO NEL SET DEL PROCESSO" + w);
							if(y>=num_mess || count_done_sent>=size_this_set-1) {
								
								//System.out.println("\nthis set is"+ considered_map.keySet());
								//System.out.println("content I sent is == " + content + " y is " + y + " count_done_sent "+ count_done_sent);
								y=0;
								if(myID==2 && port==11001) {
									//System.out.println("Y SONO P2 E STO MANDANDO UN MESSAGGIO A P1");
								}
								Task_send task_send = new Task_send((content).getBytes(), ip, port, this, parser);			
								executor_send.execute(task_send);
								content="";
							}
						}
					//}
				}
			}				
			try {
				if(sets_missing.get(parser.myId()-1).size()<300) {
					Thread.sleep(400);
				}
				else if(sets_missing.get(parser.myId()-1).size()<9500) {
					Thread.sleep(2000);
				}
				else if(sets_missing.get(parser.myId()-1).size()<50000){
					Thread.sleep(4000);
				}
				else {
					Thread.sleep(4000);
				}
			} catch (java.lang.InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public int get_broadcast() {
		return broadcast;
	}
	
	public void update_list_clock(int id) {		//the receiver updates the value in a cell everytime a message arrives (id is the id of the sender)
		my_list_clock.set(id-1,my_list_clock.get(id-1)+1);
	}
	
	synchronized boolean can_log(List<Integer> list_clock_pending, String msg_log) {
		boolean canLog=true;
		if(logs.containsKey(msg_log))
			return false;
		String senderId=msg_log.substring(2);  //d 2 3 tolko il d e poi leggo il 2
		int senderIdVal=Integer.valueOf(senderId.substring(0,senderId.indexOf(" ")));
		for(int i=1;i<list.get(senderIdVal).size();i++) { //da uno perche' il primo numero indica ID, il secondo indica le dependencies
			//qui total causal broadcast. Se vuoi localized, guarda di confrontare solo l'entri che ti interessa
			//ottieni id di chi ti manda il messaggio
			//guarda da chi dipende quel process
			int pos=list.get(senderIdVal).get(i)-1;
			if(my_list_clock.get(pos)<=list_clock_pending.get(pos)) {
				canLog=false;
				break;
			}
		}
		return canLog;
	}
	
	public String get_string_my_clock(){
		//puoi semplificare con unsemplice for che stampa tutti gli elementi in una stringa separati da uno spazio0
		String a = my_list_clock.toString();
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
	public List<Integer> get_list_sender_clock(String str_clock){
		List<Integer> new_clock_list=new ArrayList<Integer>();
		Scanner scan=new Scanner(str_clock);
		while(scan.hasNextInt()) {
			new_clock_list.add(scan.nextInt());
		}
		return new_clock_list;
	}
	
	public void store_log(String msg_log, String str_clock) {
		map_store_log.put(msg_log,str_clock);
	}

	public void check_log() throws java.lang.InterruptedException {
		while(true) {
			int i=0;
			while(true) {
				int val=broadcast+1;
				String temp = "b " + val +"\n";
				if(map_store_log.containsKey(temp)) {
					add(temp);
					System.out.println("AGGIUNTO IN CHECK_LOG() con broadcast == " + broadcast);
				}
				else {
					break;
				}
				i++;
			}
			/*for(int k=0;k<my_list_clock.size();k++) {
				while(true) {
					int numPossibleMessage = my_list_clock.get(k)+1;
					String temp = "d " + k + " " + numPossibleMessage +"\n";	//d originalSender numMessage
					if(map_store_log.containsKey(temp)) {
						add(temp);
						System.out.println("AGGIUNTO IN CHECK_LOG() con delivery == ");
					}
					else {
						break;
					}
				}
			}*/
			
			for(String stored_log : map_store_log.keySet()) {
				if(stored_log.charAt(0)=='b') {
					continue;
				}
				String senderId=stored_log.substring(2);
				senderId=senderId.substring(0,senderId.indexOf(" "));
				if(can_log(get_list_sender_clock(map_store_log.get(stored_log)),stored_log)) {
					//System.out.println("logged from map_store_log");
					add(stored_log);
				}
			}
			try {
				if(map_store_log.size()<300) {
					Thread.sleep(1000);
				}
				else if(map_store_log.size()<9500) {
					Thread.sleep(2000);
				}
				else if(map_store_log.size()<50000){
					Thread.sleep(4000);
				}
				else {
					Thread.sleep(4000);
				}
			} catch(java.lang.InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	synchronized public void add_set_missing(int IDOriginalSender, int myId, int message, String string_clock) {
		//System.out.println("string_clock " + string_clock);
		for (Host host : parser.hosts()) {
			if(host.getId() != myId) {
				String missing_content=host.getId() + " " + IDOriginalSender + " " + message;
				
				int jj=0;
				boolean ret=true;
				while(jj<maps_ack.size()) {
					if( maps_ack.get(jj).containsKey((Object)missing_content)) {
						if(missing_content.equals("3 2 4")) {
							//System.out.println("RIPETOOOO e IDOriginalSender == " + IDOriginalSender);
						}
						return;
					}
					jj++;
				}
				sets_missing.get(IDOriginalSender-1).put(missing_content,string_clock);			//es. 2 43 The information IDOriginalSender is indexOf the set in the array + 1
				
				/*if(!sets_missing.get(IDOriginalSender-1).containsKey(missing_content) && (!maps_ack.get(IDOriginalSender-1).containsKey((Object)missing_content))) {
					sets_missing.get(IDOriginalSender-1).put(missing_content,string_clock);			//es. 2 43 The information IDOriginalSender is indexOf the set in the array + 1
				}*/
			}
		}
	}
	
	public void add(String log) {
		String senderId=null;
		int senderIdVal=-1;
		if(log.charAt(0)=='b') {
			senderIdVal=parser.myId();
		}
		else {
			senderId=log.substring(2);
			senderIdVal=Integer.valueOf(senderId.substring(0,senderId.indexOf(" ")));
		}
		if(!logs.containsKey(log)) {
			if(log.charAt(0)=='b') {
				broadcast++;
			}
			logs.put(log,log);
			log_queue.add(log);
			update_list_clock(senderIdVal);
		}
		if(endd==true && logs.keySet().size()==hostsNumber*tot_number_messages) {
			System.out.println("\n*** RECEIVED ALL MESSAGES ***\n");
			endd=false;
		}
	}
	
	public void addAck(int IDOriginalSender, String logAck) {
		maps_ack.get(IDOriginalSender-1).put(logAck,"");			//1 2 int the receiver, 2 if the process is the sender
	}
	

	
	public void check() {
		for(int i=0; i<sets_missing.size();i++) {
			Iterator iter = maps_ack.get(i).keySet().iterator();
		    while (iter.hasNext()) {
		    	String aa=(String)iter.next();
	    		//System.out.println("messaggio da check " + aa);
				for(int y=0; y<sets_missing.size();y++) {
		    		if(null!=sets_missing.get(y).get((Object)aa)){
			    		//System.out.println("Ora leggo il valore alla Key data " + sets_missing.get(y).get((Object)aa));
			    		//System.out.println("Ora RIMUOVO il valore alla Key data " + sets_missing.get(y).remove((Object)aa));
		    		}
				}
		    }
		}
	}
	
	public void writeOutput() {
		System.out.println("*** NUMBER OF LOGS *** == " + logs.size());
		try(BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputPath))) {
			/*Iterator <String> iter = logs.keySet().iterator();
			while(iter.hasNext()) {
				fileWriter.write(iter.next());
			}*/
			String temp="";
			while(!log_queue.isEmpty()) {
				String message=log_queue.poll();
				if(temp.equals(message))
					continue;
				temp=message;
				fileWriter.write(message);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}