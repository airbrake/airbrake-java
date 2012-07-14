package airbrake;

import static airbrake.ApiKeys.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.*;

public class NoticeApi2XmlBuilderTest {

	private String xml(AirbrakeNotice notice) {
		NoticeApi2 noticeApi2 = new NoticeApi2(notice);
		return noticeApi2.toString();
	}

	private AirbrakeNoticeBuilder productionNoticeBuilder;

	@Test
	public void testEscapesAngleBrackets() throws Exception {
		assertThat(xml(productionNoticeBuilder.newNotice()), containsString("&lt;blink&gt;production&lt;/blink&gt;"));
	}

	@Test
	public void testSendsRequest() throws Exception {
		AirbrakeNoticeBuilder builder = new AirbrakeNoticeBuilder(TEST_API_KEY, newThrowable()) {
			{
				setRequest("http://example.com", "carburetor");
			}
		};

		assertThat(xml(builder.newNotice()), containsString("<request><url>http://example.com</url>"));
	}

	@Test
	public void testSendsSessionKeyColorAndLights() throws Exception {
		AirbrakeNoticeBuilder builder = new AirbrakeNoticeBuilder(TEST_API_KEY, newThrowable()) {
			{
				setRequest("http://example.com", "carburetor");
				addSessionKey("lights", "<blink>");
				addSessionKey("color", "orange");
			}
		};

		assertThat(xml(builder.newNotice()), containsString("<session><var key=\"color\">orange</var>"));
		assertThat(xml(builder.newNotice()), containsString("<var key=\"lights\">&lt;blink&gt;</var></session>"));
	}

	private RuntimeException newThrowable() {
		return new RuntimeException("errorMessage");
	}

	@Before
	public void setUp() {
		productionNoticeBuilder = new AirbrakeNoticeBuilder(TEST_API_KEY, newThrowable(), "<blink>production</blink>");
	}
}
