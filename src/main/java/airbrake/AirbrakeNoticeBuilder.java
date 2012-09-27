// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import static java.util.Arrays.*;

import java.util.*;
import java.util.Map.Entry;

public class AirbrakeNoticeBuilder {

	private String apiKey;

	private String projectRoot;

	private String environmentName;

	private String errorMessage;

	private Backtrace backtrace = new Backtrace(asList("backtrace is empty"));

	private final Map<String, Object> environment = new TreeMap<String, Object>();

	private Map<String, Object> request = new TreeMap<String, Object>();

	private Map<String, Object> session = new TreeMap<String, Object>();

	private final List<String> environmentFilters = new LinkedList<String>();

	private Backtrace backtraceBuilder = new Backtrace();

	private String errorClass;

	private boolean hasRequest = false;

	private String url;

	private String component;

	private String host;

	final static String DEFAULT_HOST = "api.airbrake.io";

	public AirbrakeNoticeBuilder(final String apiKey, final Backtrace backtraceBuilder, final Throwable throwable, final String env, final String host) {
		this(apiKey, throwable.getMessage(), env, host);
		this.backtraceBuilder = backtraceBuilder;
		errorClass(throwable);
		backtrace(throwable);
	}

	public AirbrakeNoticeBuilder(final String apiKey, final Backtrace backtraceBuilder, final Throwable throwable, final String env) {
		this(apiKey, backtraceBuilder, throwable, env, null);
	}

	public AirbrakeNoticeBuilder(final String apiKey, final String errorMessage) {
		this(apiKey, errorMessage, "test", null);
	}

	public AirbrakeNoticeBuilder(final String apiKey, final String errorMessage, final String env) {
		this(apiKey, errorMessage, env, null);
	}

	public AirbrakeNoticeBuilder(final String apiKey, final String errorMessage, final String env, String host) {
		apiKey(apiKey);
		errorMessage(errorMessage);
		env(env);
		host(host);
	}

	public AirbrakeNoticeBuilder(final String apiKey, final Throwable throwable) {
		this(apiKey, new Backtrace(), throwable, "test", null);
	}

	public AirbrakeNoticeBuilder(final String apiKey, final Throwable throwable, final String env) {
		this(apiKey, new Backtrace(), throwable, env, null);
	}

	public AirbrakeNoticeBuilder(final String apiKey, final Throwable throwable, final String projectRoot, final String env) {
		this(apiKey, new Backtrace(), throwable, env, null);
		projectRoot(projectRoot);
	}

	private void apiKey(final String apiKey) {
		if (notDefined(apiKey)) {
			error("The API key for the project this error is from (required). Get this from the project's page in airbrake.");
		}
		this.apiKey = apiKey;
	}

	protected void setRequest(String url, String component) {
		hasRequest = true;
		this.url = url;
		this.component = component;
	}

	protected void addSessionKey(String key, Object value) {
		session.put(key, value);
	}

	/** An array where each element is a line of the backtrace (required, but can be empty). */
	protected void backtrace(final Backtrace backtrace) {
		this.backtrace = backtrace;
	}

	private void backtrace(final Throwable throwable) {
		backtrace(backtraceBuilder.newBacktrace(throwable));
	}

	protected void ec2EnvironmentFilters() {
		environmentFilter("AWS_SECRET");
		environmentFilter("EC2_PRIVATE_KEY");
		environmentFilter("AWS_ACCESS");
		environmentFilter("EC2_CERT");
	}

	protected void projectRoot(final String projectRoot) {
		this.projectRoot = projectRoot;
	}

	private void env(final String env) {
		environmentName = env;
	}

        private void host(String host) {
    		if (notDefined(host)) {
    		    this.host = DEFAULT_HOST;
    		} else {
    		    this.host = host;
    		}
        }
	
	/** A hash of the environment data that existed when the error occurred (required, but can be empty). */
	protected void environment(final Map<String, Object> environment) {
		this.environment.putAll(environment);
	}

	protected void environment(Properties properties) {
		for (Entry<Object, Object> property : properties.entrySet()) {
			this.environment.put(property.getKey().toString(), property.getValue());
		}
	}

	public void environmentFilter(final String filter) {
		environmentFilters.add(filter);
	}

	private void error(final String message) {
		throw new RuntimeException(message);
	}

	private void errorClass(Throwable throwable) {
		this.errorClass = throwable.getClass().getName();
		if (errorMessage == null || errorMessage.trim().isEmpty()) {
			errorMessage = '[' + throwable.getClass().toString() + ']';
		}
	}

	protected boolean errorClassIs(String possibleErrorClass) {
		return errorClass.equals(possibleErrorClass);
	}

	private void errorMessage(final String errorMessage) {
		if (notDefined(errorMessage)) {
			this.errorMessage = "";
		} else {
			this.errorMessage = errorMessage;
		}
	}

	protected void filteredSystemProperties() {
		environment(System.getProperties());
		standardEnvironmentFilters();
		ec2EnvironmentFilters();
	}

	public AirbrakeNotice newNotice() {
		return new AirbrakeNotice(apiKey, projectRoot, environmentName, errorMessage, errorClass, backtrace, request, session, environment, environmentFilters,
				hasRequest, url, component, host);
	}

	private boolean notDefined(final Object object) {
		return object == null;
	}

	/** A hash of the request parameters that were given when the error occurred (required, but can be empty). */
	protected void request(final Map<String, Object> request) {
		this.request = request;
	}

	/** A hash of the session data that existed when the error occurred (required, but can be empty). */
	protected void session(final Map<String, Object> session) {
		this.session.putAll(session);
	}

	protected void standardEnvironmentFilters() {
		environmentFilter("java.awt.*");
		environmentFilter("java.vendor.*");
		environmentFilter("java.class.path");
		environmentFilter("java.vm.specification.*");
	}
}
