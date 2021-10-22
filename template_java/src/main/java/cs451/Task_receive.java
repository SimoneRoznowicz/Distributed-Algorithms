package cs451;
import java.util.concurrent.TimeUnit;

public class Task_receive implements Runnable {
	private UDP_packet rec_pack;
	
    public Task_receive(UDP_packet rec_pack) {
        this.rec_pack=rec_pack;
    }
 
    public void run() {
    	//UDP_packet rec_pack = new UDP_packet(port);
		rec_pack.receive();
    	//receiver.receiveMessage();
    }
}

