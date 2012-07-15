// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import java.io.*;
import java.net.*;

public class AirbrakeNotifier {

	private void addingProperties(final HttpURLConnection connection) throws ProtocolException {
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-type", "text/xml");
		connection.setRequestProperty("Accept", "text/xml, application/xml");
		connection.setRequestMethod("POST");
	}

	private HttpURLConnection createConnection() throws IOException {
		final HttpURLConnection connection = (HttpURLConnection) new URL("http://api.airbrake.io/notifier_api/v2/notices").openConnection();
		return connection;
	}

	private void err(final AirbrakeNotice notice, final Exception e) {
		e.printStackTrace();
	}

	public int notify(final AirbrakeNotice notice) {
		try {
			final HttpURLConnection toairbrake = createConnection();
			addingProperties(toairbrake);
			String toPost = new NoticeApi2(notice).toString();
			return send(toPost, toairbrake);
		} catch (final Exception e) {
			err(notice, e);
		}
		return 0;
	}

	private int send(final String yaml, final HttpURLConnection connection) throws IOException {
		System.out.println(yaml);
		int statusCode;
		final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
		writer.write(yaml);
		writer.close();
		statusCode = connection.getResponseCode();
		return statusCode;
	}

}
