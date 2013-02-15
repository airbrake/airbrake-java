package io.airbrake;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.servlet.http.*;

import org.junit.*;

public class AirbrakeJsonTest {

	private AirbrakeNotice notice;

	private static final List<String> environmentFilters = Arrays.asList("");
	private static final List<String> stacktraceFilters = Arrays.asList("sun.reflect", "java.lang.reflect", "org.junit");
	private static final List<String> paramFilters = Arrays.asList("creditCardNumber");

	@Before
	public void setUp() {
		notice = new AirbrakeNotice(environmentFilters, stacktraceFilters, paramFilters);
	}

	@Test
	public void shouldFilterStacktrace() {
		Throwable t = new RuntimeException("error");
		String json = notice.toJson(t, null, null, "test", System.getProperties(), "1.0");
		assertFalse(json.contains("sun.reflect"));
		assertFalse(json.contains("java.lang.reflect"));
		assertFalse(json.contains("org.junit"));
	}

	@Test
	public void shouldContainsNotifier() {
		Throwable t = new RuntimeException("error");
		String json = notice.toJson(t, null, null, "test", System.getProperties(), "1.0");
		assertTrue(json.contains("notifier\":{\"name\":\"airbrake-java\",\"version\":\"2.3\",\"url\":\"https://github.com/airbrake/airbrake-java"));
	}

	@Test
	public void shouldContainsBacktrace() {
		Throwable t = new RuntimeException("error");
		String json = notice.toJson(t,null,  null, "test", System.getProperties(), "1.0");
		assertTrue(json.contains("backtrace\":"));
		assertFalse(json.contains("\"file\":\"Native Method)\",\"line\":sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method}"));
	}

	@Test
	public void shouldContainsServerEnv() {
		Throwable t = new RuntimeException("error");
		String json = notice.toJson(t, null, request("/sample", "/", new HashMap()), "test", System.getProperties(), "1.0");
		assertTrue(json.contains("context\":{\"url\":\"http://localhost:8080/sample/\",\"environment\":\"test\",\"rootDirectory\":\"/sample\",\"version\":\"1.0"));
	}

	@Test
	public void shouldContainsVars() {
		Map<String, Object> map = new HashMap();
		map.put("creditCardNumber", "1234-5678-9101-1121");
		map.put("b", new String[] { "a", "b" });
		Throwable t = new RuntimeException("error");
		String json = notice.toJson(t, map, request("/sample", "/", map), "test", System.getProperties(), "1.0");
		System.out.println(json);
		assertTrue(json.contains("params\":{"));
		assertFalse(json.contains("\"creditCardNumber\":\"1234-5678-9101-1121\""));
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
