// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import static airbrake.ValidBacktraces.*;

import java.text.*;
import java.util.*;
import java.util.regex.*;

public class Backtrace implements Iterable<String> {

	public static boolean isValidBacktrace(String string) {
		return string.matches("[^:]*:\\d+.*");
	}

	public static boolean notValidBacktrace(String string) {
		return !isValidBacktrace(string);
	}
	private final List<String> backtrace = new LinkedList<String>();

	private final List<String> ignoreRules = new LinkedList<String>();

	private final List<String> filteredBacktrace = new LinkedList<String>();

	protected Backtrace() {
	}

	public Backtrace(final List<String> backtrace) {
		this.backtrace.addAll(backtrace);
		ignore();
		filter();
	}

	public Backtrace(final Throwable throwable) {
		toBacktrace(throwable);
		ignore(".*" + Pattern.quote(messageIn(throwable)) + ".*");
		ignore();
		filter();
	}

	private String causedBy(final Throwable throwable) {
		return MessageFormat.format("Caused by {0}", messageIn(throwable));
	}

	protected void filter() {
		filter(backtrace);
	}

	private final List<String> filter(final List<String> backtrace) {
		final ListIterator<String> iterator = backtrace.listIterator();
		while (iterator.hasNext()) {
			String string = iterator.next();
			if (mustBeIgnored(string)) {
				continue;
			}
			if (notValidBacktrace(string)) {
				string = removeDobuleDot(string);
			}
			filteredBacktrace.add(string);
		}

		return filteredBacktrace;
	}

	protected void ignore() {
		ignoreEmptyCause();
	}

	protected void ignore(final String ignoreRule) {
		ignoreRules.add(ignoreRule);
	}

	protected void ignoreJetty() {
		ignore(".*org.eclipse.jetty.*");
	}

	protected void ignoreCocoon() {
		ignore(".*org.apache.cocoon.components.expression.*");
		ignore(".*org.apache.cocoon.template.script.*");
		ignore(".*org.apache.cocoon.template.instruction.*");
		ignore(".*org.apache.cocoon.template.JXTemplateGenerator.*");
		ignore(".*org.apache.cocoon.components.pipeline.AbstractProcessingPipeline.*");
		ignore(".*org.apache.cocoon.components.treeprocessor.*");
		ignore(".*org.apache.cocoon.environment.ForwardRedirector.*");
		ignore(".*org.apache.cocoon.components.flow.AbstractInterpreter.*");
		ignore(".*org.apache.cocoon.components.flow.javascript.fom.FOM_JavaScriptInterpreter.*");
		ignore(".*org.apache.cocoon.components.flow.javascript.fom.FOM_Cocoon.*");
		ignore(".*org.apache.commons.jexl.util.introspection.*");
		ignore(".*org.apache.commons.jexl.parser.ASTMethod.*");
		ignore(".*org.apache.commons.jexl.parser.ASTReference.*");
		ignore(".*org.apache.commons.jexl.ExpressionImpl.*");
		ignore(".*org.apache.cocoon.template.expression.*");
		ignore(".*org.apache.cocoon.Cocoon.*");
		ignore(".*org.apache.cocoon.servlet.*");
	}

	protected void ignoreEclipse() {
		ignore(".*org.eclipse.jdt.internal.junit4.runner.*");
		ignore(".*org.eclipse.jdt.internal.junit.runner.*");
	}

	private void ignoreEmptyCause() {
		ignore("^Caused by $");
	}

	protected void ignoreJunit() {
		ignore(".*.*org.junit.internal.runners.*");
	}

