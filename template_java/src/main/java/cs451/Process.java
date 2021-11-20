package cs451;
import java.net.DatagramPacket; 
import java.net.InetAddress;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;


/*
 * Cosa devo fare per implementare acknowledgement?? 
 * - mando un messaggio, thread.sleep(millisecondi)
 * - se in questo tempo NON arriva un messaggio ack numero del messaggio, allora reinvio il messaggio (lo gestisce chi??)
 */

public class Process {
	
	private int length;
	private byte buf[];
	private int port;
	private int myId;
	private int receiverId;
	private InetAddress ip;
	private List<String> list_payloads;
	private int number_threads_send;
	private int number_threads_receive;
	private boolean isInterrupted;
	private String outputPath;
	private MyLogger logger;
	private Parser parser;
	
	public Process(List<String> list_payloads, InetAddress ip, int port, int myId, int receiverId, String outputPath, MyLogger llogger, Parser parser) {
		this.list_payloads=list_payloads;
		this.ip=ip;
		this.port=port;
		this.myId=myId;
		this.receiverId=receiverId;
		this.outputPath=outputPath;
		logger=llogger;
		isInterrupted=false;
		this.parser=parser;
	}
	
	public void receiveAll() {
		if(myId==receiverId) {	//This process has to RECEIVE the messages
			ExecutorService executor_receive = Executors.newSingleThreadExecutor();
			UDP_packet rec_pack = new UDP_packet(port, outputPath, list_payloads.size(), logger, ip, parser);
			Task_receive task_receive = new Task_receive(rec_pack);
			executor_receive.execute(task_receive);
	        executor_receive.shutdown();
		}
	}
	
	public void sendAll() throws java.net.UnknownHostException {
		if (myId!=receiverId) {					//This process has to SEND the messages
			int myPort=0;
			InetAddress myIp=null;
			for (Host host: parser.hosts()) {
		    	if(host.getId() == parser.myId()) {
		    		myPort = host.getPort();
		    		myIp = InetAddress.getByName(host.getIp());
		    	}
		    }
			//receive all ack packets
			/*ExecutorService executor_rec_ack = Executors.newSingleThreadExecutor();
			UDP_packet rec_pack_ack = new UDP_packet(myPort, outputPath, list_payloads.size(), logger, myIp, parser);
			Task_receive task_rec_ack = new Task_receive(rec_pack_ack);
			executor_rec_ack.execute(task_rec_ack);			
			*/
			number_threads_send=1;
			ThreadPoolExecutor executor_send = (ThreadPoolExecutor) Executors.newFixedThreadPool(number_threads_send);
			for (int i=0; i<list_payloads.size(); i++) {
				//System.out.println("listpayloads.get(i) %%%%%% = " + list_payloads.get(i));
	            Task_send task_send = new Task_send(list_payloads.get(i).getBytes(), ip, port, logger, parser);
	            executor_send.execute(task_send);
	        }
			//now check the list of messages which seem to be not arrived (until there are no messages left to be sent, keep sending the missing ones)
			List<HashSet<String>> sets_missing=null;
			sets_missing = logger.check();
			
			int myID = parser.myId();
			while(sets_missing.get(myID-1).size()!=0) {

				//System.out.println(set_missing.size());
				//for(int i=0; i<sets_missing.size(); i++) {
				//Iterator <Integer> iter = sets_missing.keySet().iterator();
				//while(iter.hasNext()) {
				synchronized (sets_missing) {
					sets_missing = logger.check();
					Iterator iter = sets_missing.iterator(); // Must be in synchronized block
				    while (iter.hasNext()) {
						for(String missing_msg : (HashSet<String>)iter.next()) {
							String content = myID + " " + missing_msg.substring(2);
							//System.out.println("content===== " + content);
							//System.out.println("missing_msg.substring(2)===== " + missing_msg.substring(2));
							Task_send task_send = new Task_send((myID + " " + missing_msg.substring(2)).getBytes(), ip, port, logger, parser);
			            	executor_send.execute(task_send);
						}
					}
				}				//}
				//}
				try {
					//Thread.sleep(20000);
					if(sets_missing.get(parser.myId()-1).size()<300) {
						Thread.sleep(100);
					}
					else if(sets_missing.get(parser.myId()-1).size()<9500) {
						Thread.sleep(400);
					}
					else if(sets_missing.get(parser.myId()-1).size()<50000){
						Thread.sleep(2000);
					}
					else {
						Thread.sleep(4000);
					}
				} catch (java.lang.InterruptedException e) {
					e.printStackTrace();
				}
				
			}
	        executor_send.shutdown();
	        //executor_rec_ack.shutdown();
		}
	}
}

