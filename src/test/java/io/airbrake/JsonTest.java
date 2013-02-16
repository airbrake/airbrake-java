package io.airbrake;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.junit.*;

public class JsonTest {

	@Test
	public void testJson() {

		new AirbrakeNotice.Json(System.out) {
			{
				object();
				{
					object("notifier");
					{
						put("name", "airbrake-java");
						put("version", "2.3");
						put("url", "https://github.com/airbrake/airbrake-java");
					}
					end();

					array("errors");
					{
						// for error in errors
						object();
						{
							put("type", "java.lang.RuntimeException");
							put("message", "error");
							array("backtrace");
							{
								// for line in backtrace
								object();
								{
									put("file", "JUnit4TestReference.java");
									put("line", 50);
									put("function", "org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run");
								}
								end();
							}
							endArray();
						}
						end();
					}
					endArray();

					object("context");
					{
						put("url", "http://localhost:8080/sample/");
						put("environment", "test");
						put("rootDirectory", "/sample");
						put("version", "1.0");
					}
					end();

					object("environment");
					{
						// for key, value in environment
						put("java.vm.version", "20.4-b02");
					}
					end();

					object("session");
					{
						// for key, value in session
						put("attr", "value");
						put("creditCardNumber", "[FILTERED]");
					}
					end();

					object("params");
					{
						// for key, value in params
						put("attr", "value");
						put("creditCardNumber", "[FILTERED]");
					}
					end();
				}
				end();
			}
		}.close();
	}
}
