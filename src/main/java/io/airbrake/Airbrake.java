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

	public void notify(Throwable throwable) {
		notify(throwable, null, null);
	}

	public void notify(Throwable throwable, HttpServletRequest request) {
		notify(throwable, request, null);
	}

	public void notify(Throwable throwable, ServletRequest request, Properties properties) {

		String json = AirbrakeNotice.json(throwable, request, envName, properties, appVersion);
		
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

}