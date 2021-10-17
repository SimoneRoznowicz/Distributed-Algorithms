package cs451;
/*
 * Goal of the class:
 * - send a message
 * - receive acknowledgement message
 * - resend a message if not arrived 
 */

public class Process {
	
	private int length;
	private byte buf[];
	private DatagramPacket received_packet;
	private DatagramPacket sent_packet;
	private int port;
	private int type; 				//I may send a message with content or it may simply be an acknowledgment message
	private InetAddress ip;
	private Queue<ProcessSender> mess_queue;
	private int number_threads;
	
	public Process(byte[] abuf, int atype, InetAddress anip, int aport) {
		buf=abuf;
		type=atype;
		ip=anip;
		port=aport;
		// new sender --> do in background send
		// new receiver --> do in background receive
	}
	
	public void sendReceiveAll() {
		//mess_queue.size()>=10 ? number_threads=mess_queue.size()/10 : number_threads=1;
		number_threads=3;
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(number_threads);
        while(!mess_queue.isEmpty()) {
            Task task = new Task(mess_queue.poll());
            System.out.println("Created : " + task.getName());
            executor.execute(task);
        }
        executor.shutdown();
	}
}











