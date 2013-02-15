package io.airbrake;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class AirbrakeNotice {

	private final List<String> stacktraceFilters;
	private final List<String> environmentFilters;
	private final List<String> paramFilters;

	public AirbrakeNotice(List<String> environmentFilters, List<String> stacktraceFilters, List<String> paramFilters) {
		this.environmentFilters = environmentFilters;
		this.stacktraceFilters = stacktraceFilters;
		this.paramFilters = paramFilters;
	}

	public String toJson(Throwable throwable, Map session, ServletRequest request, String environment, Properties properties, String version) {

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

	public String json(Throwable throwable, Map session, ServletRequest request, String environment, Properties properties, String version) {

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

	private void jsonEnvironment(StringBuilder json, Map environment) {

		json.append("\"environment\":{");

		if (environment != null) {
			Iterator<String> keys = environment.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				Object value = environment.get(key);
				json.append("\"" + key + "\":" + jsonize(value));
				if (keys.hasNext()) json.append(",");
			}
		}

		json.append("}");
	}

	private void jsonSession(StringBuilder json, Map vars) {

		json.append("\"session\":{");
		jsonVars(json, vars);
		json.append("}");
	}

	private void jsonVars(StringBuilder json, Map vars) {
		if (vars != null) {
			Iterator<String> keys = vars.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				Object value = "[FILTERED]";
				if(!paramFilters.contains(key))
					value = vars.get(key);
				json.append("\"" + key + "\":" + jsonize(value));
				if (keys.hasNext()) json.append(",");
			}
		}
	}

	private void jsonParams(StringBuilder json, Map vars) {
		json.append("\"params\":{");
		jsonVars(json, vars);
		json.append("}");
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
			if ("line.separator".equals(key)) continue;
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
