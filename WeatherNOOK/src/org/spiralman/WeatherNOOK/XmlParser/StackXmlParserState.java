package org.spiralman.WeatherNOOK.XmlParser;

import java.text.ParseException;
import java.util.Map;

public class StackXmlParserState {
	public StackXmlParserState startNewTag(String tagName,
			Map<String, String> attributes) throws ParseException {
		return this;
	}

	public void endThisTag() throws ParseException {

	}

	public void text(String text) throws ParseException {

	}
}