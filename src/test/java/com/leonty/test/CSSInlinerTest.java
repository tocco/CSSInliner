package com.leonty.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.leonty.CSSInliner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CSSInlinerTest {

	@Test
	public void test() throws Exception {
		String html = readFile("test.html");
		String css = readFile("test.css");

		String inlinedHtml = readFile("inlined.html");

		//assertEquals(CSSInliner.inlineCss(html, css, true), inlinedHtml);
		assertEquals(normalized(inlinedHtml), normalized(CSSInliner.inlineCss(html, css, true)));
	}

	private String normalized(String text) {
		return text.replaceAll("[\n\r]+", "\n");
	}

	@Test(dataProvider = "selectorPrecedenceData")
	public void testSelectorPrecedence(String css, String initialStyleAttribute, String expectedStyleAttribute) throws Exception {
		String html =
				"<html>\n" +
					"<head><style>\n" + css + "</style></head>\n" +
					"<body>\n" +
						"<a class=\"my-class\" id=\"my-id\" style=\"" + initialStyleAttribute + "\"></a>\n" +
					"</body>\n" +
				"</html>";

		String out = CSSInliner.inlineCss(html, "");

		Document document = Jsoup.parse(out);
		Element a = document.select("a").get(0);

		assertEquals(a.attr("style").trim(), expectedStyleAttribute);
	}

	@DataProvider(name="selectorPrecedenceData")
	public Object[][] getSelectorPrecedenceData() {
		return new Object[][] {
				// no selector
				new Object[] { "", "", "" },

				// single selectors
				new Object[] {
						"a { color: #000; }",
						"",

						"color: #000;"
				},
				new Object[] {
						".my-class { color: #111; }",
						"",

						"color: #111;"
				},
				new Object[] {
						"#my-id { color: #222; }",
						"",

						"color: #222;"
				},

				// class selector precedence over element selector
				new Object[] {
						"a { color: #000; }\n" +
						".my-class { color: #111; }",
						"",

						"color: #000; color: #111;"
				},
				new Object[] {
						".my-class { color: #111; }\n" +
						"a { color: #000; }",
						"",

						"color: #000; color: #111;"
				},

				// id selector precedence over class selector
				new Object[] {
						".my-class { color: #111; }\n" +
						"#my-id { color: #222; }",
						"",

						"color: #111; color: #222;"
				},
				new Object[] {
						"#my-id { color: #222; }\n" +
						".my-class { color: #111; }",
						"",

						"color: #111; color: #222;"
				},

				// initial style attribute precedence over id selector
				new Object[] {
						"#my-id { color: #222; }",
						"color: #333;",

						"color: #222; color: #333;"
				},

				// last rule wins if same selector
				new Object[] {
						"a { color: #000; }\n" +
						"a { color: #111; }",
						"",

						"color: #000; color: #111;"
				},
				new Object[] {
						".my-class { color: #000; }\n" +
						".my-class { color: #111; }",
						"",

						"color: #000; color: #111;"
				},
				new Object[] {
						"#my-id { color: #000; }\n" +
						"#my-id { color: #111; }",
						"",

						"color: #000; color: #111;"
				},
		};
	}

	private String readFile(String name) throws IOException, URISyntaxException {
		URL url = getClass().getResource(name);
		Path resPath = Paths.get(url.toURI());
		return new String(Files.readAllBytes(resPath), "UTF8");
	}
}
