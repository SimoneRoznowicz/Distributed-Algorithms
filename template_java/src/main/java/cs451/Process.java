package cs451;
import java.net.DatagramPacket; 
import java.net.InetAddress;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.ArrayList;


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
	
	public Process(List<String> list_payloads, int type, InetAddress ip, int port, int myId, int receiverID) {
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
	
	public void receiveAll() {
		//QUI CI PUOI METTERE IL FILTRO: RICEVI SE SEI ID DA RICEVERE, SENNO' INVIA
		//mess_queue.size()>=10 ? number_threads=mess_queue.size()/10 : number_threads=1;
		if(myId==receiverId) {	//This process has to RECEIVE the messages
			System.out.println("SONO DENTRO RECEIVE PROCESS");
			ThreadPoolExecutor executor_receive = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
			//ThreadPoolExecutor executor_receive = (ThreadPoolExecutor) Executors.newSingleThreadExecutor();
			//ProcessReceiver proc_rec = new ProcessReceiver(port);
			UDP_packet rec_pack = new UDP_packet(port);
			//for(int i=0;i<10;i++) {
				Task_receive task_receive = new Task_receive(rec_pack);
				executor_receive.execute(task_receive);
			//}
			//rec_pack.receive();
	        executor_receive.shutdown();
		}
	}
	
	public void sendAll() {
		if (myId!=receiverId) {					//This process has to SEND the messages
			//create the list of ProcessSender messages
			System.out.println("SONO DENTRO SEND PROCESS, list_payloads.size() == " + list_payloads.size());
			/*for (int i=0; i<list_payloads.size(); i++) {
				ProcessSender proc_sen = new ProcessSender(list_payloads.get(i).getBytes(), type, ip, port);
				mess_queue.add(proc_sen);
			}*/
			number_threads_send=3;
			System.out.println("Numero di messaggi (ProcessSender) da mandare == " + mess_queue.size());
			ThreadPoolExecutor executor_send = (ThreadPoolExecutor) Executors.newFixedThreadPool(number_threads_send);
			for (int i=0; i<list_payloads.size(); i++) {
	            Task_send task_send = new Task_send(list_payloads.get(i).getBytes(), type, ip, port);
	            executor_send.execute(task_send);
	        }
	        executor_send.shutdown();
		}
	}
}











