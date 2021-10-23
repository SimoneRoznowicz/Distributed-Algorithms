package cs451;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.io.FileWriter;

public class MyLogger {
	ArrayList<String> logs = new ArrayList<String>();
	
	private String outputPath;
	
	public MyLogger(String outputPath) {
		this.outputPath=outputPath;
	}
	
	public void add(String log) {
		logs.add(log);
	}
	
	public void writeOutput() {
		System.out.print("NUMBER OF LOGS == " + this.logs.size());
		try(BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputPath))) {
			for(String log : logs) {
				fileWriter.write(log);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		String secondPath = "../example/output/fileout.txt";
		try(BufferedWriter fileWriter = new BufferedWriter(new FileWriter(secondPath))) {
			for(String log : logs) {
				fileWriter.write(log);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
