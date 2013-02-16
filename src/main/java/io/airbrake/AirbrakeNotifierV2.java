package io.airbrake;

import java.io.*;
import java.util.*;

import javax.servlet.*;

public class AirbrakeNotifierV2 extends AirbrakeNotifier {

	public AirbrakeNotifierV2(List<String> filterStacktraceNoise, List<String> filterSensitiveData) {
		super(filterStacktraceNoise, filterSensitiveData);
	}

	public void setUrl(String urlPrefix, String projectId, String apikey) {
		setUrl(urlPrefix + "/notifier_api/v2/notices");
	}

	public String toString(final Throwable throwable, final Map session, final ServletRequest request, final String environment, final Properties properties, final String version) {

		final String requestUrl = requestUrl(request);
		final String contextPath = contextPath(request);

		OutputStream out = new ByteArrayOutputStream();

		new Xml(out) {
			{
				begin("notice", "version", "2.3");
				{
					put("api-key", apiKey);

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
					for (String filter : filterStacktraceNoise) {
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
					if (!filterSensitiveData.contains(key)) value = vars.get(key);
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
