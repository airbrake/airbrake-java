package airbrake;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.*;

public class AirbrakeNoticeBuilderTest {

	@Test
	public void testBuildNoticeErrorClass() {
		AirbrakeNoticeBuilder builder = new AirbrakeNoticeBuilder("apiKey", new RuntimeException("errorMessage"));
		AirbrakeNotice notice = builder.newNotice();
		assertThat(notice.errorClass(), is(equalTo("java.lang.RuntimeException")));
	}

	@Test
	public void testErrorClass() {
		AirbrakeNoticeBuilder builder = new AirbrakeNoticeBuilder("apiKey", new RuntimeException("errorMessage"));
		assertTrue(builder.errorClassIs("java.lang.RuntimeException"));
	}
}
