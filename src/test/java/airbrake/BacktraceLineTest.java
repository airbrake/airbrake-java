package airbrake;

import static org.junit.Assert.*;

import org.junit.*;

import airbrake.stacktrace.BacktraceLine;
import airbrake.stacktrace.JavaBacktraceLine;

public class BacktraceLineTest {

	@Test
	public void testBacktraceLineFromString() {
		JavaBacktraceLine backtraceLine = new JavaBacktraceLine("at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:46)");
		assertEquals("org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference", backtraceLine.className());
		assertEquals("run", backtraceLine.methodName());
		assertEquals(46, backtraceLine.lineNumber());
		assertEquals("JUnit4TestReference.java", backtraceLine.fileName());
	}

	@Test
	public void testBacktraceLineToString() {
		BacktraceLine backtraceLine = new JavaBacktraceLine("org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference", "JUnit4TestReference.java", 46, "run");
		assertEquals("at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:46)", backtraceLine.toString());
	}

	@Test
	public void testBacktraceLineToXml() {
		BacktraceLine backtraceLine = new JavaBacktraceLine("org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference", "JUnit4TestReference.java", 46, "run");
		assertEquals("<line method=\"org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run\" file=\"JUnit4TestReference.java\" number=\"46\"/>", backtraceLine.toXml());
	}

	@Test
	@Ignore
	public void testEscapeSpecialCharsInXml() {
		BacktraceLine backtraceLine = new JavaBacktraceLine("at com.company.Foo$$FastClassByCGLIB$$b505b4f2.invoke(<generated'\">:-1)");
		assertEquals("<line method=\"<indent> at com.company.Foo$$FastClassByCGLIB$$b505b4f2.invoke\" file=\"&lt;generated&apos;&quot;&gt;\" number=\"-1\"/>", backtraceLine.toXml());
	}
}
