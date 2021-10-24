package cs451;
import java.net.SocketException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;


public class UDP_packet {
	private int length;
	private byte buf[];
	private DatagramPacket received_packet;
	private DatagramPacket sent_packet;
	private int port;
	private int type; 				//I may send a message with content or it may simply be an acknowledgment message
	private InetAddress ip;
	private int numMessages;
	private String outputPath;
	private MyLogger logger;
	private String origin;
	private Parser parser;
	
	//received_packet
	public UDP_packet(int port, String outputPath, int numMessages, MyLogger llogger, InetAddress ip, Parser parser) {
		this.port=port;
		this.numMessages=numMessages;
		this.outputPath=outputPath;
		logger=llogger;
		this.ip=ip;
		this.parser=parser;
	}
	
	//sent_packet
	public UDP_packet(byte[] buf, int type, InetAddress ip, int port, MyLogger llogger, Parser parser) {
		this.buf=buf; 
		this.type=type;
		this.port=port;
		this.length=buf.length;
		this.ip=ip;
		logger=llogger;
		this.parser=parser;
	}
	public UDP_packet(byte[] buf, int type, InetAddress ip, int port) {
		this.buf=buf; 
		this.type=type;
		this.port=port;
		this.length=buf.length;
		this.ip=ip;
	}
	
	public void send() {
		DatagramSocket ds = null;
		try {
			ds = new DatagramSocket();
		} catch(SocketException e) {
			e.printStackTrace();
		}
	    DatagramPacket dp = new DatagramPacket(buf, length, ip, port);		//DatagramPacket(byte[] barr, int length, InetAddress address, int port)
	    try {
	    	ds.send(dp);  //should ha 1 4 where 1 is the ID of the process and 4 the number of the message
	    	String str = new String(dp.getData(), 0, dp.getLength()); 
			int IDsender = new Scanner(str).nextInt();
	    	str = "b " + str.substring(2) + "\n";
	    	logger.add(str);
			//System.out.println("MESSAGGIO INVIATO:::: " + str);
	    } catch(IOException e) {
	    	e.printStackTrace();
	    }
	    ds.close();  
	}
	
	public void receive() {
		System.out.println("INSIDE RECEIVE METHOD OF UDP_packet");
		DatagramSocket ds = null;
		//ArrayList<String> messages=null;
		try {
			ds = new DatagramSocket(port);
		} catch(SocketException e) {
			e.printStackTrace();
		}
	    byte[] rec_buf = new byte[1024];  
	    DatagramPacket dp = new DatagramPacket(rec_buf, 1024);  
	    try {
			System.out.println("Appena prima di receive");
			//for (int i=0; i<300000/*numMessages*/; i++) {
			while (true) {		//keeps receiving 
				ds.receive(dp);   //should ha 1 4 where 1 is the ID of the process and 4 the number of the message
			    String str = new String(dp.getData(), 0, dp.getLength()); 
			    if(str.charAt(0)!=('r')) {	
			    	Scanner s = new Scanner(str);
					int IDsender = s.nextInt();
					int numberMessage = s.nextInt();
				    origin = IDsender + "";
				    int senderPort = 0;
				    for (Host host: parser.hosts()) {
				    	if(host.getId() == IDsender) {
				    		senderPort = host.getPort();
				    		ip = InetAddress.getByName(host.getIp());
				    	}
				    }
					String ack_buf = "r " + numberMessage;   //--> r 3    (acknowledgement message 3 from process 1)
				    str = "d " + str + "\n";   
				    logger.add(str);
					//System.out.println("MESSAGGIO RICEVUTO:::: " + str);
					//NOW SEND BACK THE ACKNOWLEDGEMENT
					DatagramSocket ds1 = new DatagramSocket();
					DatagramPacket dp1 = new DatagramPacket(ack_buf.getBytes(), ack_buf.length(), ip, senderPort);
					
					ds1.send(dp1);
			    }
			    else {  //I'm the sender and I'm receiving an ack message: so I should check it and store the content
			    	str=str.substring(2);   //es r 43 (number of the message)
					//System.out.println("MESSAGGIO RICEVUTO ack:::: " + str);
					logger.addAck(str);
			    }
			}			
	    } catch(IOException e) {
		    e.printStackTrace();
		}
	   	ds.close(); 
	}
}





