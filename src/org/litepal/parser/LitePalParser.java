package org.litepal.parser;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.litepal.LitePalApplication;
import org.litepal.exceptions.ParseConfigurationFileException;
import org.litepal.util.Const;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.res.AssetManager;
import android.content.res.Resources.NotFoundException;

/**
 * The class is used to parse the litepal.xml file. There're two usual ways to
 * parse XML in android, SAX and DOM. LitePal use SAX as default option, and DOM
 * parser will be added soon.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class LitePalParser {

	/**
	 * Use the SAX way to parse the litepal.xml file. It will get the parsed
	 * result from LitePalContentHandler and stored in the instance of
	 * LitePalAttr.
	 * 
	 * Note while analyzing the litepal.xml file, There could be a lot of
	 * exceptions happen, be careful of writing the litepal.xml file and user
	 * should write their try catch codes well.
	 * 
	 * @throws ParseConfigurationFileException
	 */
	public static void parseBySAX() {
		LitePalContentHandler handler = null;
		try {
			AssetManager assetManager = LitePalApplication.getContext().getAssets();
			String[] fileNames = assetManager.list("");
			if (fileNames != null && fileNames.length > 0) {
				for (String fileName : fileNames) {
					if (Const.LitePal.CONFIGURATION_FILE_NAME.equalsIgnoreCase(fileName)) {
						SAXParserFactory factory = SAXParserFactory.newInstance();
						XMLReader xmlReader = factory.newSAXParser().getXMLReader();
						handler = new LitePalContentHandler();
						xmlReader.setContentHandler(handler);
						xmlReader.parse(new InputSource(assetManager.open(fileName,
								AssetManager.ACCESS_BUFFER)));
						return;
					}
				}
				throw new ParseConfigurationFileException(
						ParseConfigurationFileException.CAN_NOT_FIND_LITEPAL_FILE);
			} else {
				throw new ParseConfigurationFileException(
						ParseConfigurationFileException.CAN_NOT_FIND_LITEPAL_FILE);
			}
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
}
