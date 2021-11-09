package cs451;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class Task_send implements Runnable {
	private int length;
	private byte buf[];
	private int port;
	private int type; 				//I may send a message with content or it may simply be an acknowledgment message
	private InetAddress ip;
	private MyLogger logger;
	private Parser parser;
	
	public Task_send(byte[] buf, InetAddress ip, int port, MyLogger llogger, Parser parser) {
		this.buf=buf;
		this.type=type;
		this.ip=ip;
		this.port=port;
		logger=llogger;
		this.parser=parser;
	}
		
    public void run() {
    	UDP_packet send_pack = new UDP_packet(buf, ip, port, logger, parser); //if type == 0 --> acknowledgement packet 
		send_pack.send();
    }
}

