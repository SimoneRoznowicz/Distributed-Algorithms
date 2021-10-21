package cs451;
import java.util.concurrent.TimeUnit;

public class Task_receive implements Runnable {
	ProcessReceiver receiver;
    public Task_receive(ProcessReceiver receiver) {
        this.receiver=receiver;
    }
 
    public void run() {
		receiver.receiveMessage();
    }
}

