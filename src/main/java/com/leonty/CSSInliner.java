package com.leonty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector.SelectorParseException;
import org.slf4j.LoggerFactory;

import com.osbcp.cssparser.CSSParser;
import com.osbcp.cssparser.PropertyValue;
import com.osbcp.cssparser.Rule;

public class CSSInliner {
	private static final org.slf4j.Logger LOG = LoggerFactory
			.getLogger(CSSInliner.class);

	public static String inlineCss(String html, String css) throws Exception {
		return inlineCss(html, css, false);
	}

	/** Returns the cleaned html that will be used for inlining css. */
	public static String cleanup(String html) {
		Document doc = Jsoup.parse(html);
		return doc.html();
	}

	public static String inlineCss(String html, String css,
			Boolean removeAttributes) throws Exception {
		Document doc = Jsoup.parse(html);
		HashMap<Element, ArrayList<Selector>> selected = new HashMap<Element, ArrayList<Selector>>();
		css += getInternalStyles(doc);
		extractSelectors(css, doc, selected);
		mergeCss(removeAttributes, selected);
		return doc.html();
	}

	public static String inlineCss(String html, Boolean removeAttributes)
			throws Exception {
		Document doc = Jsoup.parse(html);
		HashMap<Element, ArrayList<Selector>> selected = new HashMap<Element, ArrayList<Selector>>();
		String css = readExternalStyles(doc);
		remvoeExternalStyles(doc);
		css += getInternalStyles(doc);
		extractSelectors(css, doc, selected);
		mergeCss(removeAttributes, selected);
		return doc.html();
	}

	public static String internalCss(String html) throws IOException {
		Document doc = Jsoup.parse(html);
		String css = readExternalStyles(doc);
		css += getInternalStyles(doc);
		remvoeExternalStyles(doc);
		Element head = doc.head();
		head.append("<style type=\"text/css\">\n"+css+"\n</style>");
		return doc.html();
	}

	private static void mergeCss(Boolean removeAttributes,
			HashMap<Element, ArrayList<Selector>> selected) {
		Iterator<Entry<Element, ArrayList<Selector>>> it = selected.entrySet()
				.iterator();
		while (it.hasNext()) {
			Map.Entry<Element, ArrayList<Selector>> pair = it.next();

			Collections.sort(pair.getValue());

			String style = "";
			for (Selector selector : pair.getValue()) {
				style += selector.getStyle();
			}

			String initialStyle = pair.getKey().attr("style");
			if (initialStyle != null && !initialStyle.isEmpty()) {
				style += initialStyle;
			}

			pair.getKey().attr("style", style);

			// if this is used for email we don't need id and style attributes
			// as all of our styles are already inline
			if (removeAttributes) {
				pair.getKey().removeAttr("class");
			}
		}
	}

	private static void extractSelectors(String css, Document doc,
			HashMap<Element, ArrayList<Selector>> selected) throws Exception {
		extractSelectors(css, doc, selected, false);
	}

	private static void extractSelectors(String css, Document doc,
			HashMap<Element, ArrayList<Selector>> selected, boolean debug)
			throws Exception {
		LOG.trace("extract selectors from [{}]", css);
		List<Rule> rules = CSSParser.parse(css);
		ArrayList<com.osbcp.cssparser.Selector> ignored = new ArrayList<com.osbcp.cssparser.Selector>();
		for (Rule rule : rules) {
			for (com.osbcp.cssparser.Selector selector : rule.getSelectors()) {
				Try<Elements> validSelector = select(doc, selector.toString());
				if (validSelector.isDefined()) {
					Elements elements = validSelector.get();
					for (Element element : elements) {

						// list of selectors to apply to this element
						ArrayList<Selector> selectors = new ArrayList<Selector>();

						// this element already has selectors attached to it
						// -
						// get
						// reference to the list of existing selectors
						if (selected.containsKey(element)) {
							selectors = selected.get(element);
						}

						// add new selector containing styles to this dom
						// element
						selectors.add(new Selector(selector.toString(),
								getStyleString(rule)));

						selected.put(element, selectors);
					}
				} else {
					ignored.add(selector);
					if (debug)
						LOG.warn("Couldn't select with [" + selector.toString()
								+ "]", validSelector.failure());
				}
			}
		}
		LOG.warn(
				"{} selectors where ignored while {} where returned. Switch to trace to get a list.",
				ignored.size(), selected.size());
		LOG.trace("The following selectors where ignored {}", ignored);
	}

	private static Try<Elements> select(Document doc, String selector) {
		try {
			return Try.success(doc.select(selector));
		} catch (SelectorParseException e) {
			return Try.failure(e);
		}
	}

	private static String readExternalStyles(Document doc) throws IOException {
		Elements elements = selectExternalStyle(doc);
		StringBuilder styles = new StringBuilder();
		for (Element style : elements) {
			String url = style.attr("href");
			LOG.info("Download external css from [{}]", url);
			String cssContent = Jsoup.connect(url).execute().body();
			styles.append("/*external css from [").append(url).append("]*/\n").append(cssContent).append("\n");
		}
		return styles.toString();
	}

	private static void remvoeExternalStyles(Document doc) {
		Elements elements = selectExternalStyle(doc);
		elements.remove();
	}

	private static Elements selectExternalStyle(Document doc) {
		return doc
				.select("link[rel=stylesheet],link[type=text/css]");
	}

	public static String getInternalStyles(Document doc) throws IOException {
		Elements elements = doc.select("style");

		String styles = "";
		for (Element style : elements) {
			styles += style.html();
		}

		// we don't need style elements anymore
		elements.remove();
		return styles;
	}

	public static String getStyleString(Rule rule) {
		String style = "";

		for (PropertyValue propertyValue : rule.getPropertyValues()) {
			style += propertyValue.getProperty() + ": "
					+ propertyValue.getValue() + "; ";
		}

		return style;
	}
}
