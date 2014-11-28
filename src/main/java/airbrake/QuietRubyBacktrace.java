// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import ch.qos.logback.classic.spi.IThrowableProxy;

import java.util.*;

public class QuietRubyBacktrace extends RubyBacktrace {

	public QuietRubyBacktrace() {
		super();
	}

	protected QuietRubyBacktrace(final List<String> backtrace) {
		super(backtrace);
	}

	public QuietRubyBacktrace(final IThrowableProxy throwable) {
		super(throwable);
	}

	@Override
	protected void ignore() {
		ignoreCocoon();
		ignoreMozilla();
		ignoreSpringSecurity();
		ignoreMortbayJetty();
		ignoreJunit();
		ignoreEclipse();
		ignoreNoise();
	}

	@Override
	public Backtrace newBacktrace(final IThrowableProxy throwable) {
		return new QuietRubyBacktrace(throwable);
	}

}
