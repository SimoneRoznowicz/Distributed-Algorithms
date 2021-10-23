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
import java.io.FileWriter;

/*
 * che fare?
 * 1 inviare un messaggio:
 * 		
 */
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
	
	//received_packet
	public UDP_packet(int port, String outputPath, int numMessages, MyLogger llogger) {
		this.port=port;
		this.numMessages=numMessages;
		this.outputPath=outputPath;
		logger=llogger;
		//DatagramPacket received_packet = new DatagramPacket(byte[] buf, 0, buf.length);
	}
	
	//sent_packet
	public UDP_packet(byte[] buf, int type, InetAddress ip, int port, MyLogger llogger) {
		this.buf=buf; 
		this.type=type;
		this.port=port;
		this.length=buf.length;
		this.ip=ip;
		logger=llogger;
		//DatagramPacket sent_packet = new DatagramPacket(buf, 0, buf.length, SocketAddress address);
	}
	
	public void send() {
		DatagramSocket ds = null;
		ArrayList<String> messages = new ArrayList<String>(10);
		try {
			ds = new DatagramSocket();
		} catch(SocketException e) {
			e.printStackTrace();
		}
	    DatagramPacket dp = new DatagramPacket(buf, length, ip, port);		//DatagramPacket(byte[] barr, int length, InetAddress address, int port)
	    try {
	    	ds.send(dp);  
	    	String str = new String(dp.getData(), 0, dp.getLength()); 
	    	logger.add(str);
			messages.add(str);
			System.out.println("MESSAGGIO INVIATO:::: " + str);
	    } catch(IOException e) {
	    	e.printStackTrace();
	    }
	    ds.close();  
	}
	
	public void receive() {
		System.out.println("INSIDE RECEIVE METHOD OF UDP_packet");
		DatagramSocket ds = null;
		ArrayList<String> messages=null;
		try {
			ds = new DatagramSocket(port);
		} catch(SocketException e) {
			e.printStackTrace();
		}
	    byte[] rec_buf = new byte[1024];  
	    DatagramPacket dp = new DatagramPacket(rec_buf, 1024);  
	    try {
			System.out.println("Appena prima di receive");
			messages = new ArrayList<String>(10);
			for (int i=0; i<300000/*numMessages*/; i++) {
			    ds.receive(dp); 
			    String str = new String(dp.getData(), 0, dp.getLength()); 
			    messages.add(str);
			    str = "d " + "x " + str + "\n";
			    logger.add(str);
				System.out.println("MESSAGGIO RICEVUTO:::: " + str);		    
			}			
	    } catch(IOException e) {
		    e.printStackTrace();
		}
	   	ds.close(); 
   		System.out.println("Tutti messaggi ricevuti: ");
	   	for(String message : messages) {
	   		System.out.println("message == " + message);
	   	}
	   	FileWriter myWriter = null;;
	   	try {
	        myWriter = new FileWriter("fileprova.txt");
	        for (int i=0; i<messages.size(); i++) {
		        myWriter.write("d " + "1 " + messages.get(i) + "\n");
			} 
	        myWriter.close();
	   	} catch (IOException e) {
		    e.printStackTrace();
		}
        System.out.println("Successfully wrote to the file.");
	}
}





