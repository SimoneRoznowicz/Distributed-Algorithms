package cs451;
import android.os.AsyncTask;

public class ProcessReceiver {
	private int length;
	private byte buf[];
	private DatagramPacket received_packet;
	private DatagramPacket sent_packet;
	private int port;
	private int type; 				//I may send a message with content or it may simply be an acknowledgment message
	private InetAddress ip;
	
	public ProcessReceiver(byte[] abuf, int atype, InetAddress anip, int aport) {
		buf=abuf;
		type=atype;
		ip=anip;
		port=aport;
		// new sender --> do in background send
		// new receiver --> do in background receive
	}

	
	public void receiveMessage(){
		while(true) {
			//ascolta messaggi in arrivo
			//OCCHI CHE FORSE E' SBAGLIATO 
						
			Packet_UDP rec_pack = new UDP_packet(port);
			rec_pack.receive();
		}
	}
}
