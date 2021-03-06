package dtnperf;

import java.util.ArrayList;

import dtnperf.gui.Gui;

public class Main {

	private static final String CLIENTSTRING = "--client";
	private static final String SERVERSTRING = "--server";
	private static final String GUISTRING = "--gui";
	
	public static void main(String[] args) throws Exception {
		System.out.println("JDTNperf 1.0");
		
		ArrayList<String> arguments = new ArrayList<>(args.length - 1);
		for (String arg : args) {
			arguments.add(arg);
		}
		
		arguments.remove(0); // Remove the first element
		
		String[] newArguments = arguments.toArray(new String[arguments.size()]);
		
		switch (args[0]) {
		case CLIENTSTRING:
			MainClient.main(newArguments);
			break;

		case SERVERSTRING:
			MainServer.main(newArguments);
			break;
			
		case GUISTRING:
			Gui.main(args);
			break;
			
		default:
			System.out.println("Mode not found.");
			break;
		}
		
	}

}
