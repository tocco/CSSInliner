package com.leonty;

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

import com.osbcp.cssparser.CSSParser;
import com.osbcp.cssparser.PropertyValue;
import com.osbcp.cssparser.Rule;

public class CSSInliner {

	public static String inlineCss(String html, String css) throws Exception {
		return inlineCss(html, css, false);
	}
	
	public static String inlineCss(String html, String css, boolean removeClassAttributes) throws Exception {
		Document doc = Jsoup.parse(html);
		HashMap<Element, ArrayList<Selector>> selected = new HashMap<Element, ArrayList<Selector>>();
			
		css += getDocumentStyles(doc);
		extractSelectors(css, doc, selected);		
		
		mergeCss(selected);

		if (removeClassAttributes) {
			removeClassAttributes(doc);
		}

		return doc.html();
	}

	private static void mergeCss(HashMap<Element, ArrayList<Selector>> selected) {
		Iterator<Entry<Element, ArrayList<Selector>>> it = selected.entrySet().iterator();
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
		}
	}

	private static void extractSelectors(String css, Document doc,
			HashMap<Element, ArrayList<Selector>> selected) throws Exception {
		List<Rule> rules = CSSParser.parse(css);

		for (Rule rule : rules) {
			
			for (com.osbcp.cssparser.Selector selector : rule.getSelectors()) {
				
				Elements elements = doc.select(selector.toString());
				
				for (Element element : elements) {
					
					// list of selectors to apply to this element
					ArrayList<Selector> selectors = new ArrayList<Selector>();
					
					// this element already has selectors attached to it - get reference to the list of existing selectors
					if (selected.containsKey(element)) {
						selectors = selected.get(element);
					}
					
					// add new selector containing styles to this dom element
					selectors.add(new Selector(selector.toString(), getStyleString(rule)));
					
					selected.put(element, selectors);
				}
			}
		}
	}
	
	public static String getDocumentStyles(Document doc) {
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
			style += propertyValue.getProperty() + ": " + propertyValue.getValue() + "; ";
		}
		
		return style;
	}

	private static void removeClassAttributes(Document doc) {
		Elements elementsWithClassAttribute = doc.select("[class]");
		for (Element element : elementsWithClassAttribute) {
			element.removeAttr("class");
		}
	}
}
