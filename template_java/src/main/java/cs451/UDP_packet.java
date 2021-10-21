package cs451;
import java.net.SocketException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
	
	//received_packet
	public UDP_packet(int aport) {
		port=aport;
		//DatagramPacket received_packet = new DatagramPacket(byte[] buf, 0, buf.length);
	}

	//sent_packet
	public UDP_packet(byte[] abuf, int atype, InetAddress anip, int aport) {
		buf=abuf; 
		type=atype;
		port=aport;
		length=buf.length;
		ip=anip;
		//DatagramPacket sent_packet = new DatagramPacket(buf, 0, buf.length, SocketAddress address);
	}
	
	public void send() {
		DatagramSocket ds = null;
		try {
			ds = new DatagramSocket();
		} catch(SocketException e) {
			e.printStackTrace();
		}
	    String str = "Welcome java";  
	    //InetAddress ip1 = InetAddress.getByName("127.0.0.1");     
	    DatagramPacket dp = new DatagramPacket(str.getBytes(), str.length(), ip, port);		//DatagramPacket(byte[] barr, int length, InetAddress address, int port)
	    try {
	    	ds.send(dp);  
	    } catch(IOException e) {
	    	e.printStackTrace();
	    }
	    ds.close();  
		//InetAddress piAddr = InetAddress.getByName("localhost");
	}
	
	public void receive() {
		DatagramSocket ds = null;
		try {
			ds = new DatagramSocket(port);
		} catch(SocketException e) {
			e.printStackTrace();
		}
	    byte[] rec_buf = new byte[1024];  
	    DatagramPacket dp = new DatagramPacket(rec_buf, 1024);  
	    
	    try {
		    ds.receive(dp);  
	    } catch(IOException e) {
	    	e.printStackTrace();
	    }
	    
	    String str = new String(dp.getData(), 0, dp.getLength());  
	    System.out.println(str);  
	    ds.close(); 
	}
}






