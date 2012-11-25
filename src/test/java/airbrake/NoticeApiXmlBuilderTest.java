package airbrake;

import static airbrake.ApiKeys.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.*;

public class NoticeApiXmlBuilderTest {

	private AirbrakeNoticeBuilder productionNoticeBuilder;

	private RuntimeException newThrowable() {
		return new RuntimeException("errorMessage");
	}

	@Before
	public void setUp() {
		productionNoticeBuilder = new AirbrakeNoticeBuilder(API_KEY, newThrowable(), "<blink>production</blink>");
	}

	@Test
	public void testEscapesAngleBrackets() throws Exception {
		assertThat(xml(productionNoticeBuilder.newNotice()), containsString("<blink>production</blink>"));
	}

	@Test
	public void testSendsRequest() throws Exception {
		AirbrakeNoticeBuilder builder = new AirbrakeNoticeBuilder(API_KEY, newThrowable()) {
			{
				setRequest("http://example.com", "carburetor");
			}
		};

		assertThat(xml(builder.newNotice()), containsString("<request><url><![CDATA[http://example.com]]></url>"));
	}

	@Test
	public void testSendsSessionKeyColorAndLights() throws Exception {
		AirbrakeNoticeBuilder builder = new AirbrakeNoticeBuilder(API_KEY, newThrowable()) {
			{
				setRequest("http://example.com", "carburetor");
				addSessionKey("lights", "<blink>");
				addSessionKey("color", "orange");
			}
		};

		assertThat(xml(builder.newNotice()), containsString("<session><var key=\"color\"><![CDATA[orange]]></var>"));
		assertThat(xml(builder.newNotice()), containsString("<var key=\"lights\"><![CDATA[<blink>]]></var></session>"));
	}

	private String xml(AirbrakeNotice notice) {
		NoticeXml noticeApi2 = new NoticeXml(notice);
		return noticeApi2.toString();
	}
}
