package io.airbrake;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
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
			append("\"" + name + "\":\"" + escapeJson(value) + "\"");
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
				append(" ", attrValues[i], "=\"", escapeXml(attrValues[i + 1]), "\"");
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
				append("<", tag, "><![CDATA[", attrValues[0], "]]></", tag, ">");
			} else if (attrValues.length == 3) {
				append("<", tag, " ", attrValues[0], "=\"", escapeXml(attrValues[1]), "\"><![CDATA[", attrValues[2], "]]></", tag, ">");
			} else {
				append("<", tag);
				for (int i = 0; i < attrValues.length; i += 2) {
					append(" ", attrValues[i], "=\"", escapeXml(attrValues[i + 1]), "\"");
				}
				append("/>");
			}
		}

		protected void text(String string) {
			append(string);
		}

	}

	protected static String escapeJson(String string) {
		if (null == string) return "";
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			switch (ch) {
			case '"':
				result.append("\\\"");
				break;
			case '\\':
				result.append("\\\\");
				break;
			case '\b':
				result.append("\\b");
				break;
			case '\f':
				result.append("\\f");
				break;
			case '\n':
				result.append("\\n");
				break;
			case '\r':
				result.append("\\r");
				break;
			case '\t':
				result.append("\\t");
				break;
			// case '/':
			// result.append("\\/");
			// break;
			default:
				// Reference: http://www.unicode.org/versions/Unicode5.1.0/
				if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
					String hex = Integer.toHexString(ch);
					result.append("\\u");
					for (int k = 0; k < 4 - hex.length(); k++) {
						result.append('0');
					}
					result.append(hex.toUpperCase());
				} else {
					result.append(ch);
				}
			}
		}
		return result.toString();
	}

	protected static String escapeXml(String string) {
		if (null == string) return "";
		boolean anyCharactersProtected = false;
		StringBuilder stringBuffer = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			boolean controlCharacter = ch < 32;
			boolean unicodeButNotAscii = ch > 126;
			boolean characterWithSpecialMeaningInXML = ch == '<' || ch == '&' || ch == '>';
			if (characterWithSpecialMeaningInXML || unicodeButNotAscii || controlCharacter) {
				stringBuffer.append("&#" + (int) ch + ";");
				anyCharactersProtected = true;
			} else {
				stringBuffer.append(ch);
			}
		}
		if (anyCharactersProtected == false) return string;
		return stringBuffer.toString();
	}

	protected String apiKey;
	protected String appVersion;

	protected String environment;
	protected final List<String> filterSensitiveData;
	protected final List<String> filterStacktraceNoise;
	private String url;

	public AirbrakeNotifier(List<String> filterStacktraceNoise, List<String> filterSensitiveData) {
		this.filterStacktraceNoise = filterStacktraceNoise;
		this.filterSensitiveData = filterSensitiveData;
	}

	protected String contextPath(ServletRequest request) {
		if (null == request) return "";
		return ((HttpServletRequest) request).getContextPath();
	}

	protected Map getParamters(Map map) {
		if (null == map) return new HashMap();
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
		Charset enc, unicode = Charset.forName("UTF-8");
		if (null == request.getCharacterEncoding()) {
			enc = Charset.forName("ISO-8859-1");
		} else {
			enc = Charset.forName(request.getCharacterEncoding());
		}
		
		if (unicode != enc) {
			Map<String, String> paramsUtf8 = new HashMap<String, String>();
			
			Enumeration params = request.getParameterNames();
			while (params.hasMoreElements()) {
				String origKey = params.nextElement().toString();
				String origValue = request.getParameter(origKey);
				
				String key = new String(origKey.getBytes(enc), unicode);
				String value = new String(origValue.getBytes(enc), unicode);
				paramsUtf8.put(key, value);
			}
			
			return paramsUtf8;
		}
		return request.getParameterMap();
	}

	protected String getRequestComponent(ServletRequest request) {
		return ((HttpServletRequest) request).getServletPath();
	}
	
	protected String getRequestAction(ServletRequest request) {
		return ((HttpServletRequest) request).getMethod();
	}
	
	public void notify(Throwable throwable, Map session, ServletRequest request, String environment, Properties properties, String version) {
		POST(url, toString(throwable, session, request, environment, properties, version), "application/xml");
	}

	private void POST(String noticesUrl, String content, String contentType) {

		System.out.println(content);

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

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	protected void setUrl(String url) {
		this.url = url;
	}

	public abstract void setUrl(String urlPrefix, String projectId, String apikey);

	protected abstract String toString(Throwable throwable, Map session, ServletRequest request, String environment, Properties properties, String version);
}
