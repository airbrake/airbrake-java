package io.airbrake;

import org.apache.log4j.*;
import org.apache.log4j.spi.*;

public class AirbrakeAppender extends AppenderSkeleton {

	private final Airbrake airbrake = new Airbrake();

	private boolean enabled;

	public AirbrakeAppender() {
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

	private void notifyThrowableIn(final LoggingEvent loggingEvent) {
		airbrake.notify(throwable(loggingEvent));
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	public void setApiKey(String apiKey) {
		airbrake.setApiKey(apiKey);
	}

	public void setAppVersion(String appVersion) {
		airbrake.setAppVersion(appVersion);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public void setEnvName(String envName) {
		airbrake.setEnvName(envName);
	}

	public void setProjectId(String projectId) {
		airbrake.setProjectId(projectId);
	}

	public void setUrl(String url) {
		airbrake.setUrl(url);
	}
	
	private boolean thereIsThrowableIn(final LoggingEvent loggingEvent) {
		return loggingEvent.getMessage() != null;
	}

	private Throwable throwable(final LoggingEvent loggingEvent) {
		Object message = loggingEvent.getMessage();
		if (message instanceof Throwable)
			return (Throwable) loggingEvent.getMessage();
		return null;
	}

}
