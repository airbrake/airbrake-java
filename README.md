Airbrake Java
=============

This is the notifier jar for integrating apps with [Airbrake](http://airbrake.io) with Java Applications. Sign up for a [Free](https://airbrake.io/account/new/Free) or [Paid](https://airbrake.io/account/new?source=github) account.

When an uncaught exception occurs, Airbrake will POST the relevant data
to the Airbrake server specified in your environment.

The easy way to use airbrake is configuriong log4j appender. Otherwise if you don't 
use log4j you can use airbrake notifier directly with a very simple API.

Setting up with Maven
---------------------

	<project>
  		<dependencies>
    		<dependency>
      		<groupId>io.airbrake</groupId>
      		<artifactId>airbrake-java</artifactId>
      		<version>2.2.3/version>
    		</dependency>
  		</dependencies>
	</project>

Without Maven
-------------

you need to add these libraries to your classpath
 * [airbrake-java-2.2.3](https://github.com/airbrake/airbrake-java/blob/master/maven2/io/airbrake/airbrake-java/2.2.3/airbrake-java-2.2.3.jar?raw=true)
 * [log4j-1.2.14](https://github.com/airbrake/airbrake-java/blob/master/maven2/log4j/1.2.14/log4j-1.2.14.jar?raw=true)

Log4j
-----

	log4j.rootLogger=INFO, stdout, airbrake

	log4j.appender.stdout=org.apache.log4j.ConsoleAppender
	log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
	log4j.appender.stdout.layout.ConversionPattern=[%d,%p] [%c{1}.%M:%L] %m%n

	log4j.appender.airbrake=airbrake.AirbrakeAppender	
	log4j.appender.airbrake.api_key=YOUR_AIRBRAKE_API_KEY
	#log4j.appender.airbrake.env=development
	#log4j.appender.airbrake.env=production
	log4j.appender.airbrake.env=test
	log4j.appender.airbrake.enabled=true
  #log4j.appender.airbrake.noticesUrl=http://api.airbrake.io/notifier_api/v2/notices

or in XML format:

	<appender name="AIRBRAKE" class="airbrake.AirbrakeAppender">
		<param name="api_key" value="YOUR_AIRBRAKE_API_KEY"/>
		<param name="env" value="test"/>
		<param name="enabled" value="true"/>
    <!-- <param name="noticesUrl" value="http://api.airbrake.io/notifier_api/v2/notices" /> -->
	</appender>

	<root>
		<appender-ref ref="AIRBRAKE"/>
	</root>

Directly
------------------------------

	try {
  		doSomethingThatThrowAnException();
	}
	catch(Throwable t) {
  		AirbrakeNotice notice = new AirbrakeNoticeBuilder(YOUR_AIRBRAKE_API_KEY, t, "env").newNotice();
  		AirbrakeNotifier notifier = new AirbrakeNotifier();
  		notifier.notify(notice);
	}

if you need to specifiy a different url to send notice you can create new notifier with this url:

	try {
  		doSomethingThatThrowAnException();
	}
	catch(Throwable t) {
  		AirbrakeNotice notice = new AirbrakeNoticeBuilder(YOUR_AIRBRAKE_API_KEY, t, "env").newNotice();
  		AirbrakeNotifier notifier = new AirbrakeNotifier("http://api.airbrake.io/notifier_api/v2/notices");
  		notifier.notify(notice);
	}


	

Support
-------

For help with using Airbrake and this notifier visit [our support site](http://help.airbrake.io).

For SSL verification see the [Resources](https://github.com/airbrake/airbrake/blob/master/resources/README.md).

For any issues, please post then in our [Issues](https://github.com/airbrake/airbrake-java/issues).


