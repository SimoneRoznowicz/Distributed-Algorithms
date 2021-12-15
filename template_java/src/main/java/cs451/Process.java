package cs451;
import java.net.DatagramPacket; 
import java.net.InetAddress;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;


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
			logger.logger_sender(list_payloads, ip, port, myId, receiverId, outputPath);
		}
	}
}

