// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import static java.util.Arrays.*;

import java.util.*;

public class AirbrakeNotice {

	private final String apiKey;

	private final String errorMessage;

	private Backtrace backtrace = new Backtrace(asList("backtrace is empty"));

	private String projectRoot;

	private String environmentName;

	private final Map<String, Object> environment = new TreeMap<String, Object>();

	private Map<String, Object> request = new TreeMap<String, Object>();

	private Map<String, Object> session = new TreeMap<String, Object>();

	private String errorClass;

	private boolean hasRequest = false;

	private final String url;

	private final String component;

	public AirbrakeNotice(final String apiKey, String projectRoot, String environmentName, final String errorMessage, String errorClass, final Backtrace backtrace, final Map<String, Object> request, final Map<String, Object> session, final Map<String, Object> environment,
			final List<String> environmentFilters, boolean hasRequest, String url, String component) {
		this.apiKey = apiKey;
		this.projectRoot = projectRoot;
		this.environmentName = environmentName;
		this.errorClass = errorClass;
		this.errorMessage = errorMessage;
		this.backtrace = backtrace;
		this.request = request;
		this.session = session;
		this.hasRequest = hasRequest;
		this.url = url;
		this.component = component;
		filter(environment, environmentFilters);
	}

	public String apiKey() {
		return apiKey;
	}

	public Backtrace backtrace() {
		return backtrace;
	}

	public String component() {
		return component;
	}

	public String env() {
		return environmentName;
	}

	public Map<String, Object> environment() {
		return environment;
	}

	public String errorClass() {
		return errorClass;
	}

	public String errorMessage() {
		return errorMessage;
	}

	private void filter(final Map<String, Object> environment, final List<String> environmentFilters) {
		for (final String key : environment.keySet()) {
			if (!matches(environmentFilters, key)) {
				this.environment.put(key, environment.get(key));
			}
		}
	}

	public boolean hasRequest() {
		return hasRequest;
	}

	private boolean matches(final List<String> environmentFilters, final String key) {
		for (final String filter : environmentFilters) {
			if (key.matches(filter))
				return true;
		}
		return false;
	}

	public String projectRoot() {
		return projectRoot;
	}

	public Map<String, Object> request() {
		return request;
	}

	public Map<String, Object> session() {
		return session;
	}

	public String url() {
		return url;
	}
}
