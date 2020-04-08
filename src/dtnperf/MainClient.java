package dtnperf;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dtnperf.client.Client;
import dtnperf.client.CongestionControl;
import dtnperf.client.DataMode;
import dtnperf.client.DataUnit;
import dtnperf.client.Mode;
import dtnperf.client.RateCongestionControl;
import dtnperf.client.RateUnit;
import dtnperf.client.TimeMode;
import dtnperf.client.WindowCongestionControl;
import dtnperf.event.BundleReceivedListener;
import dtnperf.event.BundleSentListener;
import it.unibo.dtn.JAL.Bundle;
import it.unibo.dtn.JAL.BundleDeliveryOption;
import it.unibo.dtn.JAL.BundleEID;
import it.unibo.dtn.JAL.BundlePayloadLocation;
import it.unibo.dtn.JAL.JALEngine;

public class MainClient {

	private static BundleEID destination;
	private static Mode mode;
	private static Optional<BundleEID> replyTo = Optional.empty();
	private static Optional<CongestionControl> congestionControl = Optional.empty();
	private static Optional<Integer> payloadSize = Optional.empty();
	private static Optional<DataUnit> payloadUnit = Optional.empty();
	private static Optional<Integer> timeToLife = Optional.empty();
	private static final Set<BundleDeliveryOption> deliveryOptions = new HashSet<>();
	private static Optional<BundlePayloadLocation> location = Optional.empty();

