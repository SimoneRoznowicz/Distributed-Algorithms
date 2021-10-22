package cs451;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class Task_send implements Runnable {
	private int length;
	private byte buf[];
	private int port;
	private int type; 				//I may send a message with content or it may simply be an acknowledgment message
	private InetAddress ip;
	
	public Task_send(byte[] buf, int type, InetAddress ip, int port) {
		this.buf=buf;
		this.type=type;
		this.ip=ip;
		this.port=port;
	}
		
	
	/*ProcessSender message;
    public Task_send(ProcessSender message) {
        this.message=message;
    }
	*/
    public void run() {
    	System.out.println("CIAOxxx");
    	/*try {
    		Thread.sleep(2000);
    	} catch (java.lang.InterruptedException e) {
    		e.printStackTrace();
    	}*/
    	UDP_packet send_pack = new UDP_packet(buf, type, ip, port); //if type == 0 --> acknowledgement packet 
		send_pack.send();
		//message.sendMessage();
    }
}

