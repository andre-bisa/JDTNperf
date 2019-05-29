package dtnperf;


import java.time.temporal.ChronoUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dtnperf.client.Client;
import dtnperf.client.ClientCongestionControl;
import dtnperf.client.ClientCongestionControlRate;
import dtnperf.client.ClientCongestionControlWindow;
import dtnperf.client.RateUnit;
import dtnperf.client.modes.DataMode;
import dtnperf.client.modes.DataUnit;
import dtnperf.client.modes.Mode;
import dtnperf.client.modes.TimeMode;
import it.unibo.dtn.JAL.BundleEID;
import it.unibo.dtn.JAL.JALEngine;

public class MainClient {

	private static BundleEID destination;
	private static BundleEID replyTo = BundleEID.NoneEndpoint;
	private static ClientCongestionControl congestionControl = new ClientCongestionControlWindow(1);
	private static Mode mode;
	private static int payloadSize = 10;
	private static DataUnit payloadUnit = DataUnit.KILOBYTES;
	
	public static void main(String[] args) throws Exception {
		parse(args);
		
		Client client = new Client(destination, replyTo, congestionControl, mode, payloadSize, payloadUnit);
		Thread clientThread = new Thread(client);
		
		clientThread.start();
		clientThread.join();
		
		System.out.println("\nTotal execution time = " + client.getTotalExecutionTime() / 10d + "s");
		System.out.println("Bundles sent = " + client.getSentBundles());
		System.out.println("Data sent = " + client.getDataSent()/1000 + "KB");
		
		System.out.println(client.getResultString());
		
		JALEngine.getInstance().destroy();
	}

	private static void parse(String[] args) throws ParseException {
		Options options = new Options();
		Option destinationOption = Option.builder("d").hasArg().required(true).desc("The description").longOpt("destination").build();
		options.addOption(destinationOption);
		Option replyToOption = Option.builder("m").hasArg().required(false).desc("The monitor").longOpt("monitor").build();
		options.addOption(replyToOption);
		Option dataModeOption = Option.builder("D").hasArg().required(false).desc("Data mode").longOpt("data").build();
		options.addOption(dataModeOption);
		Option timeModeOption = Option.builder("T").hasArg().required(false).desc("Time mode").longOpt("time").build();
		options.addOption(timeModeOption);
		Option windowModeOption = Option.builder("W").hasArg().required(false).desc("Window mode").longOpt("window").build();
		options.addOption(windowModeOption);
		Option rateModeOption = Option.builder("R").hasArg().required(false).desc("Rate mode").longOpt("rate").build();
		options.addOption(rateModeOption);
		
		CommandLineParser commandLineParser = new DefaultParser();
		CommandLine commandLine = commandLineParser.parse(options, args);
		
		if (!commandLine.hasOption("D") && !commandLine.hasOption("T")) {
			throw new ParseException("Error, you must to choose between Time and Data mode.");
		} else if (commandLine.hasOption("D") && commandLine.hasOption("T")) {
			throw new ParseException("Error, you must choose between Time and Data mode, but not both.");
		}
		
		destination = BundleEID.of(commandLine.getOptionValue("d"));
		
		if (commandLine.hasOption("m")) {
			replyTo = BundleEID.of(commandLine.getOptionValue("m"));
		}
		
		if (commandLine.hasOption("D")) {
			String dataString = commandLine.getOptionValue("D");
			String numberString = "";
			DataUnit unit = null;
			for (char c : dataString.toCharArray()) {
				if (c >= '0' && c <= '9')
					numberString = numberString + c;
				else {
					switch (c) {
					case 'k':
					case 'K':
						unit = DataUnit.KILOBYTES;
						break;
					case 'B':
						unit = DataUnit.BYTES;
						break;
					case 'M':
						unit = DataUnit.MEGABYTES;
						break;

					default:
						throw new IllegalArgumentException("Error on parsing data");
					}
				}
			}
			int number = Integer.parseInt(numberString);
			mode = new DataMode(number, unit);
		}
		
		if (commandLine.hasOption("T")) {
			int time = Integer.parseInt(commandLine.getOptionValue("T"));
			mode = new TimeMode(time, ChronoUnit.SECONDS);
		}
		
		if (commandLine.hasOption("W")) {
			int window = Integer.parseInt(commandLine.getOptionValue("W"));
			congestionControl = new ClientCongestionControlWindow(window);
		}
		
		if (commandLine.hasOption("R")) {
			String dataString = commandLine.getOptionValue("R");
			String numberString = "";
			RateUnit rateUnit = null;
			for (char c : dataString.toCharArray()) {
				if (c >= '0' && c <= '9')
					numberString = numberString + c;
				else {
					switch (c) {
					case 'k':
					case 'K':
						rateUnit = RateUnit.KILOBIT;
						break;
					case 'b':
						rateUnit = RateUnit.BUNDLE;
						break;
					case 'M':
						rateUnit = RateUnit.MEGABIT;
						break;

					default:
						throw new IllegalArgumentException("Error on parsing data");
					}
				}
			}
			int number = Integer.parseInt(numberString);
			congestionControl = new ClientCongestionControlRate(number, rateUnit);
		}
		
	}

}
