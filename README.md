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
  		<repositories>
    		<repository>
      		<id>airbrake-repository</id>
      		<name>Airbrake Repository</name>
      		<url>http://github.com/airbrake/airbraje-java/maven2</url>
    		</repository>
 		</repositories>
  		<dependencies>
    		<dependency>
      		<groupId>airbrake</groupId>
      		<artifactId>airbrake-java</artifactId>
      		<version>2.2</version>
    		</dependency>
  		</dependencies>
	</project>

Without Maven
-------------

you need to add these libraries to your classpath
 * [airbrake-java-2.2](http://github.com/airbrake/airbrake-java/maven2/airbrake/airbrake-java/2.2/airbrake-java-2.2.jar)
 * [log4j-1.2.14](http://repo1.maven.org/maven2/log4j/log4j/1.2.14/log4j-1.2.14.jar)

Log4j
-----

	log4j.rootLogger=INFO, stdout, airbrake

	log4j.appender.stdout=org.apache.log4j.ConsoleAppender
	log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
	log4j.appender.stdout.layout.ConversionPattern=[%d,%p] [%c{1}.%M:%L] %m%n

	log4j.appender.airbrake=code.lucamarrocco.airbrake.AirbrakeAppender	
	log4j.appender.airbrake.api_key=YOUR_HOPTOAD_API_KEY
	#log4j.appender.airbrake.env=development
	#log4j.appender.airbrake.env=production
	log4j.appender.airbrake.env=test
	log4j.appender.airbrake.enabled=true


Directly calling Airbrake-Java
------------------------------

	try {
  		doSomethingThatThrowAnException();
	}
	catch(Throwable t) {
  		AirbrakeNotice notice = new AirbrakeNoticeBuilder(YOUR_HOPTOAD_API_KEY, t, "env").newNotice();
  		AirbrakeNotifier notifier = new AirbrakeNotifier();
  		notifier.notify(notice);
	}
	

Support
-------

For help with using Airbrake and this notifier visit [our support site](http://help.airbrake.io).

For SSL verification see the [Resources](https://github.com/airbrake/airbrake/blob/master/resources/README.md).


