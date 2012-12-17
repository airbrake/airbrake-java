// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import java.io.*;
import java.net.*;

public class AirbrakeNotifier {

	public static String slurp(InputStream stream) {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			byte[] buff = new byte[4096];
			for (int len; (len = stream.read(buff)) != -1;) {
				outputStream.write(buff, 0, len);
			}
			return outputStream.toString();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String url;

	public AirbrakeNotifier() {
		setUrl("http://api.airbrake.io/notifier_api/v2/notices");
	}

	public AirbrakeNotifier(String url) {
		setUrl(url);
	}

	private void addingPropertiesTo(final HttpURLConnection connection) throws ProtocolException {
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-type", "text/xml");
		connection.setRequestProperty("Accept", "text/xml, application/xml");
		connection.setRequestMethod("POST");
	}

	private HttpURLConnection createConnection() throws IOException {
		return (HttpURLConnection) new URL(url).openConnection();
	}

	public int notify(final AirbrakeNotice notice) {
		try {
			final HttpURLConnection airbrakeConnection = createConnection();
			addingPropertiesTo(airbrakeConnection);
			String noticeXml = new NoticeXml(notice).toString();
			return send(noticeXml, airbrakeConnection);
		} catch (final Exception e) {
			printStacktrace(notice, e);
		}
		return 0;
	}

	private void printStacktrace(final AirbrakeNotice notice, final Exception e) {
		e.printStackTrace();
	}

	private int send(final String xml, final HttpURLConnection connection) throws IOException {
		final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
		writer.write(xml);
		writer.close();
		int statusCode = connection.getResponseCode();
		return statusCode;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
