package com.leonty.test;

import java.io.File;
import java.util.Scanner;

import com.leonty.CSSInliner;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class CSSInlinerTest {

	@Test
	public void test() throws Exception {
		String html = new Scanner(new File("test.html")).useDelimiter("\\Z").next();
		String css = new Scanner(new File("test.css")).useDelimiter("\\Z").next();
		
		
		String inlinedHtml = new Scanner(new File("inlined.html")).useDelimiter("\\Z").next();
		assertEquals(inlinedHtml, CSSInliner.inlineCss(html, css, true));
	}

}
