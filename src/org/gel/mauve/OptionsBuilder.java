package org.gel.mauve;

import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

public class OptionsBuilder {
	
	private Options options;
	
	public OptionsBuilder(){
		options = new Options();
	}
	
	public Option addBoolean(String opt, String desc){
		Option option = new Option(opt, desc);
		options.addOption(option);
		return option;
	}
	
	@SuppressWarnings("static-access")
	public Option addArgument(String argName, String desc, String opt, boolean required){
		Option option = OptionBuilder.withArgName(argName)
									   .hasArg()
									   .isRequired(required)
									   .withDescription(desc)
									   .create(opt);
		options.addOption(option);
		return option;
	}
	
	public Options getOptions(){
		return options;
	}
	
	public OptionGroup addMutExclOptions(Option a, Option b){
		OptionGroup grp = new OptionGroup();
		grp.addOption(a);
		grp.addOption(b);
		options.addOptionGroup(grp);
		return grp;
	}
	
	public OptionGroup addOptionGroup(OptionGroup grp){
		options.addOptionGroup(grp);
		return grp;
	}
	
	public static CommandLine getCmdLine(Options opts, String[] args){
		CommandLineParser parser = new GnuParser();
		try {
			return parser.parse(opts, args);
			
		} catch (UnrecognizedOptionException e) { 
			String opt = e.getOption();
			System.err.println("Unrecognized option: " + opt);
			System.exit(-1);
		} catch (MissingArgumentException e){
			Option opt = e.getOption();
			System.err.println("Error: " + opt.getOpt() + " not specified.");
			System.exit(-1);
		} catch (MissingOptionException e){
			Iterator it = e.getMissingOptions().iterator();
			while (it.hasNext()){
				System.err.println("Missing required option: " + it.next());
			}
			System.exit(-1);
		} catch (ParseException e){
			System.err.println("Error parsing arguments. Reason: " + e.getMessage());
			System.exit(-1);
		} 
		return null;
	}
}
