package io.airbrake;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class Airbrake {

	class Notifier {

		private String url;

		private final int version;

		public Notifier() {
			this(V2);
		}

		public Notifier(int version) {
			this.version = version;
		}

		public void notify(Throwable throwable, Map session, ServletRequest request, Properties properties) {
			if (V2 == version) notifyV2(throwable, session, request, properties);
			if (V3 == version) notifyV3(throwable, session, request, properties);
		}

		private void notifyV2(Throwable throwable, Map session, ServletRequest request, Properties properties) {
			POST(url, notice.toXml(throwable, session, request, paramEnvironment, properties, paramAppVersion), "application/xml");
		}

		private void notifyV3(Throwable throwable, Map session, ServletRequest request, Properties properties) {
			POST(url, notice.toJson(throwable, session, request, paramEnvironment, properties, paramAppVersion), "application/json");
		}

		private void POST(String noticesUrl, String content, String contentType) {
			
			System.out.println(content);

			if (true) return;

			URL url = null;
			try {
				url = new URL(noticesUrl);
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
				connection.setRequestProperty("Content-type", contentType);
				connection.setRequestMethod("POST");
				OutputStream out = connection.getOutputStream();
				out.write(content.getBytes());
				out.flush();
				out.close();
				connection.getResponseCode();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			} finally {
				connection.disconnect();
			}
		}

		public void setNoticesUrl(String projectId) {
			if (V2 == version) this.url = paramUrlPrefix + "/notifier_api/v2/notices";
			if (V3 == version) this.url = paramUrlPrefix + "/api/v3/projects/" + projectId + "/notices" + "?key=" + paramApiKey;
		}

	}

	public static final int V2 = 2;

	public static final int V3 = 3;

	private final List<String> filterSensitiveData = new ArrayList<String>();
	private final List<String> filterStacktraceNoise = new ArrayList<String>();

	private final AirbrakeNotice notice = new AirbrakeNotice(filterStacktraceNoise, filterSensitiveData);

	private Notifier notifier = new Notifier();

	private String paramApiKey;
	private String paramAppVersion;
	private String paramEnvironment;
	private String paramProjectId;
	private String paramUrlPrefix = "http://collect.airbrake.io";

	public Airbrake() {
		setV2();
	}

	public Airbrake(String apiKey) {
		setV2();
		setApiKey(apiKey);
		setNoticesUrl("");
	}

	public Airbrake(String apiKey, String projectId) {
		setV3();
		setApiKey(apiKey);
		setProjectId(projectId);
		setNoticesUrl(projectId);
	}

	public Airbrake(String apiKey, String projectId, String domainAirbrake) {
		setV3();
		setApiKey(apiKey);
		setNoticesUrl(projectId);
	}

	public void environment(String envName) {
		setEnvName(envName);
	}

	public void notify(Throwable throwable) {
		notify(throwable, null, null, null);
	}

	public void notify(Throwable throwable, HttpServletRequest request) {
		notify(throwable, null, request, null);
	}

	public void notify(Throwable throwable, Map session, ServletRequest request, Properties properties) {
		notifier.notify(throwable, session, request, properties);
	}

	public void notify(Throwable throwable, Properties properties) {
		notify(throwable, null, null, properties);
	}

	public void sensitiveFilter(String filter) {
		filterSensitiveData.add(filter);
	}

	protected void setApiKey(String apiKey) {
		this.paramApiKey = apiKey;
	}

	public void setAppVersion(String appVersion) {
		this.paramAppVersion = appVersion;
	}

	public void setEnvName(String envName) {
		this.paramEnvironment = envName;
	}

	private void setNoticesUrl(String projectId) {
		notifier.setNoticesUrl(projectId);
	}

	protected void setProjectId(String projectId) {
		setV3();
		this.paramProjectId = projectId;
		setNoticesUrl(projectId);
	}

	protected void setUrl(String url) {
		this.paramUrlPrefix = url;
		setNoticesUrl(paramProjectId);
	}

	private void setV2() {
		notifier = new Notifier(V2);
	}

	private void setV3() {
		notifier = new Notifier(V3);
	}

	public void stacktraceFilter(String filter) {
		filterStacktraceNoise.add(filter);
	}

	public void version(String appVersion) {
		setAppVersion(appVersion);
	}

}