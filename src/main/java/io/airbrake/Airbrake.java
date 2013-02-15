package io.airbrake;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class Airbrake {

	private String apiKey;
	private String appVersion;
	private String envName;
	private String noticesUrl;
	private String projectId;

	private String url = "http://collect.airbrake.io";

	private final List<String> stacktraceFilters = new ArrayList<String>();
	private final List<String> paramFilters = new ArrayList<String>();

	private AirbrakeNotice notice = new AirbrakeNotice(stacktraceFilters, paramFilters);

	protected Airbrake() {
	}

	public Airbrake(String apiKey, String projectId) {
		setApiKey(apiKey);
		setProjectId(projectId);
		setNoticesUrl(projectId);
	}

	public Airbrake(String apiKey, String projectId, String domainAirbrake) {
		setApiKey(apiKey);
		setNoticesUrl(projectId);
	}

	public void environment(String envName) {
		setEnvName(envName);
	}

	private void updateNotice() {
		notice = new AirbrakeNotice(stacktraceFilters, paramFilters);
	}

	public void notify(Throwable throwable) {
		notify(throwable, null, null, null);
	}

	public void notify(Throwable throwable, HttpServletRequest request) {
		notify(throwable, null, request, null);
	}

	public void notify(Throwable throwable, Properties properties) {
		notify(throwable, null, null, properties);
	}

	public void notify(Throwable throwable, Map session, ServletRequest request, Properties properties) {

		String json = notice.json(throwable, session, request, envName, properties, appVersion);

		System.out.println(json);

		URL url = null;
		try {
			url = new URL(noticesUrl + "?key=" + apiKey);
		} catch (MalformedURLException e) {
			System.err.println(e.getMessage());
			return;
		}

		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			System.err.println(e);
			return;
		}

		try {
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-type", "application/json");
			connection.setRequestMethod("POST");
			OutputStream out = connection.getOutputStream();
			out.write(json.getBytes());
			out.flush();
			out.close();
			connection.getResponseCode();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			throwable.printStackTrace();
		} finally {
			connection.disconnect();
		}

	}

	public void sensitiveFilter(String filter) {
		paramFilters.add(filter);
		updateNotice();
	}

	protected void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	private void setNoticesUrl(String projectId) {
		this.noticesUrl = url + "/api/v3/projects/" + projectId + "/notices";
	}

	protected void setProjectId(String projectId) {
		this.projectId = projectId;
		setNoticesUrl(projectId);
	}

	protected void setUrl(String url) {
		this.url = url;
		setNoticesUrl(projectId);
	}

	public void stacktraceFilter(String filter) {
		stacktraceFilters.add(filter);
		updateNotice();
	}

	public void version(String appVersion) {
		setAppVersion(appVersion);
	}

}