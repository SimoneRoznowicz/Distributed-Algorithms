package cs451;
import java.util.concurrent.TimeUnit;

public class Task_send implements Runnable {
	ProcessSender message;
    public Task_send(ProcessSender message) {
        this.message=message;
    }
 
    public void run() {
    	if(message==null) System.out.println("Send NULL message");
		message.sendMessage();
    }
}

