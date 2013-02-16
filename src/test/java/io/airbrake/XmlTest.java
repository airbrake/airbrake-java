package io.airbrake;

import org.junit.*;

public class XmlTest {


	@Test
	public void testJson() {

		new AirbrakeNotice.Xml(System.out) {
			{
				begin("notice", "version", "2.3");
				{
					put("api-key", "76fdb93ab2cf276ec080671a8b3d3866");

					begin("notifier");
					{
						put("name", "airbrake-java");
						put("version", "2.3");
						put("url", "https://github.com/airbrake/airbrake-java");
					}
					end("notifier");

					begin("error");
					{
						put("class", "java.lang.RuntimeException");
						put("message", "error");
						begin("backtrace");
						{
							// for line in backtrace
							put("line", "method", "org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run", "file", "JUnit4TestReference.java", "number", "53");
						}
						end("backtrace");
					}
					end("error");

					begin("request");
					{
						put("url", "http://localhost:8080/sample/");
						begin("component");
						end("component");
						begin("action");
						end("action");
						begin("cgi-data");
						{
							// for var in vars
							put("var", "key", "attr", "value");
							put("var", "key", "creditCardNumber", "[FILTERED]");
						}
						end("cgi-data");
					}
					end("request");

					begin("server-environment");
					{
						put("project-root", "/sample");
						put("envirnment", "test");
						put("app-version", "1.0");
					}
					end("server-environment");
				}
				end("notice");
			}
		}.close();
	}
}
