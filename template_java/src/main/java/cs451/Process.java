package cs451;
import java.net.DatagramPacket; 
import java.net.InetAddress;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;


/*
 * Goal of the class:
 * - send a message
 * - receive acknowledgement message
 * - resend a message if not arrived 
 */

public class Process {
	
	private int length;
	private byte buf[];
	private DatagramPacket received_packet;
	private DatagramPacket sent_packet;
	private int port;
	private int type; 				//I may send a message with content or it may simply be an acknowledgment message
	private int myId;
	private int receiverId;
	private InetAddress ip;
	private List<ProcessSender> mess_queue;
	private List<String> list_payloads;
	private int number_threads_send;
	private int number_threads_receive;
	private boolean isInterrupted;
	
	public Process(ArrayList<String> list_payloads, int type, InetAddress ip, int port, int myId, int receiverID) {
		this.list_payloads=list_payloads;
		this.type=type;
		this.ip=ip;
		this.port=port;
		this.myId=myId;
		this.receiverId=receiverID;
		isInterrupted=false;
		mess_queue = new ArrayList<ProcessSender> (10);

		// new sender --> do in background send
		// new receiver --> do in background receive
	}
	
	public void sendReceiveAll() {
		//QUI CI PUOI METTERE IL FILTRO: RICEVI SE SEI ID DA RICEVERE, SENNO' INVIA
		//mess_queue.size()>=10 ? number_threads=mess_queue.size()/10 : number_threads=1;
		if(myId==receiverId) {	//This process has to RECEIVE the messages
			ThreadPoolExecutor executor_receive = (ThreadPoolExecutor) Executors.newSingleThreadExecutor();
			ProcessReceiver proc_rec = new ProcessReceiver(port);
	        Task_receive task_receive = new Task_receive(proc_rec);  
	        while(!isInterrupted) {
	        	executor_receive.execute(task_receive);
	        }
	        executor_receive.shutdown();
		}
		else {					//This process has to SEND the messages
			//create the list of ProcessSender messages
			for (int i=0; i<list_payloads.size(); i++) {
				ProcessSender proc_sen = new ProcessSender(list_payloads.get(i).getBytes(), type, ip, port);
				mess_queue.add(proc_sen);
			}
			number_threads_send=3;
			ThreadPoolExecutor executor_send = (ThreadPoolExecutor) Executors.newFixedThreadPool(number_threads_send);
			while(!mess_queue.isEmpty()) {
	            Task_send task_send = new Task_send(mess_queue.remove(0));
	            executor_send.execute(task_send);
	        }
	        executor_send.shutdown();
		}
	}
}











