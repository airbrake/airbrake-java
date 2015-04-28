// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake.stacktrace;

public interface BacktraceLine {
	//Returns self for easy calling of the toXml/toString methods
	public BacktraceLine acceptLine(String line);
	public String toString();
	public String toXml();
}