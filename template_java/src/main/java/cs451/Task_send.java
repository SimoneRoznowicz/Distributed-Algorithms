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
	
	public Task_send(byte[] buf, int type, InetAddress ip, int port, MyLogger llogger) {
		this.buf=buf;
		this.type=type;
		this.ip=ip;
		this.port=port;
		logger=llogger;
	}
		
	
	/*ProcessSender message;
    public Task_send(ProcessSender message) {
        this.message=message;
    }
	*/
    public void run() {
    	UDP_packet send_pack = new UDP_packet(buf, type, ip, port, logger); //if type == 0 --> acknowledgement packet 
		send_pack.send();
		/*try {
			Thread.sleep(20);
		} catch (java.lang.InterruptedException e) {
			e.printStackTrace();
		}*/
		
    }
}

