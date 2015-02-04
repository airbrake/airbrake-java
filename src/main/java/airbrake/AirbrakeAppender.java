// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.AppenderBase;

public class AirbrakeAppender extends AppenderBase<ILoggingEvent> {

	private final AirbrakeNotifier airbrakeNotifier = new AirbrakeNotifier();

	private String apiKey;

	private String env;

	private boolean enabled;

	private Backtrace backtrace = new Backtrace();

	public AirbrakeAppender(final String apiKey) {
		setApi_key(apiKey);
	}

	public AirbrakeAppender(final String apiKey, final Backtrace backtrace) {
		setApi_key(apiKey);
		setBacktrace(backtrace);
	}

	@Override
	protected void append(final ILoggingEvent loggingEvent) {
		if (!enabled)
			return;

		if (thereIsThrowableIn(loggingEvent)) {
			notifyThrowableIn(loggingEvent);
		}
	}



	public AirbrakeNotice newNoticeFor(final IThrowableProxy throwable) {
		return new AirbrakeNoticeBuilderUsingFilteredSystemProperties(apiKey,
				backtrace, throwable, env).newNotice();
	}

	private int notifyThrowableIn(final ILoggingEvent loggingEvent) {
		return airbrakeNotifier.notify(newNoticeFor(throwable(loggingEvent)));
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
	private boolean thereIsThrowableIn(final ILoggingEvent loggingEvent) {
		return  loggingEvent.getThrowableProxy() != null;
	}

	/**
	 * Get the throwable information contained in a {@link ILoggingEvent}.
	 * Returns the Throwable passed to the logger or the message if it's a
	 * Throwable.
	 * @param loggingEvent
	 * @return The Throwable contained in the {@link ILoggingEvent} or null if there is none.
	 */
	private IThrowableProxy throwable(final ILoggingEvent loggingEvent) {
		return loggingEvent.getThrowableProxy();

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
