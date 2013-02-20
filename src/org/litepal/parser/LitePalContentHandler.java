package org.litepal.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This is the content handler for analysis the litepal.xml file by SAX, and
 * temporarily the only correct way to generate LitePalAttr model with values.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class LitePalContentHandler extends DefaultHandler {

	/**
	 * Node name dbname.
	 */
	private static final String NODE_DB_NAME = "dbname";

	/**
	 * Node name version.
	 */
	private static final String NODE_VERSION = "version";

	/**
	 * Node name list. Currently not used.
	 */
	private static final String NODE_LIST = "list";

	/**
	 * Node name mapping.
	 */
	private static final String NODE_MAPPING = "mapping";

	/**
	 * Node name column case.
	 */
	private static final String NODE_CASES = "cases";

	/**
	 * Attribute name value, for dbname and version node.
	 */
	private static final String ATTR_VALUE = "value";

	/**
	 * Attribute name class, for mapping node.
	 */
	private static final String ATTR_CLASS = "class";

	/**
	 * Store the parsed database value.
	 */
	private LitePalAttr litePalAttr;

	/**
	 * Characters in the <>characters</> marks. Decide to not use this method
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
		if (NODE_DB_NAME.equalsIgnoreCase(localName)) {
			for (int i = 0; i < attributes.getLength(); i++) {
				if (ATTR_VALUE.equalsIgnoreCase(attributes.getLocalName(i))) {
					litePalAttr.setDbName(attributes.getValue(i).trim());
				}
			}
		} else if (NODE_VERSION.equalsIgnoreCase(localName)) {
			for (int i = 0; i < attributes.getLength(); i++) {
				if (ATTR_VALUE.equalsIgnoreCase(attributes.getLocalName(i))) {
					litePalAttr.setVersion(Integer.parseInt(attributes.getValue(i).trim()));
				}
			}
		} else if (NODE_MAPPING.equalsIgnoreCase(localName)) {
			for (int i = 0; i < attributes.getLength(); i++) {
				if (ATTR_CLASS.equalsIgnoreCase(attributes.getLocalName(i))) {
					litePalAttr.addClassName(attributes.getValue(i).trim());
				}
			}
		} else if (NODE_CASES.equalsIgnoreCase(localName)) {
			for (int i = 0; i < attributes.getLength(); i++) {
				if (ATTR_VALUE.equalsIgnoreCase(attributes.getLocalName(i))) {
					litePalAttr.setCases(attributes.getValue(i).trim());
				}
			}
		}
	}

}
