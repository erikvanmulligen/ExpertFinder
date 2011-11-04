package AffiliationResolver;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class testAffiliationResolver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final CommandLineParser cmdLineGnuParser = new GnuParser();
		final Options gnuOptions = constructGnuOptions();
		CommandLine commandLine;
		String name = null;

		/*
		 * extract the -name argument from the args
		 */
		try{
			commandLine = cmdLineGnuParser.parse(gnuOptions, args);
			if ( commandLine.hasOption("n") ){
				name = commandLine.getOptionValue("n");
				System.out.println( "You entered the name " + name );
			}
		}
		catch (ParseException parseException){  // checked exception
			System.err.println(
					"Encountered exception while parsing using GnuParser:\n"
					+ parseException.getMessage() );
		}
		
		
		if ( name != null ){
			Affiliation affiliation = new Affiliation();
			System.out.println( affiliation.find(name) );
		}

	}
	
	/**
	 * Construct and provide GNU-compatible Options.
	 * 
	 * @return Options expected from command-line of GNU form.
	 */
	public static Options constructGnuOptions(){
		final Options gnuOptions = new Options();
		gnuOptions.addOption("n", "name", true, "Name to use for affiliation" );
		return gnuOptions;
	}

}