	protected void ignoreMortbayJetty() {
		ignore(".*org.mortbay.jetty.handler.ContextHandlerCollection.*");
		ignore(".*org.mortbay.jetty.handler.ContextHandler.*");
		ignore(".*org.mortbay.jetty.handler.HandlerCollection.*");
		ignore(".*org.mortbay.jetty.handler.HandlerWrapper.*");
		ignore(".*org.mortbay.jetty.HttpConnection.*");
		ignore(".*org.mortbay.jetty.HttpParser.*");
		ignore(".*org.mortbay.jetty.security.SecurityHandler.*");
		ignore(".*org.mortbay.jetty.Server.*");
		ignore(".*org.mortbay.jetty.servlet.ServletHandler.*");
		ignore(".*org.mortbay.jetty.servlet.ServletHolder.*");
		ignore(".*org.mortbay.jetty.servlet.SessionHandler.*");
		ignore(".*org.mortbay.jetty.webapp.WebAppContext.*");
		ignore(".*org.mortbay.io.nio.*");
		ignore(".*org.mortbay.thread.*");
	}

	protected void ignoreMozilla() {
		ignore(".*org.mozilla.javascript.FunctionObject.*");
		ignore(".*org.mozilla.javascript.ScriptRuntime.*");
		ignore(".*org.mozilla.javascript.continuations.*");
		ignore(".*org.mozilla.javascript.ScriptRuntime.*");
		ignore(".*org.mozilla.javascript.ScriptableObject.*");
		ignore(".*org.mozilla.javascript.FunctionObject.*");
	}

	protected void ignoreNoise() {
		ignore(".*inv1.invoke.*");
		ignore(".*javax.servlet.http.HttpServlet.*");
		ignore(".*sun.reflect.*");
		ignore(".*java.lang.reflect.Method.*");
	}

	protected void ignoreSpringSecurity() {
		ignore(".*org.springframework.security.context.HttpSessionContextIntegrationFilter.*");
		ignore(".*org.springframework.security.intercept.web.FilterSecurityInterceptor.*");
		ignore(".*org.springframework.security.providers.anonymous.AnonymousProcessingFilter.*");
		ignore(".*org.springframework.security.ui.AbstractProcessingFilter.*");
		ignore(".*org.springframework.security.ui.basicauth.BasicProcessingFilter.*");
		ignore(".*org.springframework.security.ui.ExceptionTranslationFilter.*");
		ignore(".*org.springframework.security.ui.logout.LogoutFilter.*");
		ignore(".*org.springframework.security.ui.rememberme.RememberMeProcessingFilter.*");
		ignore(".*org.springframework.security.ui.SessionFixationProtectionFilter.*");
		ignore(".*org.springframework.security.ui.SpringSecurityFilter.*");
		ignore(".*org.springframework.security.util.FilterChainProxy.*");
		ignore(".*org.springframework.security.wrapper.SecurityContextHolderAwareRequestFilter.*");
		ignore(".*org.springframework.web.filter.DelegatingFilterProxy.*");
	}

	public Iterator<String> iterator() {
		if (needToBeFiltered()) {
			filter(backtrace);
		}
		return filteredBacktrace.iterator();
	}

	private String messageIn(final Throwable throwable) {
		String message = throwable.getMessage();
		if (message == null) {
			message = throwable.getClass().getName();
		}
		return message;
	}

	private boolean mustBeIgnored(final String string) {
		for (final String ignore : ignoreRules) {
			if (string.matches(ignore))
				return true;
		}
		return false;
	}

	private boolean needToBeFiltered() {
		return filteredBacktrace.isEmpty();
	}

	public Backtrace newBacktrace(final Throwable throwable) {
		return new Backtrace(throwable);
	}

	private String removeDobuleDot(final String string) {
		return string.replaceAll(":", "");
	}

	private String toBacktrace(final StackTraceElement element) {
		return toBacktrace(element.getClassName(), element.getFileName(),
				element.getLineNumber(), element.getMethodName());
	}

	protected String toBacktrace(final String className, final String fileName,
			final int lineNumber, final String methodName) {
		return new BacktraceLine(className, fileName, lineNumber, methodName)
				.toString();
	}

	private void toBacktrace(final Throwable throwable) {
		if (throwable == null)
			return;

		backtrace.add(causedBy(throwable));
		for (final StackTraceElement element : throwable.getStackTrace()) {
			backtrace.add(toBacktrace(element));
		}

		toBacktrace(throwable.getCause());
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder();
		for (final String string : filteredBacktrace) {
			stringBuilder.append(string).append("\n");
		}
		return stringBuilder.toString();
	}
}
