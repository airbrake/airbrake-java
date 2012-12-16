// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import static airbrake.ApiKeys.*;
import static airbrake.Exceptions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.*;

public class AirbrakeAppenderTest {

	@Test
	public void testNewAppenderWithApiKey() {
		final AirbrakeAppender appender = new AirbrakeAppender(API_KEY);

		final AirbrakeNotice notice = appender.newNoticeFor(newException(ERROR_MESSAGE));

		assertThat(notice, is(notNullValue()));
	}

	@Test
	public void testNewAppenderWithApiKeyAndBacktrace() {
		final AirbrakeAppender appender = new AirbrakeAppender(API_KEY, new Backtrace());

		final AirbrakeNotice notice = appender.newNoticeFor(newException(ERROR_MESSAGE));

		assertThat(notice, is(notNullValue()));
	}

	@Test
	public void testNotyfyThrowable() {
		final AirbrakeAppender appender = new AirbrakeAppender(API_KEY, new RubyBacktrace());

		final AirbrakeNotice notice = appender.newNoticeFor(newException(ERROR_MESSAGE));

		assertThat(notice.backtrace(), hasItem("at airbrake.Exceptions.java:15:in `newException'"));
		assertThat(notice.backtrace(), hasItem("Caused by java.lang.NullPointerException"));
		assertThat(notice.backtrace(), hasItem("at airbrake.Exceptions.java:13:in `newException'"));
	}

	@Test
	public void testNotyfyThrowable$UseBacktrace() {
		final AirbrakeAppender appender = new AirbrakeAppender(API_KEY, new Backtrace());

		final AirbrakeNotice notice = appender.newNoticeFor(newException(ERROR_MESSAGE));

		assertThat(notice.backtrace(), hasItem("at airbrake.Exceptions.newException(Exceptions.java:15)"));
		assertThat(notice.backtrace(), hasItem("Caused by java.lang.NullPointerException"));
		assertThat(notice.backtrace(), hasItem("at airbrake.Exceptions.newException(Exceptions.java:13)"));

		assertThat(notice.backtrace(), hasItem("at sun.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java-2)"));
	}

	@Test
	public void testNotyfyThrowable$UseQuiteBacktrace() {
		final AirbrakeAppender appender = new AirbrakeAppender(API_KEY, new QuietRubyBacktrace());

		final AirbrakeNotice notice = appender.newNoticeFor(newException(ERROR_MESSAGE));

		assertThat(notice.backtrace(), hasItem("at airbrake.Exceptions.java:15:in `newException'"));
		assertThat(notice.backtrace(), hasItem("Caused by java.lang.NullPointerException"));
		assertThat(notice.backtrace(), hasItem("at airbrake.Exceptions.java:13:in `newException'"));
	}

	@Test
	public void testNotyfyThrowable$UseRubyBacktrace() {
		final AirbrakeAppender appender = new AirbrakeAppender(API_KEY, new RubyBacktrace());

		final AirbrakeNotice notice = appender.newNoticeFor(newException(ERROR_MESSAGE));

		assertThat(notice.backtrace(), hasItem("at airbrake.Exceptions.java:15:in `newException'"));
		assertThat(notice.backtrace(), hasItem("Caused by java.lang.NullPointerException"));
		assertThat(notice.backtrace(), hasItem("at airbrake.Exceptions.java:13:in `newException'"));
	}

	@Test
	public void testNotyfyThrowable$UseSwitchBacktrace() {
		final SwitchBacktrace switchBacktrace = new SwitchBacktrace();
		final AirbrakeAppender appender = new AirbrakeAppender(API_KEY, switchBacktrace);

		switchBacktrace.quiet();
		final AirbrakeNotice quietNotice = appender.newNoticeFor(newException(ERROR_MESSAGE));
		assertThat(quietNotice.backtrace(), not(hasItem("at sun.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java-2)")));

		switchBacktrace.verbose();
		final AirbrakeNotice verboseNotice = appender.newNoticeFor(newException(ERROR_MESSAGE));
		assertThat(verboseNotice.backtrace(), hasItem("at sun.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java-2)"));
	}
}
