package cs451;
import java.net.DatagramPacket; 
import java.net.InetAddress;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

/*
 * Cosa devo fare per implementare acknowledgement?? 
 * - mando un messaggio, thread.sleep(millisecondi)
 * - se in questo tempo NON arriva un messaggio ack numero del messaggio, allora reinvio il messaggio (lo gestisce chi??)
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
	private List<String> list_payloads;
	private int number_threads_send;
	private int number_threads_receive;
	private boolean isInterrupted;
	private String outputPath;
	private MyLogger logger;
	private Parser parser;
	
	public Process(List<String> list_payloads, int type, InetAddress ip, int port, int myId, int receiverID, String outputPath, MyLogger llogger, Parser parser) {
		this.list_payloads=list_payloads;
		this.type=type;
		this.ip=ip;
		this.port=port;
		this.myId=myId;
		this.receiverId=receiverID;
		this.outputPath=outputPath;
		logger=llogger;
		isInterrupted=false;
		this.parser=parser;
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
			UDP_packet rec_pack = new UDP_packet(port, outputPath, list_payloads.size(), logger, ip, parser);
			//for(int i=0;i<10;i++) {
			Task_receive task_receive = new Task_receive(rec_pack);
			executor_receive.execute(task_receive);
			//}
			//rec_pack.receive();
	        executor_receive.shutdown();
		}
	}
	
	public void sendAll() throws java.net.UnknownHostException{
		if (myId!=receiverId) {					//This process has to SEND the messages
			//create the list of ProcessSender messages
			System.out.println("SONO DENTRO SEND PROCESS, list_payloads.size() == " + list_payloads.size());
			
			
		/////////////////// ricevo tutti ack
			int myPort=0;
			InetAddress myIp=null;
			for (Host host: parser.hosts()) {
		    	if(host.getId() == parser.myId()) {
		    		myPort = host.getPort();
		    		myIp = InetAddress.getByName(host.getIp());
		    	}
		    }
			ExecutorService executor_rec_ack = Executors.newSingleThreadExecutor();
			//ThreadPoolExecutor executor_rec_ack = (ThreadPoolExecutor) Executors.newSingleThreadExecutor();
			UDP_packet rec_pack_ack = new UDP_packet(myPort, outputPath, list_payloads.size(), logger, myIp, parser);
			Task_receive task_rec_ack = new Task_receive(rec_pack_ack);
			executor_rec_ack.execute(task_rec_ack);			
		///////////////////
			
			number_threads_send=100;
			ThreadPoolExecutor executor_send = (ThreadPoolExecutor) Executors.newFixedThreadPool(number_threads_send);
			for (int i=0; i<list_payloads.size(); i++) {
				//QUI CI DEVI METTERE RECIEVE MODIFICATO PER MESSAGGI ACK: FA ESEGUIRE UN SINGLE THREAD APPOSITO
				//(RICHIAMA SOLO RECEIVE CHE PERO' DEVI MODIFICARE: OGNI ACK CHE RICEVO VIENE INSERITO IN UN SET DEL LOGGER
				//(COSI' NON HA DUPLICATI!!!!)).
				//DEVO AVERE ACK DI OGNI MESSAGGIO. SE ME NE MANCA UNO PER IL MESSAGGIO 3 ALLORA IL SENDER REINVIA MESS. 3
				//
	            Task_send task_send = new Task_send(list_payloads.get(i).getBytes(), type, ip, port, logger, parser);
	            executor_send.execute(task_send);
	        }
			//now check the list of messages which seem to be not arrived (until there are no messages left to be sent, keep sending the missing ones)
			ArrayList<String> list_missing = logger.check();
			int myID = parser.myId();
			while(list_missing.size()!=0) {
				System.out.println("list_missing.size() == " + list_missing.size());
				list_missing = logger.check();
				for(int i=0; i<list_missing.size(); i++) {
					Task_send task_send = new Task_send((myID + " " + list_missing.get(i)).getBytes(), type, ip, port, logger, parser);
	            	executor_send.execute(task_send);
				}
			}
	        executor_send.shutdown();
	        executor_rec_ack.shutdown();
		}
	}
}











