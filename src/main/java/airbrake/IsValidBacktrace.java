// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import org.hamcrest.*;

public class IsValidBacktrace<T> extends BaseMatcher<T> {

	@Factory
	public static final <T> Matcher<T> validBacktrace() {
		return new IsValidBacktrace();
	}

	public void describeTo(final Description description) {
		description.appendText("valid backtrace");
	}

	public boolean matches(final Object item) {
		return item.toString().matches("[^:]*:\\d+.*");
	}

}
