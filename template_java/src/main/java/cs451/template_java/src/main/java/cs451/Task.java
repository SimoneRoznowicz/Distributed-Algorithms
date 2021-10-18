package cs451;
import java.util.concurrent.TimeUnit;
import java.util.Queue;


public class Task implements Runnable {
 
	ProcessSender message;
    public Task(ProcessSender message) {
        this.message=message;
    }
 
    public void run() {
        try {
        	if(message==null) System.out.println("Send NULL message");
    		message.send();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

