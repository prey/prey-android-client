/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.prey.actions.ReportActionResponse;

public class ResponseParser {

	public static ReportActionResponse parseResponse(String responseAsXML) {
		// URL url = new
		// URL("http://www.anddev.org/images/tut/basic/parsingxml/example.xml");

		/*
		 * Get a SAXParser from the SAXPArserFactory.
		 */
		ResponsesXMLHandler responsesHandler = new ResponsesXMLHandler();
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader */

			xr.setContentHandler(responsesHandler);

			ByteArrayInputStream tobeparsed = new ByteArrayInputStream(responseAsXML.getBytes());

			xr.parse(new InputSource(tobeparsed));

			/* Parsing has finished. */

		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* Our ExampleHandler now provides the parsed data to us. */
		return responsesHandler.getActionResponses();
	}

}
