// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import java.util.*;

public class NoticeApi1 {

	private final StringBuilder yaml = new StringBuilder();

	public NoticeApi1(final AirbrakeNotice notice) {
		notice();
		{
			session();

			api_key(notice.apiKey());
			error_message(notice.errorMessage());
			error_class(notice.errorClass());
			request();

			environment(notice.environment());

			backtraces();
			{
				for (final String backtrace : notice.backtrace()) {
					backtrace(backtrace);
				}
			}
		}
	}

	private void api_key(final String string) {
		append("  api_key: " + string + "\n");
	}

	private void append(final Map<String, ?> map) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (final String key : map.keySet()) {
			stringBuilder.append("    " + key + ": " + map.get(key) + ",\n");
		}
		stringBuilder.append("\n");
		append(stringBuilder.toString().replaceAll(",\n\n$", "\n"));
	}

	private void append(final String string) {
		yaml.append(string);
	}

	private void backtrace(final String string) {
		append("  - " + string + "\n");
	}

	private void backtraces() {
		append("  backtrace: \n");
	}

	private void environment(final Map map) {
		append("  environment: {\n");
		append(map);
		append("  }\n");
	}

	private void error_class(final String string) {
		append("  error_class: " + string + "\n");
	}

	private void error_message(final String string) {
		append("  error_message: " + string + "\n");
	}

	private void notice() {
		append("notice: \n");
	}

	private void request() {
		append("  request: {}\n\n");
	}

	private void session() {
		append("  session: {}\n\n");
	}

	@Override
	public String toString() {
		return yaml.toString();
	}
}
