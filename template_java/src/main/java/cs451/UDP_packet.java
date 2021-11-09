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
		this.ip=ip;
		this.parser=parser;
		logger=llogger;
	}
	
	//sent_packet
	public UDP_packet(byte[] buf, InetAddress ip, int port, MyLogger llogger, Parser parser) {
		this.buf=buf; 
		this.port=port;
		this.length=buf.length;
		this.ip=ip;
		this.parser=parser;
		logger=llogger;
	}
	
	public void send() {
		DatagramSocket dss = null;
		try {
			dss = new DatagramSocket();
		} catch(SocketException e) {
			e.printStackTrace();
		}
	    DatagramPacket dps = new DatagramPacket(buf, length, ip, port);		//DatagramPacket(byte[] barr, int length, InetAddress address, int port)
	    try {
	    	dss.send(dps);  //should ha 1 4 where 1 is the ID of the process and 4 the number of the message
	    	String str = new String(dps.getData(), 0, dps.getLength()); 
	    	Scanner scanner= new Scanner(str);
			int IDsender = scanner.nextInt();
			scanner.nextInt();
			//System.out.println("str ======= " + str);
			if(!scanner.hasNextInt()) {
				str = "b " + str.substring(2) + "\n";
		    	logger.add(str);
			}
			//System.out.println("MESSAGGIO INVIATO:::: " + str);
	    } catch(IOException e) {
	    	e.printStackTrace();
	    }
	    dss.close();  
	}
	
	public void receive() {
		DatagramSocket dsr = null;
		//ArrayList<String> messages=null;
		try {
			dsr = new DatagramSocket(port);
		} catch(SocketException e) {
			e.printStackTrace();
		}
	    byte[] rec_buf = new byte[1024];  
	    DatagramPacket dpr = new DatagramPacket(rec_buf, 1024);
	    ThreadPoolExecutor client_handle1=null;
	    ThreadPoolExecutor client_handle2=null;
	    try {
			System.out.println("Appena prima di receive");
			int num_rec_threads1 = 5;
			int num_rec_threads2 = 50;
			client_handle1 = (ThreadPoolExecutor) Executors.newFixedThreadPool(num_rec_threads1);
			client_handle2 = (ThreadPoolExecutor) Executors.newFixedThreadPool(num_rec_threads2);
			while (true) {		//keeps receiving 
				//System.out.println("ciao\n");
				dsr.receive(dpr);   //should ha 1 4 where 1 is the ID of the process and 4 the number of the message
			    String msg = new String(dpr.getData(), 0, dpr.getLength());
			    if(msg.charAt(0)!=('r')) {
			    	ClientHandler clientSock = new ClientHandler(msg);
		            client_handle1.execute(clientSock);
			    }
			    else {  //I'm the sender and I'm receiving an ack message: so I should check it and store the content
			    	AckHandler ackHandler = new AckHandler(msg);
		            client_handle2.execute(ackHandler);
			    }
			}		
	    } catch(IOException e) {
		    e.printStackTrace();
		}
        client_handle1.shutdown();
        client_handle2.shutdown();
	   	dsr.close(); 
	}
	
	
	
	private class AckHandler implements Runnable {
        private String msg;
  
        public AckHandler(String msg) {
            this.msg = msg;
        }
  
        public void run() {
        	msg=msg.substring(2);   //es r 1 43 (43rd acknowledgement message received form host 1)
			logger.addAck(msg);
        }
        
	}
	
	private class ClientHandler implements Runnable {
        private String msg;
  
        public ClientHandler(String msg) {
            this.msg = msg;
        }
  
        public void run() {
    	    try {
		    	Scanner s = new Scanner(msg);
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
				String ack_buf = "r " + IDsender + " " + numberMessage;   //--> r 1 43    (acknowledgement message 43 from process 1)
				msg = "d " + msg + "\n";   
				//System.out.println("MESSAGGIO RICEVUTO:::: " + str);
				//NOW SEND BACK THE ACKNOWLEDGEMENT
			    logger.add(msg);
				DatagramSocket ds1 = new DatagramSocket();
				DatagramPacket dp1 = new DatagramPacket(ack_buf.getBytes(), ack_buf.length(), ip, senderPort);
				
				ds1.send(dp1);
				ds1.close();
    	    } catch(IOException e) {
    		    e.printStackTrace();
    		}
        }
    }
}



