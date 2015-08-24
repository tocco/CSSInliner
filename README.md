#CSSInliner - Utility to inline css styles in html document

By Leonti Bielski

##Code examples

Css will be merged with provided html:
	String inlinedHtml = CSSInliner.inlineCss(html, css);

If used for email, 'class' attributes can be removed:
	String inlinedHtml = CSSInliner.inlineCss(html, css, true);

##Build

The dependency com.osbcp:cssparser:1.5 is now available
in maven central.

##Other similar libraries
- CSS parsers
	- [https://github.com/phax/ph-css](https://github.com/phax/ph-css)
	- [http://stackoverflow.com/questions/1513587/looking-for-a-css-parser-in-java](http://stackoverflow.com/questions/1513587/looking-for-a-css-parser-in-java)
	- [http://sourceforge.net/p/cssparser/](http://sourceforge.net/p/cssparser/)
- Other resources
	- [inline vs internal vs external css](https://vineetgupta22.wordpress.com/2011/07/09/inline-vs-internal-vs-external-css/)
	
##Changes

v1.0 2012-02-05 Leonti Bielski

* Initial release

##License

CSSInliner
Copyright 2012 Leonti Bielski, leonti.me

Licensed under the Apache License, Version 2.0

