package io.airbrake;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class Airbrake {

	private final List<String> filterSensitiveData = new ArrayList<String>();
	private final List<String> filterStacktraceNoise = new ArrayList<String>();

	private String paramApiKey;
	private String paramAppVersion;
	private String paramEnvironment;
	private String paramProjectId;
	private String paramUrl = "http://collect.airbrake.io";

	private AirbrakeNotifier notice;

	public Airbrake() {
		setV2();
	}

	public Airbrake(String apiKey) {
		setV2();
		setApiKey(apiKey);
		updateUrl();
	}

	public Airbrake(String apiKey, String projectId) {
		setV3();
		setApiKey(apiKey);
		setProjectId(projectId);
		updateUrl();
	}

	public Airbrake(String apiKey, String projectId, String urlPrefix) {
		setV3();
		setApiKey(apiKey);
		setUrlPrefix(urlPrefix);
		updateUrl();
	}

	private void setUrlPrefix(String urlPrefix) {
		this.paramUrl = urlPrefix;
	}

	public void notify(Throwable throwable) {
		notify(throwable, null, null, null);
	}

	public void notify(Throwable throwable, HttpServletRequest request) {
		notify(throwable, null, request, null);
	}

	public void notify(Throwable throwable, Map session, ServletRequest request, Properties properties) {
		notice.notify(throwable, session, request, paramEnvironment, properties, paramAppVersion);
	}

	public void notify(Throwable throwable, Properties properties) {
		notify(throwable, null, null, properties);
	}

	public void sensitiveFilter(String filter) {
		filterSensitiveData.add(filter);
	}

	protected void setApiKey(String apiKey) {
		this.paramApiKey = apiKey;
		notice.setApiKey(apiKey);
	}

	public void setAppVersion(String appVersion) {
		this.paramAppVersion = appVersion;
		notice.setAppVersion(appVersion);
	}

	public void setEnvName(String envName) {
		this.paramEnvironment = envName;
		notice.setEnvironment(envName);
	}

	private void updateUrl() {
		notice.setUrl(paramUrl, paramProjectId, paramApiKey);
	}

	protected void setProjectId(String projectId) {
		this.paramProjectId = projectId;
		setV3();
		updateUrl();
	}

	public void setUrl(String url) {
		this.paramUrl = url;
		updateUrl();
	}

	private void setV2() {
		notice = new AirbrakeNotifierV2(filterStacktraceNoise, filterSensitiveData);
		notice.setApiKey(paramApiKey);
		notice.setAppVersion(paramAppVersion);
		notice.setEnvironment(paramEnvironment);
	}

	private void setV3() {
		notice = new AirbrakeNotifierV3(filterStacktraceNoise, filterSensitiveData);
		notice.setApiKey(paramApiKey);
		notice.setAppVersion(paramAppVersion);
		notice.setEnvironment(paramEnvironment);
	}

	public void stacktraceFilter(String filter) {
		filterStacktraceNoise.add(filter);
	}

	public void version(String appVersion, String envName) {
		setAppVersion(appVersion);
		setEnvName(envName);
	}

}