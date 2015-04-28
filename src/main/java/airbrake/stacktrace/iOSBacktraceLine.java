// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake.stacktrace;

import static java.text.MessageFormat.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import airbrake.NoticeXml;

public class iOSBacktraceLine implements BacktraceLine {

	private String crashLocation;
	private String memAddress;
	private String command;
	private String offset;
	
	private static Pattern p = Pattern.compile("^[0-9]+[ ]*(?<crashlocation>[A-Za-z_.?]+)[ ]+(?<memaddress>[^ ]+)[ ]+(?<command>(?:.(?!\\+ [0-9]+$))+) \\+ (?<offset>[0-9]+)$");
	
	public iOSBacktraceLine(String line) {
		this.acceptLine(line);
	}

	public BacktraceLine acceptLine(String line) {
		Matcher m = p.matcher(line);
		Boolean b = m.matches();
		if(b && m.groupCount() == 4) {
			this.crashLocation = m.group(1);
			this.memAddress = m.group(2);
			this.command = m.group(3);
			this.offset = m.group(4);
		} else {
			//throw new Exception("Bad iOS stacktrace line to parse: " + line);
		}
		
		return this;
	}
	
	private String toBacktrace(final String crashLocation, final String memAddress, final String command, final String offset) {
		return format("{0} {1} {2} + {3}", crashLocation, memAddress, command, offset);
	}

	@Override
	public String toString() {
		return toBacktrace(crashLocation, memAddress, command, offset);
	}

	@Override
	public String toXml() {
		//return format("<line method=\"{0}.{1}\" file=\"{2}\" number=\"{3}\"/>", className, methodName, crashLocation, lineNumber);
		return format("<line method=\"{0} {1} + {2}\" file=\"{3}\" number=\"\"/>", memAddress, command, offset, crashLocation);
	}
}