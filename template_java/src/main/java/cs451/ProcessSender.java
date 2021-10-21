package cs451;
import java.net.DatagramPacket; 
import java.net.InetAddress; 


public class ProcessSender {
	private int length;
	private byte buf[];
	private int port;
	private int type; 				//I may send a message with content or it may simply be an acknowledgment message
	private InetAddress ip;
	
	public ProcessSender(byte[] abuf, int atype, InetAddress anip, int aport) {
		buf=abuf;
		type=atype;
		ip=anip;
		port=aport;
		// new sender --> do in background send
		// new receiver --> do in background receive
	}
	
	public void sendMessage(){		//ANZI DATO CHE POSSO NON SAPERE IL NUMERO, MEGLIO METTERE TUTTO DENTRO UNA LISTA E MANDARE OGNI MESSAGGIO NELLA LISTA			
		UDP_packet send_pack = new UDP_packet(buf, type, ip, port); //if type == 0 --> acknowledgement packet 
		send_pack.send();
	}
	
	public int get_type(){
		return type;
	}
	
	public int get_port(){
		return port;
	}
	
	public int get_length(){
		return buf.length;
	}
	
	public String get_Message_content(){
		return buf.toString();
	}
}


