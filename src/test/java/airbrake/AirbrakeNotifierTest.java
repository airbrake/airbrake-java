// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import static airbrake.ApiKeys.*;
import static airbrake.Exceptions.*;
import static airbrake.Slurp.*;
import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.*;

import org.apache.commons.logging.*;
import org.hamcrest.*;
import org.junit.*;

public class AirbrakeNotifierTest {

	protected static final Backtrace BACKTRACE = new Backtrace(asList("backtrace is empty"));;
	protected static final Map<String, Object> REQUEST = new HashMap<String, Object>();
	protected static final Map<String, Object> SESSION = new HashMap<String, Object>();
	protected static final Map<String, Object> ENVIRONMENT = new HashMap<String, Object>();

	private final Log logger = LogFactory.getLog(getClass());

	private final Map<String, Object> EC2 = new HashMap<String, Object>();

	private AirbrakeNotifier notifier;

	private <T> Matcher<T> internalServerError() {
		return new BaseMatcher<T>() {
			public void describeTo(final Description description) {
				description.appendText("internal server error");
			}

			public boolean matches(final Object item) {
				return item.equals(500);
			}
		};
	}

	private int notifing(final String string) {
		return new AirbrakeNotifier().notify(new AirbrakeNoticeBuilder(API_KEY, ERROR_MESSAGE) {
			{
				backtrace(new Backtrace(asList(string)));
			}
		}.newNotice());
	}

	@Before
	public void setUp() {
		ENVIRONMENT.put("A_KEY", "test");
		EC2.put("AWS_SECRET", "AWS_SECRET");
		EC2.put("EC2_PRIVATE_KEY", "EC2_PRIVATE_KEY");
		EC2.put("AWS_ACCESS", "AWS_ACCESS");
		EC2.put("EC2_CERT", "EC2_CERT");
		notifier = new AirbrakeNotifier();
	}

	@Test
	public void testHowBacktraceairbrakeNotInternalServerError() {
		assertThat(notifing(ERROR_MESSAGE), not(internalServerError()));
		assertThat(notifing("java.lang.RuntimeException: an expression is not valid"), not(internalServerError()));
		assertThat(notifing("Caused by: java.lang.NullPointerException"), not(internalServerError()));
		assertThat(notifing("at org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1307)"), not(internalServerError()));
		assertThat(notifing("... 23 more"), not(internalServerError()));
	}

	@Test
	public void testLogErrorWithException() {
		logger.error("error", newException(ERROR_MESSAGE));
	}

	@Test
	public void testLogErrorWithoutException() {
		logger.error("error");
	}

	@Test
	public void testLogThresholdLesserThatErrorWithExceptionDoNotNotifyToairbrake() {
		logger.info("info", newException(ERROR_MESSAGE));
		logger.warn("warn", newException(ERROR_MESSAGE));
	}

	@Test
	public void testLogThresholdLesserThatErrorWithoutExceptionDoNotNotifyToairbrake() {
		logger.info("info");
		logger.warn("warn");
	}

	@Test
	public void testNotifyToairbrakeUsingBuilderNoticeFromExceptionInEnv() {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final AirbrakeNotice notice = new AirbrakeNoticeBuilder(API_KEY, EXCEPTION, "test").newNotice();

		assertThat(notifier.notify(notice), is(200));
	}

	@Test
	public void testNotifyToairbrakeUsingBuilderNoticeFromExceptionInEnvAndSystemProperties() {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final AirbrakeNotice notice = new AirbrakeNoticeBuilder(API_KEY, EXCEPTION, "test") {
			{
				filteredSystemProperties();
			}

		}.newNotice();

		assertThat(notifier.notify(notice), is(200));
	}

	@Test
	public void testNotifyToairbrakeUsingBuilderNoticeInEnv() {
		final AirbrakeNotice notice = new AirbrakeNoticeBuilder(API_KEY, ERROR_MESSAGE, "test").newNotice();

		assertThat(notifier.notify(notice), is(200));
	}

	@Test
	public void testSendExceptionNoticeWithFilteredBacktrace() {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final AirbrakeNotice notice = new AirbrakeNoticeBuilder(API_KEY, new QuietRubyBacktrace(), EXCEPTION, "test").newNotice();

		assertThat(notifier.notify(notice), is(200));
	}

	@Test
	public void testSendExceptionToairbrake() {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final AirbrakeNotice notice = new AirbrakeNoticeBuilder(API_KEY, EXCEPTION).newNotice();

		assertThat(notifier.notify(notice), is(200));
	}

	@Test
	public void testSendExceptionToairbrakeUsingRubyBacktrace() {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final AirbrakeNotice notice = new AirbrakeNoticeBuilder(API_KEY, new RubyBacktrace(), EXCEPTION, "test").newNotice();

		assertThat(notifier.notify(notice), is(200));
	}

	@Test
	public void testSendExceptionToairbrakeUsingRubyBacktraceAndFilteredSystemProperties() {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final AirbrakeNotice notice = new AirbrakeNoticeBuilderUsingFilteredSystemProperties(API_KEY, new RubyBacktrace(), EXCEPTION, "test").newNotice();

		assertThat(notifier.notify(notice), is(200));
	}

	@Test
	public void testSendNoticeToairbrake() {
		final AirbrakeNotice notice = new AirbrakeNoticeBuilder(API_KEY, ERROR_MESSAGE).newNotice();

		assertThat(notifier.notify(notice), is(200));
	}

	@Test
	public void testSendNoticeWithFilteredBacktrace() {
		final AirbrakeNotice notice = new AirbrakeNoticeBuilder(API_KEY, ERROR_MESSAGE) {
			{
				backtrace(new QuietRubyBacktrace(strings(slurp(read("backtrace.txt")))));
			}
		}.newNotice();

		assertThat(notifier.notify(notice), is(200));
	}

	@Test
	public void testSendNoticeWithLargeBacktrace() {
		final AirbrakeNotice notice = new AirbrakeNoticeBuilder(API_KEY, ERROR_MESSAGE) {
			{
				backtrace(new Backtrace(strings(slurp(read("backtrace.txt")))));
			}
		}.newNotice();

		assertThat(notifier.notify(notice), is(200));
	}
}
