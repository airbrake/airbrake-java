// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import org.apache.log4j.*;
import org.apache.log4j.spi.*;

public class AirbrakeAppender extends AppenderSkeleton {
	
	private final AirbrakeNotifier airbrakeNotifier = new AirbrakeNotifier();

	private String apiKey;

	private String env;

	private boolean enabled;

	private Backtrace backtrace = new QuietRubyBacktrace();

	private String host;

	private String protocol;
	
	public AirbrakeAppender() {
		setThreshold(Level.ERROR);
	}

	public AirbrakeAppender(final String apiKey) {
		setApi_key(apiKey);
	}

	public AirbrakeAppender(final String apiKey, final Backtrace backtrace) {
		setApi_key(apiKey);
		setBacktrace(backtrace);
	}

	@Override
	protected void append(final LoggingEvent loggingEvent) {
		if (!enabled) return;

		if (thereIsThrowableIn(loggingEvent)) {
			notifyThrowableIn(loggingEvent);
		}
	}

	@Override
	public void close() {}

	public AirbrakeNotice newNoticeFor(final Throwable throwable) {
		return new AirbrakeNoticeBuilderUsingFilteredSystemProperties(apiKey, backtrace, throwable, env, host, protocol).newNotice();
	}

	private int notifyThrowableIn(final LoggingEvent loggingEvent) {
		return airbrakeNotifier.notify(newNoticeFor(throwable(loggingEvent)));
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	public void setApi_key(final String apiKey) {
		this.apiKey = apiKey;
	}

	public void setBacktrace(final Backtrace backtrace) {
		this.backtrace = backtrace;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public void setEnv(final String env) {
		this.env = env;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setProtocol(String protocol) {
	    this.protocol = protocol;
	}
	
	private boolean thereIsThrowableIn(final LoggingEvent loggingEvent) {
		return loggingEvent.getThrowableInformation() != null;
	}

	private Throwable throwable(final LoggingEvent loggingEvent) {
		return loggingEvent.getThrowableInformation().getThrowable();
	}
}
