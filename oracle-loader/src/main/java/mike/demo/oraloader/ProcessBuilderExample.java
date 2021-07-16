package mike.demo.process;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import mike.commons.utilities.Print;
import mike.commons.utilities.Utils;
import mike.demo.Runner;

public class ProcessBuilderRunner implements Runner {

	private static final List<String> mathematicians = new ArrayList<>();
	
	static {
		//                  1-3---------13---
		mathematicians.add("08Pythagore");
		mathematicians.add("02Pierre    De Fermat");
		mathematicians.add("04Isaac     Newton");
		mathematicians.add("05Rene      Descartes");
		mathematicians.add("06Alan      Turing");
		mathematicians.add("03Albert    Einstein");
		mathematicians.add("07Leonardo  Fibonacci");
		mathematicians.add("09Blaise    Pascal");
		mathematicians.add("10Euclide");
		mathematicians.add("01Henri     Poincare");
	}
	
	@Override
	public void go() throws Exception {
		
		/*
		this.runSimpleCommand("EchoHelloWorld", "echo", "Hello World !");
		this.runSimpleCommand("ListCurrentDirectory", "ls", "-al");
		this.runSimpleCommand("CommandError", "ls", "-y");
		*/
		
		this.runSimpleCommand("WhichSort", "which", "sort");
		//this.runUnixSortMathematicians("SortByFirstName", "sort", "-k1.3,1.13"); // this does work on Cmder
		this.runUnixSortMathematicians("SortByFirstName", "sort");
	}
	
	private void runSimpleCommand(String label, String... commands) throws IOException, InterruptedException {
		
		Print.info("***** Process %s started", label);
		
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.redirectErrorStream(true); // Redirect errors to the "stdin" (unix: 2>&1) 
		
		Process process = pb.start();
		
		try ( Scanner sc = new Scanner(process.getInputStream()) ) {
			while ( sc.hasNextLine() )
				Print.info(sc.nextLine());
		}
		
		int rc = process.waitFor();
		
		Print.info("***** Process %s completed (rc=%d)", label, rc);
	}
	
	private void runUnixSortMathematicians(String label, String... commands) throws IOException, InterruptedException {
		
		Print.info("***** Process %s started", label);
		
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.redirectErrorStream(true); // Redirect errors to the "stdin" (unix: 2>&1)
		
		Process process = pb.start();

		/* Inject lines in the sort command */
		try ( OutputStream sortInput = process.getOutputStream() ) {
			for (String m : mathematicians) {
				sortInput.write( (m + Utils.LINE_SEP).getBytes());
			}
			
			sortInput.flush();
		}
		
		/* Get the sort output */
		try ( Scanner sc = new Scanner(process.getInputStream()) ) {
			while ( sc.hasNextLine() )
				Print.info(sc.nextLine());
		}
		
		int rc = process.waitFor();
		
		Print.info("***** Process %s completed (rc=%d)", label, rc);
	}
}
