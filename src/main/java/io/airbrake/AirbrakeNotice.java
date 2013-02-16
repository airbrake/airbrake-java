package io.airbrake;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class AirbrakeNotice {

	public static class Xml {

		private final PrintWriter writer;

		public Xml(OutputStream out) {
			writer = new PrintWriter(out);
			append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		}

		protected void text(String string) {
			append(string);
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

		protected void put(String tag, String... attrValues) {
			if (attrValues.length == 1) {
				append("<", tag, ">", attrValues[0], "</", tag, ">");
			} else {
				append("<", tag);
				for (int i = 0; i < attrValues.length; i += 2) {
					append(" ", attrValues[i], "=\"", attrValues[i + 1], "\"");
				}
				append("/>");
			}
		}

		protected void end(String tag) {
			append("</", tag, ">");
		}

		public void close() {
			writer.flush();
			writer.close();
		}

	}

	public static class Json {

		private String escape(String string) {
			return string;
		}

		private final PrintWriter writer;

		private Stack<AtomicInteger> stack = new Stack<AtomicInteger>();

		public Json(OutputStream out) {
			writer = new PrintWriter(out);
			push();
		}

		protected void object(String name) {
			comma();
			append("\"" + name + "\":{");
			increment();
			push();
		}

		private void push() {
			stack.push(new AtomicInteger());
		}

		protected void object() {
			comma();
			append("{");
			increment();
			push();
		}

		protected void put(String name, String value) {
			comma();
			append("\"" + name + "\":\"" + escape(value) + "\"");
			increment();
		}

		protected void put(String name, Integer value) {
			comma();
			append("\"" + name + "\":" + value + "");
			increment();
		}

		private void increment() {
			stack.peek().incrementAndGet();
		}

		private void comma() {
			if (stack.peek().get() > 0) append(",");
		}

		protected void end() {
			append("}");
			pop();
		}

		private void pop() {
			stack.pop();
		}

		protected void endArray() {
			append("]");
			pop();
		}

		protected void array(String name) {
			comma();
			append("\"" + name + "\":[");
			increment();
			push();
		}

		private void append(String string) {
			writer.append(string);
		}

		public void close() {
			writer.flush();
			writer.close();
		}

	}

	private final List<String> stacktraceFilters;
	private final List<String> paramFilters;

	public AirbrakeNotice(List<String> stacktraceFilters, List<String> paramFilters) {
		this.stacktraceFilters = stacktraceFilters;
		this.paramFilters = paramFilters;
	}

	public String toXml(Throwable throwable, Map session, ServletRequest request, String environment, Properties properties, String version) {

		StringBuilder json = new StringBuilder();

		json.append("{");
		jsonNotifier(json);
		json.append("\"errors\": [");
		jsonErrors(json, throwable);
		json.append("],");
		jsonContext(json, request, environment, version);
		json.append(",");
		jsonEnvironment(json, getParamters(properties));
		json.append(",");
		jsonSession(json, session);
		json.append(",");
		jsonParams(json, getParamters(request));
		json.append("}");

		return json.toString();
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
					// {
					// // for error in errors
					// object();
					// {
					// put("type", "java.lang.RuntimeException");
					// put("message", "error");
					// array("backtrace");
					// {
					// // for line in backtrace
					// object();
					// {
					// put("file", "JUnit4TestReference.java");
					// put("line", 50);
					// put("function",
					// "org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run");
					// }
					// end();
					// }
					// endArray();
					// }
					// end();
					// }
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
					putVars(this, session);
					end();

					object("params");
					putVars(this, getParamters(request));
					end();
				}
				end();
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

		// StringBuilder json = new StringBuilder();
		//
		// json.append("{");
		// jsonNotifier(json);
		// json.append("\"errors\": [");
		// jsonErrors(json, throwable);
		// json.append("],");
		// jsonContext(json, request, environment, version);
		// json.append(",");
		// jsonEnvironment(json, getParamters(properties));
		// json.append(",");
		// jsonSession(json, session);
		// json.append(",");
		// jsonParams(json, getParamters(request));
		// json.append("}");
		//
		// System.out.println(json);
		//
		// return json.toString();
	}

	private void putErrors(Json json, Throwable throwable) {
		if (null == throwable) return;
		String errorType = throwable.getClass().getName();
		String errorMessage = throwable.getMessage();

		json.object();
		{
			json.put("type", errorType);
			json.put("message", errorMessage);
			putBacktrace(json, throwable.getStackTrace());
		}
		json.end();

		Throwable cause = throwable.getCause();
		if (null == cause) return;
		if (cause.equals(throwable)) return;
		putErrors(json, cause);
	}

	private void putBacktrace(Json json, StackTraceElement[] stackTrace) {

		json.array("backtrace");
		{
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
		json.endArray();
	}

	private void putVars(Json json, Map vars) {
		if (vars != null) {
			Iterator<String> keys = vars.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				if ("line.separator".equals(key)) continue;
				Object value = "[FILTERED]";
				if (!paramFilters.contains(key)) value = vars.get(key);
				json.append("\"" + key + "\":" + jsonize(value));
				if (keys.hasNext()) json.append(",");
			}
		}
	}

	private String contextPath(ServletRequest request) {
		if (null == request) return "";
		return ((HttpServletRequest) request).getContextPath();
	}

	private String requestUrl(ServletRequest request) {
		if (null == request) return "";
		return ((HttpServletRequest) request).getRequestURL().toString();
	}

	private void jsonEnvironment(StringBuilder json, Map vars) {
		json.append("\"environment\":{");
		jsonVars(json, vars);
		json.append("}");
	}

	private void jsonSession(StringBuilder json, Map vars) {
		json.append("\"session\":{");
		jsonVars(json, vars);
		json.append("}");
	}

	private void jsonParams(StringBuilder json, Map vars) {
		json.append("\"params\":{");
		jsonVars(json, vars);
		json.append("}");
	}

	private void jsonVars(StringBuilder json, Map vars) {
		if (vars != null) {
			Iterator<String> keys = vars.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				Object value = "[FILTERED]";
				if (!paramFilters.contains(key)) value = vars.get(key);
				json.append("\"" + key + "\":" + jsonize(value));
				if (keys.hasNext()) json.append(",");
			}
		}
	}

	private Map getParamters(ServletRequest request) {
		if (null == request) return new HashMap();
		return request.getParameterMap();
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

	private void jsonContext(StringBuilder json, ServletRequest request, String environment, String version) {
		String requestUrl = "";
		if (null != request) {
			requestUrl = ((HttpServletRequest) request).getRequestURL().toString();
		}
		String contextPath = "";
		if (null != request) {
			contextPath = ((HttpServletRequest) request).getContextPath();
		}
		json.append(("\"context\":{\"url\":\"" + requestUrl + "\",\"environment\":\"" + environment + "\",\"rootDirectory\":\"" + contextPath + "\",\"version\":\"" + version + "\"}"));
	}

	private void jsonErrors(StringBuilder json, Throwable throwable) {
		if (null == throwable) return;
		String errorType = throwable.getClass().getName();
		String errorMessage = throwable.getMessage();
		json.append("{");
		json.append(("\"type\":\"" + errorType + "\","));
		json.append(("\"message\":\"" + errorMessage + "\","));
		jsonBacktrace(json, throwable.getStackTrace());
		json.append("}");
		Throwable cause = throwable.getCause();
		if (null == cause) return;
		if (cause.equals(throwable)) return;
		json.append(",");
		jsonErrors(json, cause);
	}

	private void jsonBacktrace(StringBuilder json, StackTraceElement[] stackTrace) {
		json.append("\"backtrace\":[");
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
			String errorLine = "{\"file\":\"" + fileName + "\",\"line\":" + lineNumber + ",\"function\":\"" + escape(classAndMethodName) + "\"}";
			json.append(errorLine);
			if (i < stackTrace.length - 1) json.append(",");
		}
		json.append("]");
	}

	private String escape(String string) {
		return string.replace("\t", "");
	}

	private void jsonNotifier(StringBuilder json) {
		json.append("\"notifier\":{\"name\":\"airbrake-java\",\"version\":\"2.3\",\"url\":\"https://github.com/airbrake/airbrake-java\"},");
	}

	private String jsonize(Object value) {

		if (value instanceof String[]) {
			// put only first value
			String[] array = (String[]) value;
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < 1; i++) {
				result.append(jsonize(array[i]));
			}
			return result.toString();
		}

		return "\"" + value + "\"";
	}

	public String filter(String string, List<String> filters) {
		StringBuilder result = new StringBuilder();
		Scanner scanner = new Scanner(string).useDelimiter("\n");
		while (scanner.hasNext()) {
			String line = scanner.next();
			boolean requireFilter = false;
			for (String filter : filters) {
				requireFilter = requireFilter || line.contains(filter);
				if (requireFilter) break;
			}
			if (!requireFilter) result.append(line).append("\n");
		}
		return result.toString().replaceAll("\n$", "");
	}

	public String toString(Throwable t) {
		OutputStream out = new ByteArrayOutputStream();
		t.printStackTrace(new PrintStream(out));
		String string = out.toString();
		try {
			out.close();
		} catch (IOException e) {
		}
		return filter(string, stacktraceFilters);
	}

}
