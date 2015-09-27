package cs425.mp2;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class serverMain {
	/**
	 * Formats commandline inputs and flags
	 */
	private static FD FormatCommandLineInputs(String [] args) {
		Options op=createOptions();
		CommandLineParser parser=new DefaultParser();
		CommandLine line=null;
		try {
			line=parser.parse(op,args);	
		} catch (ParseException e) {
			printHelp(op);
			e.printStackTrace();
		}
		if (!line.hasOption("i")) {	
			int port = Integer.parseInt(line.getOptionValue("port"));
			return new FDIntroducer(port);
		}
		else {
			int port = Integer.parseInt(line.getOptionValue("port"));
			String iadd=line.getOptionValues("i")[0];  
			int iport=Integer.parseInt(line.getOptionValues("i")[1]);
			System.out.println(port);
			System.out.println(iadd);
			System.out.println(iport);
			return new FD(port,iadd,iport);
		}
	}

	/** Creates the required options to look for in command line arguments
	 * @return Options object
	 */
	private static Options createOptions() {
		Option port = Option.builder("port").argName("serverPort").hasArg().desc("Port to run faliure detector server")
				.required().build();
		Option i = Option.builder("i").desc("Describes the address and port of introducer").numberOfArgs(2).build();
		Options op=new Options();
		op.addOption(port);
		op.addOption(i);
		return op;
	}

	/** print helper for usage
	 * @param op options
	 */
	private static void printHelp(Options op) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("faliureDetector",op);
	}
	public static void main(String [] args) throws IOException {
		FD faliureDetector = FormatCommandLineInputs(args);
		faliureDetector.runFD();
	}
}
