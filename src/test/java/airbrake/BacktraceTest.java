// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package airbrake;

import static airbrake.Exceptions.*;
import static airbrake.Slurp.*;
import static airbrake.ValidBacktraces.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.*;
import java.util.regex.*;

import org.apache.commons.lang.exception.*;
import org.junit.*;

public class BacktraceTest {

	public static final List<String> backtrace() {
		return strings(slurp(read("backtrace.txt")));
	}

	private List<String> filteredBacktrace() {
		return strings(slurp(read("filteredBacktrace.txt")));
	}

	@Test
	public void testEscapesExceptionClassName() {
		try {
			new Backtrace(new Exception("com.banana.MyClass{junk}"));
		} catch (PatternSyntaxException e) {
			fail("Throwing a pattern syntax exception means the class name might not have been escaped properly");
		}
	}

	@Test
	public void testExceptionToRubyBacktrace() {
		final Throwable EXCEPTION = newException("java.lang.RuntimeException: undefined method `password' for nil:NilClass");

		final Iterable<String> backtrace = new RubyBacktrace(EXCEPTION);

		assertThat(backtrace, hasItem("at airbrake.Exceptions.java:15:in `newException'"));
	}

	@Test
	public void testExceptionToRubyBacktrace$UsingNewRubyBacktraceEmptyInstanceAsFactoryOfRubyBacktrace() {
		final Throwable EXCEPTION = newException("java.lang.RuntimeException: undefined method `password' for nil:NilClass");

		final Iterable<String> backtrace = new RubyBacktrace().newBacktrace(EXCEPTION);

		assertThat(backtrace, hasItem("at airbrake.Exceptions.java:15:in `newException'"));
	}

	@Test
	public void testFilteredIgnoringMessage() {
		final Throwable EXCEPTION = newException("java.lang.RuntimeException: undefined method `password' for nil:NilClass");

		final Iterable<String> backtrace = new QuietRubyBacktrace(EXCEPTION);

		assertThat(backtrace, not(hasItem("java.lang.RuntimeException: undefined method `password' for nil:NilClass")));
		assertThat(backtrace, not(hasItem("java.lang.RuntimeException undefined method `password' for nilNilClass")));
	}

	@Test
	public void testFilteredIgnoringMessage$UsingNewQuiteBacktraceEmptyInstanceAsFactoryOfQuietRubyBacktrace() {
		final Throwable EXCEPTION = newException(ERROR_MESSAGE);

		final Iterable<String> backtrace = new QuietRubyBacktrace().newBacktrace(EXCEPTION);

		assertThat(backtrace, not(hasItem("java.lang.RuntimeException: undefined method `password' for nil:NilClass")));
		assertThat(backtrace, not(hasItem("java.lang.RuntimeException undefined method `password' for nilNilClass")));
	}

	@Test
	public void testFilteredSafeCausedByTest() {
		final Throwable EXCEPTION = newException(ERROR_MESSAGE);

		final Iterable<String> backtrace = new Backtrace(strings(ExceptionUtils.getStackTrace(EXCEPTION)));

		assertThat(backtrace, not(hasItem("Caused by: java.lang.NullPointerException")));
		assertThat(backtrace, hasItem("Caused by java.lang.NullPointerException"));

		assertThat(backtrace, not(hasItem("java.lang.RuntimeException: undefined method `password' for nil:NilClass")));
		assertThat(backtrace, hasItem("java.lang.RuntimeException undefined method `password' for nilNilClass"));
	}

