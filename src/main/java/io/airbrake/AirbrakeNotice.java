package io.airbrake;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class AirbrakeNotice {

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

	private final List<String> paramFilters;
	private final List<String> stacktraceFilters;

	public AirbrakeNotice(List<String> stacktraceFilters, List<String> paramFilters) {
		this.stacktraceFilters = stacktraceFilters;
		this.paramFilters = paramFilters;
	}

	private String contextPath(ServletRequest request) {
		if (null == request) return "";
		return ((HttpServletRequest) request).getContextPath();
	}

	private Map getParamters(Map map) {
		return map;
	}

	private Map getParamters(Properties properties) {
		if (null == properties) return new HashMap();
		Map result = new HashMap();
		Iterator<Object> iterator = properties.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			result.put(key, properties.get(key));
		}
		return result;
	}

	private Map getParamters(ServletRequest request) {
		if (null == request) return new HashMap();
		return request.getParameterMap();
	}

	private String requestUrl(ServletRequest request) {
		if (null == request) return "";
		return ((HttpServletRequest) request).getRequestURL().toString();
	}

	public String toJson(final Throwable throwable, final Map session, final ServletRequest request, final String environment, final Properties properties, final String version) {

		final String requestUrl = requestUrl(request);
		final String contextPath = contextPath(request);

		OutputStream out = new ByteArrayOutputStream();

		new Json(out) {
			{
				object();
				{
					object("notifier");
					{
						put("name", "airbrake-java");
						put("version", "2.3");
						put("url", "https://github.com/airbrake/airbrake-java");
					}
					end();

					array("errors");
					putErrors(this, throwable);
					endArray();

					object("context");
					{
						put("url", requestUrl);
						put("environment", environment);
						put("rootDirectory", contextPath);
						put("version", version);
					}
					end();

					object("environment");
					putVars(this, getParamters(properties));
					end();

					object("session");
					putVars(this, getParamters(session));
					end();

					object("params");
					putVars(this, getParamters(request));
					end();
				}
				end();
			}

			void putBacktrace(Json json, StackTraceElement[] stackTrace) {

				for (int i = 1; i < stackTrace.length; i++) {

					String line = stackTrace[i].toString();
					boolean skipLine = false;
					for (String filter : stacktraceFilters) {
						skipLine = skipLine || line.contains(filter);
						if (skipLine) break;
					}
					if (skipLine) continue;

					String classAndMethodName = line.replaceAll("\\(.*", "");
					String fileName = line.replaceAll("^.*\\(", "").replaceAll(":.*", "").replaceAll("\\)", "");
					String lineNumber = line.replaceAll("^.*:", "").replaceAll("\\)", "").replaceAll(":.*", "").replaceAll(".*Native Method", "");
					if ("".equals(lineNumber)) lineNumber = "-1";

					json.object();
					{
						json.put("file", fileName);
						json.put("line", lineNumber);
						json.put("function", classAndMethodName);
					}
					json.end();
				}
			}

			void putErrors(Json json, Throwable throwable) {
				
				if (null == throwable) return;
				String errorType = throwable.getClass().getName();
				String errorMessage = throwable.getMessage();

				json.object();
				{
					json.put("type", errorType);
					json.put("message", errorMessage);
					json.array("backtrace");
					{
						putBacktrace(json, throwable.getStackTrace());
					}
					json.endArray();
				}
				json.end();

				Throwable cause = throwable.getCause();
				if (null == cause) return;
				if (cause.equals(throwable)) return;
				putErrors(json, cause);
			}

			void putVars(Json json, Map vars) {

				if (vars == null) return;

				Iterator<String> keys = vars.keySet().iterator();
				while (keys.hasNext()) {
					String key = keys.next();
					if ("line.separator".equals(key)) continue;
					Object value = "[FILTERED]";
					if (!paramFilters.contains(key)) value = vars.get(key);
					if (value instanceof List) {
						List list = (List) value;
						if (list.isEmpty()) json.put(key, "");
						else json.put(key, list.get(0).toString());
					} else if (value instanceof String[]) {
						String[] strings = (String[]) value;
						if (strings.length == 0) json.put(key, "");
						else json.put(key, strings[0].toString());
					} else {
						json.put(key, value.toString());
					}
				}
			}
		}.close();

		String result = out.toString();

		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			// nop
		}

		return result;
	}

	public String toXml(final Throwable throwable, final Map session, final ServletRequest request, final String environment, final Properties properties, final String version, final String apikey) {

		final String requestUrl = requestUrl(request);
		final String contextPath = contextPath(request);

		OutputStream out = new ByteArrayOutputStream();

		new Xml(System.out) {
			{
				begin("notice", "version", "2.3");
				{
					put("api-key", apikey);

					begin("notifier");
					{
						put("name", "airbrake-java");
						put("version", "2.3");
						put("url", "https://github.com/airbrake/airbrake-java");
					}
					end("notifier");

					putErrors(throwable);

					begin("request");
					{
						put("url", requestUrl);
						begin("component");
						end("component");
						begin("action");
						end("action");
						begin("cgi-data");
						putVars(getParamters(request));
						end("cgi-data");
					}
					end("request");

					begin("server-environment");
					{
						put("project-root", contextPath);
						put("environment", environment);
						put("app-version", version);
					}
					end("server-environment");
				}
				end("notice");
			}

			void putErrors(Throwable throwable) {

				if (null == throwable) return;
				String errorType = throwable.getClass().getName();
				String errorMessage = throwable.getMessage();

				begin("error");
				{
					put("class", errorType);
					put("message", errorMessage);
					begin("backtrace");
					{
						putBacktrace(throwable.getStackTrace());
					}
					end("backtrace");
				}
				end("error");

				Throwable cause = throwable.getCause();
				if (null == cause) return;
				if (cause.equals(throwable)) return;
				putErrors(cause);
			}

			private void putBacktrace(StackTraceElement[] stackTrace) {

				for (int i = 1; i < stackTrace.length; i++) {

					String line = stackTrace[i].toString();
					boolean skipLine = false;
					for (String filter : stacktraceFilters) {
						skipLine = skipLine || line.contains(filter);
						if (skipLine) break;
					}
					if (skipLine) continue;

					String classAndMethodName = line.replaceAll("\\(.*", "");
					String fileName = line.replaceAll("^.*\\(", "").replaceAll(":.*", "").replaceAll("\\)", "");
					String lineNumber = line.replaceAll("^.*:", "").replaceAll("\\)", "").replaceAll(":.*", "").replaceAll(".*Native Method", "");
					if ("".equals(lineNumber)) lineNumber = "-1";

					put("line", "method", classAndMethodName, "file", fileName, "number", lineNumber);
				}
			}

			private void putVars(Map vars) {

				Iterator<String> keys = vars.keySet().iterator();
				while (keys.hasNext()) {
					String key = keys.next();
					if ("line.separator".equals(key)) continue;
					Object value = "[FILTERED]";
					if (!paramFilters.contains(key)) value = vars.get(key);
					if (value instanceof List) {
						List list = (List) value;
						if (list.isEmpty()) put("var", "key", key, "");
						else put("var", "key", key, list.get(0).toString());
					} else if (value instanceof String[]) {
						String[] strings = (String[]) value;
						if (strings.length == 0) put("var", "key", key, "");
						else put("var", "key", key, strings[0].toString());
					} else {
						put("var", "key", key, value.toString());
					}
				}
			}

		}.close();

		String result = out.toString();

		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			// nop
		}

		return result;
	}

}
