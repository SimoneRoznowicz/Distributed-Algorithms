package cs451;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.text.DecimalFormat;
import java.lang.Thread;

public class Main {

    private static void handleSignal(MyLogger logger) {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        logger.writeOutput();
        //write/flush output file if necessary
        System.out.println("Writing output.");
    }

    private static void initSignalHandlers(MyLogger logger) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal(logger);
            }
        });
    }

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        Parser parser = new Parser(args);
        parser.parse();

        FileInputStream file = null;
        try {
        	file = new FileInputStream(parser.config());
		    System.out.println("file config path :: " + parser.config());
        } catch(FileNotFoundException e) {
        	e.printStackTrace();
        }
        Scanner scanner = new Scanner(file);
		List<Integer> list= new ArrayList<Integer>();
		while (scanner.hasNextInt()) {
		    list.add(scanner.nextInt());
		}
		int num_mess_send = list.get(0);
		int myID = parser.myId();
		
        MyLogger logger = new MyLogger(parser, num_mess_send);
        initSignalHandlers(logger);		
        //Thread.sleep(2000);
        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");
        
        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        
        
		//list of payloads (== list of numbers in ascending order)
		List<String> list_payloads = new ArrayList<String> (num_mess_send);
		for (int i=0; i<num_mess_send; i++) {
			int b = i+1;
			String a = myID + " " + myID+ " " + b;	//example: a == " 1 1 3" where 1 is the ID of the sender, 1 the Id of the original sender, 3 is the 3rd message sent by the process
			list_payloads.add(a);		//index of payloads goes from 1...n
		}
		
		System.out.println("list_payloads.size() " + list_payloads.size());		
		System.out.println("number of messages to be sent: " + num_mess_send);
        long start = System.currentTimeMillis();
        for (Host host: parser.hosts()) {
        	System.out.println("host: " + host.getId());
        }
    	System.out.println("*********-------------------");
        for (Host host: parser.hosts()) {
        	//AVVIA UNA CLASSE ESEGUIBILE CHE INVII TUTTI I MESSAGGI
			System.out.println("myID == " + myID);
			System.out.println("host.getId() == " + host.getId()+ "\n\n");
			if (host.getId() == myID) {
				Thread thread = new Thread() {
				    public void run(){
				    	try {
							Process process = new Process(list_payloads, InetAddress.getByName(host.getIp()), host.getPort(), myID, host.getId(), parser.output(), logger, parser);
							process.receiveAll();
				    	} catch(UnknownHostException e) {
				    		e.printStackTrace();
				    	}
				    }
				};
				thread.start();
			}
			if (host.getId() != myID) {
				Thread thread = new Thread() {
				    public void run(){
				    	try {
							Process process = new Process(list_payloads, InetAddress.getByName(host.getIp()), host.getPort(), myID, host.getId(), parser.output(), logger, parser);
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
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");

        System.out.println("Broadcasting and delivering messages...\n");
        long end = System.currentTimeMillis() - start; 
        float seconds = end / 1000.0f; 
        System.out.println("DELTA TIME: " + seconds);

        System.out.println("FINISHED!!!");
        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
