package cs451;
import java.net.DatagramPacket; 
import java.net.InetAddress;

public class ProcessReceiver {
	private int port;
	
	public ProcessReceiver(int sourcePort) {
		port=sourcePort;
	}
	
	public void receiveMessage() {
		while(true) {
			//keeps listening to messages coming through the specified port
			//UDP_packet rec_pack = new UDP_packet(port);
			//rec_pack.receive();
		}
	}
}
