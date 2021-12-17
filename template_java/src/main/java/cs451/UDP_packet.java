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
	    	//System.out.println("*** MESSAGGIO DA MANDARE == " + str);
	    	int count$=0;
	    	for(int i=0;i<str.length();i++) {
				if(str.charAt(i)=='$') {
					count$++;
				}
			}
	    	int iteration=0;
	    	int index=0;
	    	int index$=0;
	    	String orig_modif_msg=str;
	    	while(iteration<(count$+1)) {
	    		str=orig_modif_msg;
	    		
    	    	
    	    	String str_clock = str.substring(0,index);
    	    	for(int i=0;i<str.length();i++) {
    	    		if(str.charAt(i)=='|') {
    	    			index=i;
    	    			break;
    	    		}
    	    	}
    	    	//System.out.println("str........ " + str.length() + " str == " + str + "index == "+ index);
    	    	if(str.length()==0)
    	    		break;
    	    	str=str.substring(index+1);
		    	Scanner scanner = new Scanner(str);
				int IDsender = scanner.nextInt();
				int IDOriginalsender = scanner.nextInt();
				scanner.nextInt();
				String a = str.substring(str.indexOf(" ")+1);
				index$=0;
				for(int i=0;i<a.length();i++) {
    	    		if(a.charAt(i)=='$') {
    	    			index$=i;
    	    			break;
    	    		}
    	    	}
				if(index$==0) {
					a=a.substring(str.indexOf(" ")+1);
					//System.out.println("a1 |||||||||== "+ a.length());
				}
				else {
					a=a.substring(str.indexOf(" ")+1,index$-1);
					//System.out.println("a2 |||||||||== "+ a.length());
				}
				str = "b " + a + "\n";
				//System.out.println("what I am going to log  " + str + "STR.LENGTH() == " + str.length());
		    	logger.add(str);
		    	logger.update_list_clock(parser.myId());
		    	if(index$==-1)
					break;
		    	for(int i=0;i<orig_modif_msg.length();i++) {
    	    		if(orig_modif_msg.charAt(i)=='$') {
    	    			index$=i;
    	    			break;
    	    		}
    	    	}
				orig_modif_msg=orig_modif_msg.substring(index$+1);
		    	iteration++;
	    	}
	    	
	    } catch(IOException e) {
	    	e.printStackTrace();
	    }
	    dss.close();  
	}
	
	public void receive() {
	   Thread myThread = new Thread(){
	    	public void run(){
	    		try {
	    			logger.check_log();
	    		} catch(java.lang.InterruptedException e) {
	    			e.printStackTrace();
	    		}
		    }
	    };
	    myThread.start();
	    
		DatagramSocket dsr = null;
		
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
			int num_rec_threads1 = 5;
			int num_rec_threads2 = 220;
			client_handle1 = (ThreadPoolExecutor) Executors.newFixedThreadPool(num_rec_threads1);
			client_handle2 = (ThreadPoolExecutor) Executors.newFixedThreadPool(num_rec_threads2);
			while (true) {		//keeps receiving 
				dsr.receive(dpr);		//should ha 1 4 where 1 is the ID of the process and 4 the number of the message
			    String msg = new String(dpr.getData(), 0, dpr.getLength());
    	    	//System.out.println("receive message origin: " + msg);
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
        	//in the block of messages, if the first is r, then all the others are r
        	msg=msg.substring(2);   //es r 1 1 43 ---> 1 1 43 (43rd acknowledgement message received form host 1)
			Scanner s = new Scanner(msg);
			//System.out.println("ACK MESSAGE ===== " + msg);
			int IDsender = s.nextInt();
			int IDOriginalSender = s.nextInt();
			int num_mess=s.nextInt();
			for(int j=1;j<=parser.hosts().size();j++) {
				msg=IDOriginalSender+ " " + j + " " + num_mess;
	        	logger.addAck(IDOriginalSender, msg);
			}
        	//logger.addAck(IDOriginalSender, msg);
        }
	}
	
	private class ClientHandler implements Runnable {
        private String msg;
  
        public ClientHandler(String msg) {
            this.msg = msg;
        }
  
        public void run() {
    	    try {
    	    	//count how many $ you have
    	    	//System.out.println("receive message: " + msg);
    	    	int count$=0;
    	    	for(int i=0;i<msg.length();i++) {
    				if(msg.charAt(i)=='$') {
    					count$++;
    				}
    			}
    	    	int iteration=0;
    	    	String orig_modif_msg=msg;
    	    	while(iteration<(count$+1)) {
	    	    	//ci sono count$+1 messaggi
    	    		msg=orig_modif_msg;
    	    		//System.out.println("RECEIVED MESSAGE at the moment is " + msg);
    	    		//System.out.println("MSG receiver packet == " + msg);
	    	    	int index=-1;
	    	    	int index$=-1;
	    	    	for(int i=0;i<msg.length();i++) {
	    	    		if(msg.charAt(i)=='|') {
	    	    			index=i;
	    	    			break;
	    	    		}
	    	    	}
	    	    	
	    	    	String str_clock = msg.substring(0,index);
	    	    	msg=msg.substring(index+1);
			    	Scanner s = new Scanner(msg);
					int IDsender = s.nextInt();		//id of the last sender
					int IDOriginalSender = s.nextInt();		//id of the original first sender
					logger.update_list_clock(IDOriginalSender);
					int numberMessage = s.nextInt();
				    origin = IDsender + "";
				    int senderPort = 0;
				    for (Host host: parser.hosts()) {
				    	if(host.getId() == IDsender) {
				    		senderPort = host.getPort();
				    		ip = InetAddress.getByName(host.getIp());
				    	}
				    }
					String ack_buf = "r " + IDsender + " " + IDOriginalSender + " " + numberMessage;   //--> r 1 2 43    (acknowledgement message 43 from process 2 on behalf of process 1)
					index$=-1;
					for(int i=0;i<msg.length();i++) {
	    	    		if(msg.charAt(i)=='$') {
	    	    			index$=i;
	    	    			break;
	    	    		}
	    	    	}
					//String msg_log = "d " + msg.substring(msg.indexOf(" ")+1,index$) + "\n";  
					String msg_log=null;
					if(index$==-1) {
						msg_log = "d " + msg.substring(msg.indexOf(" ")+1) + "\n";  
					}
					else {
						msg_log = "d " + msg.substring(msg.indexOf(" ")+1,index$-1) + "\n";  
						//System.out.println("msg_log 2 |||||||||== "+ msg_log.length());
					}
					if(IDsender!=parser.myId() && IDOriginalSender!=parser.myId()) {
						//METTICI LE CONDIZIONI PER PASSARE:
						if(logger.can_log(logger.get_list_sender_clock(str_clock),IDOriginalSender)==true) {
							logger.update_list_clock(IDOriginalSender);
							logger.add(msg_log);
						}
						else {
							//store the log..it will be logged later
							logger.store_log(msg_log,str_clock);
						}
					}
					
					DatagramSocket ds1 = new DatagramSocket();
					DatagramPacket dp1 = new DatagramPacket(ack_buf.getBytes(), ack_buf.length(), ip, senderPort);
					ds1.send(dp1);
					ds1.close();
					
	
					//here I broadcast this message to all the other processes
					for (Host host: parser.hosts()) {
				    	if(host.getId() != IDsender) {
				    		//dovrei aggiungere solo se non ho ack
							logger.add_set_missing(IDOriginalSender, parser.myId(), numberMessage, str_clock);
				    	}
				    }
					//reduce the length of string msg for the next iteration
					iteration++;
					if(index$==-1)
						break;
					for(int i=0;i<orig_modif_msg.length();i++) {
	    	    		if(orig_modif_msg.charAt(i)=='$') {
	    	    			index$=i;
	    	    			break;
	    	    		}
	    	    	}
					orig_modif_msg=orig_modif_msg.substring(index$+1);
    	    	}
				
    	    } catch(IOException e) {
    		    e.printStackTrace();
    		}
        }
    }
}



