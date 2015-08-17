/*
 * Copyright (C)  Tony Green, LitePal Framework Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litepal.parser;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.litepal.LitePalApplication;
import org.litepal.exceptions.ParseConfigurationFileException;
import org.litepal.util.Const;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.AssetManager;
import android.content.res.Resources.NotFoundException;

/**
 * The class is used to parse the litepal.xml file. There're three usual ways to
 * parse XML in android, SAX, Pull and DOM. LitePal use SAX as default option,
 * and DOM parser will be added soon.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class LitePalParser {

	/**
	 * Node name dbname.
	 */
	static final String NODE_DB_NAME = "dbname";

	/**
	 * Node name version.
	 */
	static final String NODE_VERSION = "version";

	/**
	 * Node name list. Currently not used.
	 */
	static final String NODE_LIST = "list";

	/**
	 * Node name mapping.
	 */
	static final String NODE_MAPPING = "mapping";

	/**
	 * Node name column case.
	 */
	static final String NODE_CASES = "cases";

	/**
	 * Attribute name value, for dbname and version node.
	 */
	static final String ATTR_VALUE = "value";

	/**
	 * Attribute name class, for mapping node.
	 */
	static final String ATTR_CLASS = "class";

	/**
	 * Store the parsed value of litepal.xml.
	 */
	private static LitePalParser parser;

	/**
	 * Analyze litepal.xml, and store the analyzed result in LitePalParser. Use
	 * DomParse to parse the configuration file as default. SAXParser and
	 * XmlPullParser is also optional, but not visible to developers.
	 */
	public static void parseLitePalConfiguration() {
		if (parser == null) {
			parser = new LitePalParser();
		}
		parser.useSAXParser();
	}

	/**
	 * Use SAXParser to parse the litepal.xml file. It will get the parsed
	 * result from LitePalContentHandler and stored in the instance of
	 * LitePalAttr.
	 * 
	 * Note while analyzing litepal.xml file, ParseConfigurationFileException
	 * could be thrown. Be careful of writing litepal.xml file, or developer's
	 * application may be crash.
	 */
	void useSAXParser() {
		LitePalContentHandler handler = null;
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			XMLReader xmlReader = factory.newSAXParser().getXMLReader();
			handler = new LitePalContentHandler();
			xmlReader.setContentHandler(handler);
			xmlReader.parse(new InputSource(getConfigInputStream()));
			return;
		} catch (NotFoundException e) {
			throw new ParseConfigurationFileException(
					ParseConfigurationFileException.CAN_NOT_FIND_LITEPAL_FILE);
		} catch (SAXException e) {
			throw new ParseConfigurationFileException(
					ParseConfigurationFileException.FILE_FORMAT_IS_NOT_CORRECT);
		} catch (ParserConfigurationException e) {
			throw new ParseConfigurationFileException(
					ParseConfigurationFileException.PARSE_CONFIG_FAILED);
		} catch (IOException e) {
			throw new ParseConfigurationFileException(ParseConfigurationFileException.IO_EXCEPTION);
		}
	}

	/**
	 * Use XmlPullParser to parse the litepal.xml file. It will store the result
	 * in the instance of LitePalAttr.
	 * 
	 * Note while analyzing litepal.xml file, ParseConfigurationFileException
	 * could be thrown. Be careful of writing litepal.xml file, or developer's
	 * application may be crash.
	 */
	void usePullParse() {
		try {
			LitePalAttr litePalAttr = LitePalAttr.getInstance();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlPullParser = factory.newPullParser();
			xmlPullParser.setInput(getConfigInputStream(), "UTF-8");
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String nodeName = xmlPullParser.getName();
				switch (eventType) {
				case XmlPullParser.START_TAG: {
					if (NODE_DB_NAME.equals(nodeName)) {
						String dbName = xmlPullParser.getAttributeValue("", ATTR_VALUE);
						litePalAttr.setDbName(dbName);
					} else if (NODE_VERSION.equals(nodeName)) {
						String version = xmlPullParser.getAttributeValue("", ATTR_VALUE);
						litePalAttr.setVersion(Integer.parseInt(version));
					} else if (NODE_MAPPING.equals(nodeName)) {
						String className = xmlPullParser.getAttributeValue("", ATTR_CLASS);
						litePalAttr.addClassName(className);
					} else if (NODE_CASES.equals(nodeName)) {
						String cases = xmlPullParser.getAttributeValue("", ATTR_VALUE);
						litePalAttr.setCases(cases);
					}
					break;
				}
				default:
					break;
				}
				eventType = xmlPullParser.next();
			}
		} catch (XmlPullParserException e) {
			throw new ParseConfigurationFileException(
					ParseConfigurationFileException.FILE_FORMAT_IS_NOT_CORRECT);
		} catch (IOException e) {
			throw new ParseConfigurationFileException(ParseConfigurationFileException.IO_EXCEPTION);
		}
	}

	/**
	 * Iterates all files in the root of assets folder. If find litepal.xml,
	 * open this file and return the input stream. Or throw
	 * ParseConfigurationFileException.
	 * 
	 * @return The input stream of litepal.xml.
	 * @throws java.io.IOException
	 */
	private InputStream getConfigInputStream() throws IOException {
		AssetManager assetManager = LitePalApplication.getContext().getAssets();
		String[] fileNames = assetManager.list("");
		if (fileNames != null && fileNames.length > 0) {
			for (String fileName : fileNames) {
				if (Const.LitePal.CONFIGURATION_FILE_NAME.equalsIgnoreCase(fileName)) {
					return assetManager.open(fileName, AssetManager.ACCESS_BUFFER);
				}
			}
		}
		throw new ParseConfigurationFileException(
				ParseConfigurationFileException.CAN_NOT_FIND_LITEPAL_FILE);
	}
}
