package io.airbrake;

import org.junit.*;

public class AirbrakeTest {

	private Airbrake airbrake;

	@Before
	public void setUp() {
		airbrake = new Airbrake("5f658d2b3713f8b26fe5ed698fd3710c", "84971");
	}

	@Test
	public void shouldNotify() {
		airbrake.notify(new RuntimeException("errore"));
	}
}
