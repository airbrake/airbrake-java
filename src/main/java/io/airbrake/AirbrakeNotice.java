package io.airbrake;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class AirbrakeNotice {

	public static String json(Throwable throwable, ServletRequest request, String environment, Properties properties, String version) {

		StringBuilder json = new StringBuilder();

		json.append("{");

		jsonNotifier(json);
		json.append("\"errors\": [");
		jsonError(json, throwable);
		json.append("],");
		jsonContext(json, request, environment, version);
		jsonData(json, request);

		json.append("}");

		return json.toString();
	}

	private static void jsonData(StringBuilder json, ServletRequest request) {

		Map vars = getParamters(request);

		json.append("\"data\":{");

		if (vars != null) {
			Iterator<String> keys = vars.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				Object value = vars.get(key);
				json.append("\"" + key + "\":" + jsonize(value));
				if (keys.hasNext())
					json.append(",");
			}
		}

		json.append("}");
	}

	private static Map getParamters(ServletRequest request) {
		if (null == request)
			return new HashMap();
		return request.getParameterMap();
	}

	private static void jsonContext(StringBuilder json, ServletRequest request, String environment, String version) {
		String requestUrl = "";
		if (null != request) {
			requestUrl = ((HttpServletRequest) request).getRequestURL().toString();
		}
		String contextPath = "";
		if (null != request) {
			contextPath = ((HttpServletRequest) request).getContextPath();
		}
		json.append(("\"context\":{\"url\":\"" + requestUrl + "\",\"environment\":\"" + environment + "\",\"rootDirectory\":\"" + contextPath + "\",\"version\":\"" + version + "\"},"));
	}

	private static void jsonError(StringBuilder json, Throwable throwable) {
		if (null == throwable)
			return;
		String errorType = throwable.getClass().getName();
		String errorMessage = throwable.getMessage();
		json.append("{");
		json.append(("\"type\":\"" + errorType + "\","));
		json.append(("\"message\":\"" + errorMessage + "\","));
		jsonBacktrace(json, throwable.getStackTrace());
		json.append("}");
		Throwable cause = throwable.getCause();
		if (null == cause)
			return;
		if (cause.equals(throwable))
			return;
		json.append(",");
		jsonError(json, cause);
	}

	private static void jsonBacktrace(StringBuilder json, StackTraceElement[] stackTrace) {
		json.append("\"backtrace\":[");
		for (int i = 1; i < stackTrace.length; i++) {
			String line = stackTrace[i].toString();
			String classAndMethodName = line.replaceAll("\\(.*", "");
			String fileName = line.replaceAll("^.*\\(", "").replaceAll(":.*", "").replaceAll("\\)", "");
			String lineNumber = line.replaceAll("^.*:", "").replaceAll("\\)", "").replaceAll(":.*", "").replaceAll(".*Native Method", "");
			if ("".equals(lineNumber))
				lineNumber = "-1";
			String errorLine = "{\"file\":\"" + fileName + "\",\"line\":" + lineNumber + ",\"func\":\"" + escape(classAndMethodName) + "\"}";
			json.append(errorLine);
			if (i < stackTrace.length - 1)
				json.append(",");
		}
		json.append("]");
	}

	private static void jsonBacktrace(StringBuilder json, Throwable throwable) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		throwable.printStackTrace(new PrintStream(out));

		Scanner scanner = new Scanner(new ByteArrayInputStream(out.toByteArray())).useDelimiter("\n");
		json.append("\"backtrace\":[");
		if (scanner.hasNext())
			scanner.next();
		while (scanner.hasNext()) {
			String line = scanner.next();
			if (line.startsWith("\tat")) {
				String classAndMethodName = line.replaceAll("\\(.*", "");
				String fileName = line.replaceAll("^.*\\(", "").replaceAll(":.*", "").replaceAll("\\)", "");
				String lineNumber = line.replaceAll("^.*:", "").replaceAll("\\)", "").replaceAll(":.*", "").replaceAll(".*Native Method", "");
				if ("".equals(lineNumber))
					lineNumber = "-1";
				String errorLine = "{\"file\":\"" + fileName + "\",\"line\":" + lineNumber + ",\"func\":\"" + escape(classAndMethodName) + "\"}";
				json.append(errorLine);
			} else {
				json.append("{\"file\":null,\"line\":null,\"func\":\"" + escape(line) + "\"}");
			}
			if (scanner.hasNext())
				json.append(",");
		}
		json.append("]");
	}

	private static String escape(String string) {
		return string.replace("\t", "");
	}

	private static void jsonNotifier(StringBuilder json) {
		json.append("\"notifier\":{\"name\":\"airbrake-java\",\"version\":\"2.3\",\"url\":\"https://github.com/airbrake/airbrake-java\"},");
	}

	private static String jsonize(Object value) {

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

}
