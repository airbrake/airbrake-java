package io.airbrake;

import java.io.*;
import java.util.*;

import javax.servlet.*;

public class AirbrakeNotifierV3 extends AirbrakeNotifier {

	public AirbrakeNotifierV3(List<String> filterStacktraceNoise, List<String> filterSensitiveData) {
		super(filterStacktraceNoise, filterSensitiveData);
	}

	public void setUrl(String urlPrefix, String projectId, String apikey) {
		setUrl(urlPrefix + "/api/v3/projects/" + projectId + "/notices" + "?key=" + apikey);
	}
	
	public String toString(final Throwable throwable, final Map session, final ServletRequest request, final String environment, final Properties properties, final String version) {

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
					putErrors(throwable);
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
					putVars(getParamters(properties));
					end();

					object("session");
					putVars(getParamters(session));
					end();

					object("params");
					putVars(getParamters(request));
					end();
				}
				end();
			}

			void putBacktrace(StackTraceElement[] stackTrace) {

				for (int i = 1; i < stackTrace.length; i++) {

					String line = stackTrace[i].toString();
					boolean skipLine = false;
					for (String filter : filterStacktraceNoise) {
						skipLine = skipLine || line.contains(filter);
						if (skipLine) break;
					}
					if (skipLine) continue;

					String classAndMethodName = line.replaceAll("\\(.*", "");
					String fileName = line.replaceAll("^.*\\(", "").replaceAll(":.*", "").replaceAll("\\)", "");
					String lineNumber = line.replaceAll("^.*:", "").replaceAll("\\)", "").replaceAll(":.*", "").replaceAll(".*Native Method", "");
					if ("".equals(lineNumber)) lineNumber = "-1";

					object();
					{
						put("file", fileName);
						put("line", lineNumber);
						put("function", classAndMethodName);
					}
					end();
				}
			}

			void putErrors(Throwable throwable) {

				if (null == throwable) return;
				String errorType = throwable.getClass().getName();
				String errorMessage = throwable.getMessage();

				object();
				{
					put("type", errorType);
					put("message", errorMessage);
					array("backtrace");
					{
						putBacktrace(throwable.getStackTrace());
					}
					endArray();
				}
				end();

				Throwable cause = throwable.getCause();
				if (null == cause) return;
				if (cause.equals(throwable)) return;
				putErrors(cause);
			}

			void putVars(Map vars) {

				if (vars == null) return;

				Iterator<String> keys = vars.keySet().iterator();
				while (keys.hasNext()) {
					String key = keys.next();
					if ("line.separator".equals(key)) continue;
					Object value = "[FILTERED]";
					if (!filterSensitiveData.contains(key)) value = vars.get(key);
					if (value instanceof List) {
						List list = (List) value;
						if (list.isEmpty()) put(key, "");
						else put(key, list.get(0).toString());
					} else if (value instanceof String[]) {
						String[] strings = (String[]) value;
						if (strings.length == 0) put(key, "");
						else put(key, strings[0].toString());
					} else {
						put(key, value.toString());
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
