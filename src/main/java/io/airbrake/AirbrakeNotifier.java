package io.airbrake;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import javax.servlet.*;
import javax.servlet.http.*;

public abstract class AirbrakeNotifier {

	public static class Json {

		private Stack<AtomicInteger> stack = new Stack<AtomicInteger>();

		private final PrintWriter writer;

		public Json(OutputStream out) {
			writer = new PrintWriter(out);
			push();
		}

		private void append(String string) {
			writer.append(string);
		}

		protected void array(String name) {
			comma();
			append("\"" + name + "\":[");
			increment();
			push();
		}

		public void close() {
			writer.flush();
			writer.close();
		}

		private void comma() {
			if (stack.peek().get() > 0) append(",");
		}

		protected void end() {
			append("}");
			pop();
		}

		protected void endArray() {
			append("]");
			pop();
		}

		private String escape(String string) {
			return string;
		}

		private void increment() {
			stack.peek().incrementAndGet();
		}

		protected void object() {
			comma();
			append("{");
			increment();
			push();
		}

		protected void object(String name) {
			comma();
			append("\"" + name + "\":{");
			increment();
			push();
		}

		private void pop() {
			stack.pop();
		}

		private void push() {
			stack.push(new AtomicInteger());
		}

		protected void put(String name, Integer value) {
			comma();
			append("\"" + name + "\":" + value + "");
			increment();
		}

		protected void put(String name, String value) {
			comma();
			append("\"" + name + "\":\"" + escape(value) + "\"");
			increment();
		}
	}

	public static class Xml {

		private final PrintWriter writer;

		public Xml(OutputStream out) {
			writer = new PrintWriter(out);
			append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		}

		private void append(String... strings) {
			for (String string : strings)
				writer.append(string);
		}

		protected void begin(String tag, String... attrValues) {
			append("<");
			append(tag);
			for (int i = 0; i < attrValues.length; i += 2) {
				append(" ", attrValues[i], "=\"", attrValues[i + 1], "\"");
			}
			append(">");
		}

		public void close() {
			writer.flush();
			writer.close();
		}

		protected void end(String tag) {
			append("</", tag, ">");
		}

		protected void put(String tag, String... attrValues) {
			if (attrValues.length == 1) {
				append("<", tag, ">", attrValues[0], "</", tag, ">");
			} else if (attrValues.length == 3) {
				append("<", tag, " ", attrValues[0], "=\"", attrValues[1], "\">", attrValues[2], "</", tag, ">");
			} else {
				append("<", tag);
				for (int i = 0; i < attrValues.length; i += 2) {
					append(" ", attrValues[i], "=\"", attrValues[i + 1], "\"");
				}
				append("/>");
			}
		}

		protected void text(String string) {
			append(string);
		}
	}

	protected final List<String> filterSensitiveData;
	protected final List<String> filterStacktraceNoise;

	private String url;
	protected String apiKey;
	protected String appVersion;
	protected String environment;

	public AirbrakeNotifier(List<String> filterStacktraceNoise, List<String> filterSensitiveData) {
		this.filterStacktraceNoise = filterStacktraceNoise;
		this.filterSensitiveData = filterSensitiveData;
	}

	protected String contextPath(ServletRequest request) {
		if (null == request) return "";
		return ((HttpServletRequest) request).getContextPath();
	}

	protected Map getParamters(Map map) {
		return map;
	}

	protected Map getParamters(Properties properties) {
		if (null == properties) return new HashMap();
		Map result = new HashMap();
		Iterator<Object> iterator = properties.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			result.put(key, properties.get(key));
		}
		return result;
	}

	protected Map getParamters(ServletRequest request) {
		if (null == request) return new HashMap();
		return request.getParameterMap();
	}

	public void notify(Throwable throwable, Map session, ServletRequest request, String environment, Properties properties, String version) {
		POST(url, toString(throwable, session, request, environment, properties, version), "application/xml");
	}

	private void POST(String noticesUrl, String content, String contentType) {

		HttpURLConnection connection = null;

		try {
			connection = (HttpURLConnection) new URL(noticesUrl).openConnection();
		} catch (MalformedURLException e) {
			System.err.println(e.getMessage());
			return;
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

	protected String requestUrl(ServletRequest request) {
		if (null == request) return "";
		return ((HttpServletRequest) request).getRequestURL().toString();
	}

	protected void setUrl(String url) {
		this.url = url;
	}

	public abstract void setUrl(String urlPrefix, String projectId, String apikey);

	protected abstract String toString(Throwable throwable, Map session, ServletRequest request, String environment, Properties properties, String version);

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}
}
