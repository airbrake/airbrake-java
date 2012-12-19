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
		jsonError(json, throwable);
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
		String errorType = throwable.getClass().getName();
		String errorMessage = throwable.getMessage();
		json.append("\"error\":{");
		json.append(("\"type\":\"" + errorType + "\","));
		json.append(("\"message\":\"" + errorMessage + "\","));
		jsonBacktrace(json, throwable);
		json.append("},");
	}

	private static void jsonBacktrace(StringBuilder json, Throwable throwable) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		throwable.printStackTrace(new PrintStream(out));

		Scanner scanner = new Scanner(new ByteArrayInputStream(out.toByteArray())).useDelimiter("\n");
		json.append("\"backtrace\":[");
		while(scanner.hasNext()) {
			String line = scanner.next();
			if(line.startsWith("\tat")) {
				String classAndMethodName = line.replaceAll("\\(.*", "");
				String fileName = line.replaceAll("^.*\\(", "").replaceAll(":.*", "").replaceAll("\\)", "");
				String lineNumber = line.replaceAll("^.*:", "").replaceAll("\\)", "").replaceAll(":.*", "").replaceAll(".*Native Method", "");
				if ("".equals(lineNumber))
					lineNumber = "-1";
				String errorLine = "{\"file\":\"" + fileName + "\",\"line\":" + lineNumber + ",\"function\":\"" + escape(classAndMethodName) + "\"}";
				json.append(errorLine);				
			}else {
				json.append("{\"file\":null,\"line\":null,\"function\":\"" + escape(line) +"\"}");
			}
			if(scanner.hasNext())
				json.append(",");
		}
		json.append("]");
	}

	private static String escape(String string) {
		return string.replace("\t", "<indent>");
	}

	private static void jsonNotifier(StringBuilder json) {
		json.append("\"notifier\":{\"name\":\"airbrake-java\",\"version\":\"2.3\",\"url\":\"https://github.com/airbrake/airbrake-java\"},");
	}

	private static String jsonize(Object value) {

		if (value instanceof String[]) {
			String[] array = (String[]) value;
			StringBuilder result = new StringBuilder();
			result.append("[");
			for (int i = 0; i < array.length; i++) {
				result.append(jsonize(array[i]));
				if (i < array.length - 1)
					result.append(",");
			}
			result.append("]");
			return result.toString();
		}

		return "\"" + value + "\"";
	}

}
