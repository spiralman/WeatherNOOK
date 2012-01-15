package org.spiralman.WeatherNOOK.XmlParser;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class StackXmlParser {
	public static final int PARSING_PROGRESS = 0;

	private XmlPullParserFactory m_xmlFactory = null;

	public void parseXml(Reader xml, StackXmlParserState baseState)
			throws IOException, XmlPullParserException, ParseException {
		if (m_xmlFactory == null) {
			m_xmlFactory = XmlPullParserFactory.newInstance();
		}

		XmlPullParser parser = m_xmlFactory.newPullParser();

		parser.setInput(xml);

		Stack<StackXmlParserState> states = new Stack<StackXmlParserState>();
		states.push(baseState);

		int xmlEvent = parser.getEventType();
		while (xmlEvent != XmlPullParser.END_DOCUMENT) {
			StackXmlParserState current = states.peek();

			switch (xmlEvent) {
			case XmlPullParser.START_TAG:
				HashMap<String, String> attributes = new HashMap<String, String>();
				for (int i = 0; i < parser.getAttributeCount(); ++i) {
					attributes.put(parser.getAttributeName(i),
							parser.getAttributeValue(i));
				}
				states.push(current.startNewTag(parser.getName(), attributes));
				break;
			case XmlPullParser.END_TAG:
				states.pop();
				if (current != states.peek()) {
					current.endThisTag();
				}
				break;
			case XmlPullParser.TEXT:
				current.text(parser.getText());
				break;
			}

			xmlEvent = parser.next();
		}
	}
}
