package cs451;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.Scanner;
import java.text.DecimalFormat;
import java.lang.Thread;


public class Main {
/*
 * problema quando ribroadcasti il messaggio: il vector clock che viene mandato non e' quello del dispositivo originario ma quello dell'ultimo intermediario:
 * devi cambiare qualcosa quando in process manda con task_send e nel receive() dove ribroadcasti ci devi mettere il vector clock nel messaggio
 * */
    private static void handleSignal(MyLogger logger) {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        System.out.println("Writing output.");
        logger.writeOutput();
        //write/flush output file if necessary
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
    	List<List<Integer>> list = Collections.synchronizedList(new ArrayList<List<Integer>>(2));
    	List<Integer> newList = new ArrayList<Integer>(1);
    	list.add(newList);
    	Integer[] integers = new Integer[parser.hosts().size()];
    	Arrays.fill(integers, -1);
    	newList = Arrays.asList(integers);
    	//newList = new ArrayList<Integer>(integers);
    	list.add(newList);
    	//System.out.println("list ========= ", list);

    	
		try (BufferedReader br = new BufferedReader(new FileReader(parser.config()))) {
		    String line;
		    boolean isFirst=true;
		    while ((line = br.readLine()) != null) {
		    	if(isFirst) {
			    	scanner = new Scanner(line);
			    	list.get(0).add(scanner.nextInt());	//add num_messages 
			    	isFirst=false;
		    	}
		    	scanner = new Scanner(line);
		    	int idFile=scanner.nextInt();
		    	if(idFile==parser.myId()) {
		    		int i=0;
			    	while (scanner.hasNextInt()) {
			    		list.get(1).set(scanner.nextInt(), 0);
			    		i++;
			    	}
			    	break;
		    	}	
		    }
		}catch(java.io.IOException e) {
			e.printStackTrace();
		}
		//so I have just 2 two inner arrayLists: the first contains just the number of messages, 
		//the second contains the vector clock
		int num_mess_send = list.get(0).get(0);
		int myID = parser.myId();
		
        MyLogger logger = new MyLogger(parser, list);
        initSignalHandlers(logger);		
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
		
    	
    	URB urb = new URB(parser, list_payloads, logger);
		Thread thread1 = new Thread() {
		    public void run(){
		    	urb.receive();
		    }
		};
		thread1.start();
		Thread thread2 = new Thread() {
		    public void run(){
		    	urb.send();
		    }
		};
		thread2.start();
		//creare vettore del time, inviarlo come metadata con ogni messaggio,
		//nel receiver usare questa informazione ogni volta
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");

        System.out.println("Broadcasting and delivering messages...\n");

        System.out.println("FINISHED!!!");
        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
