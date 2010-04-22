package org.gel.mauve;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class OptionsBuilder {
	
	private Options options;
	
	public OptionsBuilder(){
		options = new Options();
	}
	
	public void addBoolean(String opt, String desc){
		options.addOption(new Option(opt, desc));
	}
	
	@SuppressWarnings("static-access")
	public void addArgument(String argName, String desc, String opt){
		options.addOption(OptionBuilder.withArgName(argName)
		  .hasArg()
		  .withDescription(desc)
		  .create(opt));
	}
	
	public Options getOptions(){
		return options;
	}
	
}
