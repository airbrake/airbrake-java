package io.airbrake;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.servlet.http.*;

import org.junit.*;

public class AirbrakeNoticeTest {

	@Test
	public void shouldContainsNotifier() {
		String json = AirbrakeNotice.json(new RuntimeException("error"), null, "test", null, "1.0");
		assertTrue(json.contains("notifier\":{\"name\":\"airbrake-java\",\"version\":\"2.3\",\"url\":\"https://github.com/airbrake/airbrake-java"));
	}

	@Test
	public void shouldContainsBacktrace() {
		String json = AirbrakeNotice.json(new RuntimeException("error"), null, "test", null, "1.0");
		assertTrue(json.contains("backtrace\":"));
		assertFalse(json.contains("\"file\":\"Native Method)\",\"line\":sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method}"));
	}

	@Test
	public void shouldContainsServerEnv() {
		String json = AirbrakeNotice.json(new RuntimeException("error"), request("/sample", "/", new HashMap()), "test", null, "1.0");
		assertTrue(json.contains("context\":{\"url\":\"http://localhost:8080/sample/\",\"environment\":\"test\",\"rootDirectory\":\"/sample\",\"version\":\"1.0"));
	}

	@Test
	public void shouldContainsVars() {
		Map<String, Object> parameterMap = new HashMap();
		parameterMap.put("a", "a");
		parameterMap.put("b", new String[] {"a", "b"});
		String json = AirbrakeNotice.json(new RuntimeException("error"), request("/sample", "/", parameterMap), "test", null, "1.0");
		System.out.println(json);
		assertTrue(json.contains("params\":{"));
		assertTrue(json.contains("\"a\":\"a\""));
		assertTrue(json.contains("\"b\":\"a\""));
	}

	private HttpServletRequest request(String contextPath, String requestUri, Map<String, Object> parameterMap) {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getParameterMap()).thenReturn(parameterMap);
		when(request.getContextPath()).thenReturn(contextPath);
		when(request.getRequestURI()).thenReturn(contextPath + requestUri);
		when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/sample/"));
		return request;
	}
}