	@Test
	public void testIgnoreJettyBacktrace() {
		final Iterable<String> backtrace = new Backtrace(backtrace()) {
			@Override
			protected void ignore() {
				ignoreJetty();
			}
		};

		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1307)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:453)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:559)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:232)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1072)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:382)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:194)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1006)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.server.handler.ContextHandlerCollection.handle(ContextHandlerCollection.java:255)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.server.handler.HandlerCollection.handle(HandlerCollection.java:154)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.server.Server.handle(Server.java:365)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:485)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:926)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:988)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:635)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.server.AsyncHttpConnection.handle(AsyncHttpConnection.java:82)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.io.nio.SelectChannelEndPoint.handle(SelectChannelEndPoint.java:627)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.io.nio.SelectChannelEndPoint$1.run(SelectChannelEndPoint.java:51)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608)")));
		assertThat(backtrace, not(hasItem("at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)")));
	}

	@Test
	public void testIgnoreEclipseBacktrace() {
		final Iterable<String> backtrace = new Backtrace(backtrace()) {
			@Override
			protected void ignore() {
				ignoreEclipse();
			}
		};

		assertThat(backtrace, not(hasItem("org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:45)")));
		assertThat(backtrace, not(hasItem("org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)")));
		assertThat(backtrace, not(hasItem("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:460)")));
	}

	@Test
	public void testIgnoreExceptionGenerateInsideTest() {
		final Throwable EXCEPTION = newException(ERROR_MESSAGE);

		final Iterable<String> backtrace = new Backtrace(strings(ExceptionUtils.getStackTrace(EXCEPTION))) {
			@Override
			protected void ignore() {
				ignoreJunit();
				ignoreEclipse();
				ignoreNoise();
			}

		};

		assertThat(backtrace, not(hasItem("	at org.junit.internal.runners.TestMethod.invoke(TestMethod.java:59)")));
		assertThat(backtrace, not(hasItem("	at org.junit.internal.runners.MethodRoadie.runTestMethod(MethodRoadie.java:98)")));
		assertThat(backtrace, not(hasItem("	at org.junit.internal.runners.MethodRoadie.runBeforesThenTestThenAfters(MethodRoadie.java:87)")));
		assertThat(backtrace, not(hasItem("	at org.junit.internal.runners.MethodRoadie.runTest(MethodRoadie.java:77)")));
		assertThat(backtrace, not(hasItem("	at org.junit.internal.runners.MethodRoadie.run(MethodRoadie.java:42)")));
		assertThat(backtrace, not(hasItem("	at org.junit.internal.runners.JUnit4ClassRunner.invokeTestMethod(JUnit4ClassRunner.java:88)")));
		assertThat(backtrace, not(hasItem("	at org.junit.internal.runners.ClassRoadie.runUnprotected(ClassRoadie.java:27)")));
		assertThat(backtrace, not(hasItem("	at org.junit.internal.runners.JUnit4ClassRunner.run(JUnit4ClassRunner.java:42)")));

		assertThat(backtrace, not(hasItem("	at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:45)")));
		assertThat(backtrace, not(hasItem("	at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)")));
		assertThat(backtrace, not(hasItem("	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:460)")));

		assertThat(backtrace, not(hasItem("	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)")));
		assertThat(backtrace, not(hasItem("	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)")));
		assertThat(backtrace, not(hasItem("	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)")));
	}

	@Test
	public void testIgnoreIgnomreCommonsBacktrace() {
		final Iterable<String> backtrace = new QuietRubyBacktrace(backtrace());
		final Iterable<String> filteredBacktrace = new QuietRubyBacktrace(filteredBacktrace());

		assertEquals(filteredBacktrace.toString(), backtrace.toString());
	}

	@Test
	public void testIgnoreJunitBacktrace() {
		final Iterable<String> backtrace = new Backtrace(backtrace()) {
			@Override
			protected void ignore() {
				ignoreJunit();
			}
		};

		assertThat(backtrace, not(hasItem("org.junit.internal.runners.TestMethod.invoke(TestMethod.java:59)")));
		assertThat(backtrace, not(hasItem("org.junit.internal.runners.MethodRoadie.runTestMethod(MethodRoadie.java:98)")));
		assertThat(backtrace, not(hasItem("org.junit.internal.runners.MethodRoadie.runBeforesThenTestThenAfters(MethodRoadie.java:87)")));
		assertThat(backtrace, not(hasItem("org.junit.internal.runners.MethodRoadie.runTest(MethodRoadie.java:77)")));
		assertThat(backtrace, not(hasItem("org.junit.internal.runners.MethodRoadie.run(MethodRoadie.java:42)")));
		assertThat(backtrace, not(hasItem("org.junit.internal.runners.JUnit4ClassRunner.invokeTestMethod(JUnit4ClassRunner.java:88)")));
		assertThat(backtrace, not(hasItem("org.junit.internal.runners.ClassRoadie.runUnprotected(ClassRoadie.java:27)")));
		assertThat(backtrace, not(hasItem("org.junit.internal.runners.JUnit4ClassRunner.run(JUnit4ClassRunner.java:42)")));
	}

	@Test
	public void testIgnoreMortbayJettyBacktrace() {
		final Iterable<String> backtrace = new Backtrace(backtrace()) {
			@Override
			protected void ignore() {
				ignoreMortbayJetty();
			}
		};

		assertThat(backtrace, not(hasItem("org.mortbay.jetty.handler.ContextHandlerCollection.handle(ContextHandlerCollection.java:206)")));
		assertThat(backtrace, not(hasItem("org.mortbay.jetty.handler.ContextHandler.handle(ContextHandler.java:729)")));
		assertThat(backtrace, not(hasItem("org.mortbay.jetty.handler.HandlerCollection.handle(HandlerCollection.java:114)")));
		assertThat(backtrace, not(hasItem("org.mortbay.jetty.handler.HandlerWrapper.handle(HandlerWrapper.java:152)")));
		assertThat(backtrace, not(hasItem("org.mortbay.jetty.HttpConnection.handle(HttpConnection.java:380)")));
		assertThat(backtrace, not(hasItem("org.mortbay.jetty.HttpConnection.handleRequest(HttpConnection.java:505)")));
		assertThat(backtrace, not(hasItem("org.mortbay.jetty.HttpConnection$RequestHandler.content(HttpConnection.java:843)")));
		assertThat(backtrace, not(hasItem("org.mortbay.jetty.HttpParser.parseAvailable(HttpParser.java:211)")));
		assertThat(backtrace, not(hasItem("org.mortbay.jetty.HttpParser.parseNext(HttpParser.java:647)")));
		assertThat(backtrace, not(hasItem("org.mortbay.jetty.security.SecurityHandler.handle(SecurityHandler.java:216)")));
		assertThat(backtrace, not(hasItem("org.mortbay.jetty.Server.handle(Server.java:324)")));
		assertThat(backtrace, not(hasItem("org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1,088)")));
		assertThat(backtrace, not(hasItem("org.mortbay.jetty.servlet.ServletHandler.handle(ServletHandler.java:360)")));
		assertThat(backtrace, not(hasItem("org.mortbay.jetty.servlet.ServletHolder.handle(ServletHolder.java:487)")));
		assertThat(backtrace, not(hasItem("org.mortbay.jetty.servlet.SessionHandler.handle(SessionHandler.java:181)")));
		assertThat(backtrace, not(hasItem("org.mortbay.jetty.webapp.WebAppContext.handle(WebAppContext.java:405)")));
		assertThat(backtrace, not(hasItem("org.mortbay.io.nio.SelectChannelEndPoint.run(SelectChannelEndPoint.java:395)")));
		assertThat(backtrace, not(hasItem("org.mortbay.thread.QueuedThreadPool$PoolThread.run(QueuedThreadPool.java:488)")));
	}

	@Test
	public void testIgnoreMozillaBacktrace() {
		final Iterable<String> backtrace = new Backtrace(backtrace()) {
			@Override
			protected void ignore() {
				ignoreMozilla();
			}
		};

		assertThat(backtrace, not(hasItem("org.mozilla.javascript.FunctionObject.doInvoke(FunctionObject.java:523)")));
		assertThat(backtrace, not(hasItem("org.mozilla.javascript.ScriptRuntime.call(ScriptRuntime.java:1,244)")));
		assertThat(backtrace, not(hasItem("org.mozilla.javascript.continuations.ContinuationInterpreter.interpret(ContinuationInterpreter.java:1,134)")));
		assertThat(backtrace, not(hasItem("org.mozilla.javascript.ScriptRuntime.call(ScriptRuntime.java:1,244)")));
		assertThat(backtrace, not(hasItem("org.mozilla.javascript.ScriptableObject.callMethod(ScriptableObject.java:1,591)")));
		assertThat(backtrace, not(hasItem("org.mozilla.javascript.FunctionObject.doInvoke(FunctionObject.java:523)")));
	}

	@Test
	public void testIgnoreNoiseBacktrace() {
		final Iterable<String> backtrace = new Backtrace(backtrace()) {
			@Override
			protected void ignore() {
				ignoreNoise();
			}
		};

		assertThat(backtrace, not(hasItem("inv1.invoke(:-1)")));
		assertThat(backtrace, not(hasItem("javax.servlet.http.HttpServlet.service(HttpServlet.java:820)")));
		assertThat(backtrace, not(hasItem("sun.reflect.GeneratedMethodAccessor338.invoke(null:-1)")));
		assertThat(backtrace, not(hasItem("sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)")));
		assertThat(backtrace, not(hasItem("java.lang.reflect.Method.invoke(Method.java:597)")));
	}

	@Test
	public void testIgnoreSpringBacktrace() {
		final Iterable<String> backtrace = new Backtrace(backtrace()) {
			@Override
			protected void ignore() {
				ignoreSpringSecurity();
			}
		};

		assertThat(backtrace, not(hasItem("org.springframework.security.context.HttpSessionContextIntegrationFilter.doFilterHttp(HttpSessionContextIntegrationFilter.java:235)")));
		assertThat(backtrace, not(hasItem("org.springframework.security.intercept.web.FilterSecurityInterceptor.doFilter(FilterSecurityInterceptor.java:83)")));
		assertThat(backtrace, not(hasItem("org.springframework.security.providers.anonymous.AnonymousProcessingFilter.doFilterHttp(AnonymousProcessingFilter.java:105)")));
		assertThat(backtrace, not(hasItem("org.springframework.security.ui.AbstractProcessingFilter.doFilterHttp(AbstractProcessingFilter.java:271)")));
		assertThat(backtrace, not(hasItem("org.springframework.security.ui.basicauth.BasicProcessingFilter.doFilterHttp(BasicProcessingFilter.java:173)")));
		assertThat(backtrace, not(hasItem("org.springframework.security.ui.ExceptionTranslationFilter.doFilterHttp(ExceptionTranslationFilter.java:101)")));
		assertThat(backtrace, not(hasItem("org.springframework.security.ui.logout.LogoutFilter.doFilterHttp(LogoutFilter.java:89)")));
		assertThat(backtrace, not(hasItem("org.springframework.security.ui.rememberme.RememberMeProcessingFilter.doFilterHttp(RememberMeProcessingFilter.java:116)")));
		assertThat(backtrace, not(hasItem("org.springframework.security.ui.SessionFixationProtectionFilter.doFilterHttp(SessionFixationProtectionFilter.java:67)")));
		assertThat(backtrace, not(hasItem("org.springframework.security.ui.SpringSecurityFilter.doFilter(SpringSecurityFilter.java:53)")));
		assertThat(backtrace, not(hasItem("org.springframework.security.util.FilterChainProxy.doFilter(FilterChainProxy.java:174)")));
		assertThat(backtrace, not(hasItem("org.springframework.security.wrapper.SecurityContextHolderAwareRequestFilter.doFilterHttp(SecurityContextHolderAwareRequestFilter.java:91)")));
		assertThat(backtrace, not(hasItem("org.springframework.web.filter.DelegatingFilterProxy.doFilter(DelegatingFilterProxy.java:167)")));
	}

	@Test
	public void testJavaBacktrace() {
		final Throwable EXCEPTION = newException(ERROR_MESSAGE);

		final Iterable<String> backtrace = new Backtrace(EXCEPTION);

		assertThat(backtrace, hasItem("at airbrake.Exceptions.newException(Exceptions.java:15)"));
	}

	@Test
	public void testJavaBacktrace$UsingNewBacktraceEmptyInstanceAsFactoryOfBacktrace() {
		final Throwable EXCEPTION = newException(ERROR_MESSAGE);

		final Iterable<String> backtrace = new Backtrace().newBacktrace(EXCEPTION);

		assertThat(backtrace, hasItem("at airbrake.Exceptions.newException(Exceptions.java:15)"));
	}

	@Test
	public void testNotValidaBacktrace() {
		String string = "Caused by: java.lang.NullPointerException";
		assertFalse(isValidBacktrace(string));
	}

	@Test
	public void testValidaBacktrace() {
		assertTrue(isValidBacktrace("at org.junit.internal.runners.TestMethod.invoke(TestMethod.java:59)"));
		assertTrue(isValidBacktrace("vendors/rails/actionpack/lib/action_controller/filter.rb:579:in `call_filters'"));
	}
}
