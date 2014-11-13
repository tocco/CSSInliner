#CSSInliner - Utility to inline css styles in html document

By Leonti Bielski

##Code examples

Css will be merged with provided html:
	String inlinedHtml = CSSInliner.inlineCss(html, css);

If used for email, 'class' attributes can be removed:
	String inlinedHtml = CSSInliner.inlineCss(html, css, true);

##Build

The dependency com.osbcp.cssparser:cssparser:1.4 is not available
in maven central. Deploy the jar from lib/osbcp-css-parser-1.4.jar
to your own maven repository so that it fits the dependency
declaration in the pom.

##Changes

v1.0 2012-02-05 Leonti Bielski

* Initial release

##License

CSSInliner
Copyright 2012 Leonti Bielski, leonti.me

Licensed under the Apache License, Version 2.0


