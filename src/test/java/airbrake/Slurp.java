// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import java.io.*;
import java.util.*;

public class Slurp {
	public static InputStream read(final String file) {
		final InputStream backtraceAsStream;
		try {
			backtraceAsStream = Slurp.class.getClassLoader().getResourceAsStream(file);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		return backtraceAsStream;
	}

	public static final String slurp(final InputStream inputStream) {
		final StringBuffer out = new StringBuffer();
		try {
			final byte[] b = new byte[4096];
			for (int n; (n = inputStream.read(b)) != -1;) {
				out.append(new String(b, 0, n));
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		return out.toString();
	}

	public static final List<String> strings(final String backtraceAsString) {
		final List<String> strings = new LinkedList<String>();
		final Scanner scanner = new Scanner(backtraceAsString).useDelimiter("\n");
		while (scanner.hasNext()) {
			strings.add(scanner.next());
		}
		return strings;
	}

}
