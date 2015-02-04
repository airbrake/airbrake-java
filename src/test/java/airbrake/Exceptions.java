// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;

public class Exceptions {
	
	protected static final String ERROR_MESSAGE = "undefined method `password' for nil:NilClass";

	public static IThrowableProxy newException(final String errorMessage) {

		final String string = null;
		try {
			string.toString();
		} catch (final Exception e) {
			return new ThrowableProxy(new RuntimeException(errorMessage, e));
		}
		return new ThrowableProxy(new RuntimeException(errorMessage));
	}

}
