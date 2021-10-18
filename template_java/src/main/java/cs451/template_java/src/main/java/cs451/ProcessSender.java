package cs451;
import java.net.DatagramPacket; 
import java.net.InetAddress; 


public class ProcessSender {
	private int length;
	private byte buf[];
	private DatagramPacket rec.eived_packet;
	private DatagramPacket sent_packet;
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
		int counter=0;
		while(counter<1) {
			//manda tutti i messaggi
			//sostituire 127.0.0.1 con elemento della lista che contiene destinazioni
			
    		Packet_UDP send_pack = new Packet_UDP(buf, 1, ip, port); //if type == 0 --> acknowledgement packet 
    		pack.send();
		}
	}
	
	public void get_type(){
		return type;
	}
	
	public void get_port(){
		return port;
	}
	
	public void get_length(){
		return buf.length();
	}
}
