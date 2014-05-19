package org.litepal.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This is the content handler for analysis the litepal.xml file by SAXParser,
 * and temporarily the only correct way to generate LitePalAttr model with
 * values.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class LitePalContentHandler extends DefaultHandler {

	/**
	 * Store the parsed value of litepal.xml.
	 */
	private LitePalAttr litePalAttr;

	/**
	 * Characters in the <>characters</> tag. Decide to not use this method
	 * temporarily. Use value attribute instead.
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
	}

	/**
	 * End of the document. Doing nothing temporarily.
	 */
	@Override
	public void endDocument() throws SAXException {
	}

	/**
	 * End of the element. Doing nothing temporarily.
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
	}

	/**
	 * Start of the document. Generate a LitePalAttr model at the same time.
	 */
	@Override
	public void startDocument() throws SAXException {
		litePalAttr = LitePalAttr.getInstance();
	}

	/**
	 * Start analysis the litepal.xml file. Set all the parsed value into the
	 * LitePalAttr model.
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		if (LitePalParser.NODE_DB_NAME.equalsIgnoreCase(localName)) {
			for (int i = 0; i < attributes.getLength(); i++) {
				if (LitePalParser.ATTR_VALUE.equalsIgnoreCase(attributes.getLocalName(i))) {
					litePalAttr.setDbName(attributes.getValue(i).trim());
				}
			}
		} else if (LitePalParser.NODE_VERSION.equalsIgnoreCase(localName)) {
			for (int i = 0; i < attributes.getLength(); i++) {
				if (LitePalParser.ATTR_VALUE.equalsIgnoreCase(attributes.getLocalName(i))) {
					litePalAttr.setVersion(Integer.parseInt(attributes.getValue(i).trim()));
				}
			}
		} else if (LitePalParser.NODE_MAPPING.equalsIgnoreCase(localName)) {
			for (int i = 0; i < attributes.getLength(); i++) {
				if (LitePalParser.ATTR_CLASS.equalsIgnoreCase(attributes.getLocalName(i))) {
					litePalAttr.addClassName(attributes.getValue(i).trim());
				}
			}
		} else if (LitePalParser.NODE_CASES.equalsIgnoreCase(localName)) {
			for (int i = 0; i < attributes.getLength(); i++) {
				if (LitePalParser.ATTR_VALUE.equalsIgnoreCase(attributes.getLocalName(i))) {
					litePalAttr.setCases(attributes.getValue(i).trim());
				}
			}
		}
	}

}
