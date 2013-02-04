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

	private Backtrace backtrace = new Backtrace();

	public AirbrakeAppender() {
		setThreshold(Level.ERROR);
	}

	public AirbrakeAppender(final String apiKey) {
		setApi_key(apiKey);
		setThreshold(Level.ERROR);
	}

	public AirbrakeAppender(final String apiKey, final Backtrace backtrace) {
		setApi_key(apiKey);
		setBacktrace(backtrace);
		setThreshold(Level.ERROR);
	}

	@Override
	protected void append(final LoggingEvent loggingEvent) {
		if (!enabled)
			return;

		if (thereIsThrowableIn(loggingEvent)) {
			notifyThrowableIn(loggingEvent);
		}
	}

	@Override
	public void close() {
	}

	public AirbrakeNotice newNoticeFor(final Throwable throwable) {
		return new AirbrakeNoticeBuilderUsingFilteredSystemProperties(apiKey,
				backtrace, throwable, env).newNotice();
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

	public void setUrl(final String url) {
		airbrakeNotifier.setUrl(url);
	}

	/**
	 * Checks if the LoggingEvent contains a Throwable
	 * @param loggingEvent
	 * @return
	 */
	private boolean thereIsThrowableIn(final LoggingEvent loggingEvent) {
		return  loggingEvent.getThrowableInformation() != null ||
				loggingEvent.getMessage() instanceof Throwable;
	}

	/**
	 * Get the throwable information contained in a {@link LoggingEvent}.
	 * Returns the Throwable passed to the logger or the message if it's a
	 * Throwable.
	 * @param loggingEvent
	 * @return The Throwable contained in the {@link LoggingEvent} or null if there is none.
	 */
	private Throwable throwable(final LoggingEvent loggingEvent) {
		ThrowableInformation throwableInfo = loggingEvent.getThrowableInformation();
		if (throwableInfo != null)
			return throwableInfo.getThrowable();
		
		Object message = loggingEvent.getMessage();
		if (message instanceof Throwable)
			return (Throwable) message;
		
		return null;
	}

	protected String getApiKey() {
		return apiKey;
	}
	
	public Backtrace getBacktrace() {
		return backtrace;
	}

	protected String getEnv() {
		return env;
	}
}
