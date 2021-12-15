package cs451;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;


public class URB {
	List<Host> hosts;
	List<String> list_payloads;
	Parser parser;
	MyLogger logger;
	
	public URB(Parser parser, List<String> list_payload, MyLogger logger) {
		this.parser=parser;
		this.list_payloads = list_payload;
		this.logger=logger;
	}
	public void send() {
		for (Host host: parser.hosts()) {
			if (host.getId() != parser.myId()) {
				Thread thread = new Thread() {
				    public void run(){
				    	try {
				    		System.out.println("SEND IN URB=========================================");
							Process process = new Process(list_payloads, InetAddress.getByName(host.getIp()), host.getPort(), parser.myId(), host.getId(), parser.output(), logger, parser);
							process.sendAll();
				    	} catch(UnknownHostException e) {
				    		e.printStackTrace();
				    	}
				    }
				};
				thread.start();
			}
	        System.out.println(host.getId());
	        System.out.println("Human-readable IP: " + host.getIp());
	        System.out.println("Human-readable Port: " + host.getPort());
	        System.out.println();
		}
	}
	public void receive() {
		for (Host host: parser.hosts()) {
			if (host.getId() == parser.myId()) {
				Thread thread = new Thread() {
				    public void run(){
				    	try {
							Process process = new Process(list_payloads, InetAddress.getByName(host.getIp()), host.getPort(), parser.myId(), host.getId(), parser.output(), logger, parser);
							process.receiveAll();
				    	} catch(UnknownHostException e) {
				    		e.printStackTrace();
				    	}
				    }
				};
				thread.start();
			}
	        System.out.println(host.getId());
	        System.out.println("Human-readable IP: " + host.getIp());
	        System.out.println("Human-readable Port: " + host.getPort());
	        System.out.println();
		}
	}
}
