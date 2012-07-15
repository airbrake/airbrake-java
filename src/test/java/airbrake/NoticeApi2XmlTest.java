// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.*;

public class NoticeApi2XmlTest {

	NoticeApi2XmlBuilderTest t;

	private AirbrakeNotice notice;

	private String clean(String string) {
		return string.replaceAll("\\\"", "");
	}

	@Before
	public void setUp() {
		notice = new AirbrakeNoticeBuilder("apiKey", new RuntimeException("errorMessage")).newNotice();
	}

	@Test
	public void testApiKey() {
		assertThat(xml(new NoticeApi2(notice)), containsString("<api-key>apiKey</api-key>"));
	}

	@Test
	public void testError() {
		assertThat(xml(new NoticeApi2(notice)), containsString("error>"));
	}

	@Test
	public void testErrorBacktrace() {
		assertThat(xml(new NoticeApi2(notice)), containsString("backtrace>"));
	}

	@Test
	public void testErrorBacktraceLine() {
		System.out.println(new NoticeApi2(notice));
		assertThat(xml(new NoticeApi2(notice)), containsString("<line method=org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run file=JUnit4TestReference.java number=50/>"));
	}

	@Test
	public void testErrorClass() {
		assertThat(xml(new NoticeApi2(notice)), containsString("<class>java.lang.RuntimeException</class>"));
	}

	@Test
	public void testErrorMessage() {
		assertThat(xml(new NoticeApi2(notice)), containsString("<message>errorMessage</message>"));
	}

	@Test
	public void testNoticeVersion() {
		assertThat(xml(new NoticeApi2(notice)), containsString("notice version=2.0"));
	}

	@Test
	public void testNotifier() {
		assertThat(xml(new NoticeApi2(notice)), containsString("notifier>"));
	}

	@Test
	public void testNotifierName() {
		assertThat(xml(new NoticeApi2(notice)), containsString("<name>airbrake</name>"));
	}

	@Test
	public void testNotifierUrl() {
		assertThat(xml(new NoticeApi2(notice)), containsString("<url>http://airbrake.googlecode.com</url>"));
	}

	@Test
	public void testNotifierVersion() {
		assertThat(xml(new NoticeApi2(notice)), containsString("<version>1.7-socrata-SNAPSHOT</version>"));
	}

	private String xml(NoticeApi2 noticeApi) {
		return clean(noticeApi.toString());
	}

}