	public static void main(String[] args) throws Exception {
		parse(args);

		Client client = new Client(destination, mode);
		client.addBundleSentListener(new BundleSentListener() {
			@Override
			public void bundleSentEvent(Bundle bundleSent) {
				System.out.println("Sent bundle of size " + bundleSent.getData().length + " to " + bundleSent.getDestination());
			}
		});
		client.addBundleReceivedListener(new BundleReceivedListener() {
			@Override
			public void bundleReceivedEvent(Bundle bundle) {
				System.out.println("Received bundle of size " + bundle.getData().length);
			}
		});

		if (location.isPresent()) {
			client.setBundleLocation(location.get());
		}
		
		if (payloadSize.isPresent()) {
			if (payloadUnit.isPresent()) {
				client.setPayloadSize(payloadSize.get(), payloadUnit.get());
			} else {
				client.setPayloadSize(payloadSize.get());
			}
		}

		if (congestionControl.isPresent()) {
			client.setCongestionControl(congestionControl.get());
		}

		if (replyTo.isPresent()) {
			client.setReplyTo(replyTo.get());
		}

		if (timeToLife.isPresent()) {
			client.setTimeToLive(timeToLife.get());
		}

		Thread clientThread = client.start();

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

		OptionGroup modeOption = new OptionGroup();
		Option dataModeOption = Option.builder("D").hasArg().required(false).desc("Data mode").longOpt("data").build();
		modeOption.addOption(dataModeOption);
		Option timeModeOption = Option.builder("T").hasArg().required(false).desc("Time mode").longOpt("time").build();
		modeOption.addOption(timeModeOption);
		modeOption.setRequired(true);
		options.addOptionGroup(modeOption);

		OptionGroup congestionControlOption = new OptionGroup();
		Option windowModeOption = Option.builder("W").hasArg().required(false).desc("Window mode").longOpt("window").build();
		congestionControlOption.addOption(windowModeOption);
		Option rateModeOption = Option.builder("R").hasArg().required(false).desc("Rate mode").longOpt("rate").build();
		congestionControlOption.addOption(rateModeOption);
		congestionControlOption.setRequired(false);
		options.addOptionGroup(congestionControlOption);

		Option payloadOption = Option.builder("P").hasArg().required(false).desc("Payload size").longOpt("payload").build();
		options.addOption(payloadOption);
		Option lifeTimeOption = Option.builder("l").hasArg().required(false).desc("Bundle lifetime. Default is 60.").longOpt("lifetime").build();
		options.addOption(lifeTimeOption);
		Option custodyOption = Option.builder("C").hasArg(false).required(false).desc("Request of custody transfer (and of \"custody accepted\" status reports as well).").build();
		options.addOption(custodyOption);
		Option forwardOption = Option.builder("f").hasArg(false).required(false).desc("Request of custody transfer (and of \"forward accepted\" status reports as well).").build();
		options.addOption(forwardOption);
		Option receivedOption = Option.builder("r").hasArg(false).required(false).desc("Request of custody transfer (and of \"received accepted\" status reports as well).").build();
		options.addOption(receivedOption);
		Option deletedOption = Option.builder().hasArg(false).required(false).desc("Request of custody transfer (and of \"deleted accepted\" status reports as well).").longOpt("del").build();
		options.addOption(deletedOption);
		Option helpOption = Option.builder("h").hasArg(false).required(false).desc("This help.").longOpt("help").build();
		options.addOption(helpOption);
		Option memoryOption = Option.builder("M").hasArg(false).required(false).desc("Store the bundle into memory instead of file (if payload < 50KB).").longOpt("memory").build();
		options.addOption(memoryOption);

		CommandLineParser commandLineParser = new DefaultParser();
		CommandLine commandLine = commandLineParser.parse(options, args);

		if (commandLine.hasOption("h")) {
			HelpFormatter help = new HelpFormatter();
			help.printHelp("MainClient", options);
			System.exit(0);
		}

		if (commandLine.hasOption("M")) {
			location = Optional.of(BundlePayloadLocation.Memory);
		}

		if (commandLine.hasOption("C")) {
			deliveryOptions.add(BundleDeliveryOption.Custody);
		}

		if (commandLine.hasOption("f")) {
			deliveryOptions.add(BundleDeliveryOption.ForwardReceipt);
		}

		if (commandLine.hasOption("r")) {
			deliveryOptions.add(BundleDeliveryOption.ReceiveReceipt);
		}

		if (Arrays.asList(args).contains("--del")) {
			deliveryOptions.add(BundleDeliveryOption.DeleteReceipt);
		}

		if (!commandLine.hasOption("D") && !commandLine.hasOption("T")) {
			throw new ParseException("Error, you must to choose between Time and Data mode.");
		} else if (commandLine.hasOption("D") && commandLine.hasOption("T")) {
			throw new ParseException("Error, you must choose between Time and Data mode, but not both.");
		}

		destination = BundleEID.of(commandLine.getOptionValue("d"));

		if (commandLine.hasOption("m")) {
			replyTo = Optional.of(BundleEID.of(commandLine.getOptionValue("m").trim()));
		}

		if (commandLine.hasOption("l")) {
			timeToLife = Optional.of(Integer.parseInt(commandLine.getOptionValue("l").trim()));
		}

		if (commandLine.hasOption("D")) {
			String dataString = commandLine.getOptionValue("D").trim();
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

		if (commandLine.hasOption("P")) {
			String payloadString = commandLine.getOptionValue("P").trim();
			String numberString = "";
			boolean parsingNumber = true;
			DataUnit unit = null;
			for (char c : payloadString.toCharArray()) {
				if (c >= '0' && c <= '9' && parsingNumber)
					numberString = numberString + c;
				else {
					parsingNumber = false;
					if (unit != null)
						throw new IllegalArgumentException("Error on parsing data in payload");
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
						throw new IllegalArgumentException("Error on parsing data in payload");
					}
				}
			}
			int number = Integer.parseInt(numberString);
			payloadSize = Optional.of(number);
			if (unit != null)
				payloadUnit = Optional.of(unit);
		}

		if (commandLine.hasOption("T")) {
			int time = Integer.parseInt(commandLine.getOptionValue("T").trim());
			mode = new TimeMode(time, ChronoUnit.SECONDS);
		}

		if (commandLine.hasOption("W")) {
			int window = Integer.parseInt(commandLine.getOptionValue("W").trim());
			congestionControl = Optional.of(new WindowCongestionControl(window));
		}

		if (commandLine.hasOption("R")) {
			String dataString = commandLine.getOptionValue("R").trim();
			String numberString = "";
			boolean parsingNumber = true;
			RateUnit rateUnit = null;
			for (char c : dataString.toCharArray()) {
				if (c >= '0' && c <= '9' && parsingNumber)
					numberString = numberString + c;
				else {
					parsingNumber = false;
					if (rateUnit != null)
						throw new IllegalArgumentException("Error on parsing data in rate");
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
						throw new IllegalArgumentException("Error on parsing data in rate");
					}
				}
			}
			int number = Integer.parseInt(numberString);
			congestionControl = Optional.of(new RateCongestionControl(number, rateUnit));
		}

	}

}
