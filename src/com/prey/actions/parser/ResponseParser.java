/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
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
	 	StringBuffer sb = new StringBuffer();
		sb.append("<device>");
		sb.append("<status>");
		sb.append("<missing>true</missing>");
		sb.append("</status>");
		sb.append("<configuration>");
		sb.append("<current_release>0.5.3</current_release>");
		sb.append("<delay>20</delay>");
		sb.append("<post_url>http://control.preyproject.com/devices/u3zqay/reports.xml</post_url>");
		sb.append("</configuration>");
		sb.append("<modules>");
		sb.append("<module type=\"action\" active=\"true\" name=\"system\" version=\"1.5\"/>");
		sb.append("<module type=\"report\" active=\"true\" name=\"geo\" version=\"1.6\">");
		sb.append("<accuracy>max</accuracy>");
		sb.append(" </module>");
		sb.append("<module type=\"report\" active=\"true\" name=\"network\" version=\"1.5\"/>");
		sb.append("<module type=\"report\" active=\"true\" name=\"webcam\" version=\"1.6\">");
		sb.append("<webcam_message>greets the camera</webcam_message>");
		sb.append("</module>");
		sb.append("<module type=\"action\" active=\"true\" name=\"alarm\" version=\"1.5\"/>");
		sb.append("<module type=\"action\" active=\"true\" name=\"alert\" version=\"1.6\">");
		sb.append(" <alert_message>This device is stolen property, and your movements are currently being tracked. Please contact the owner at user@forkhq.com to resolve the situation. He's willing to give you 100 USD for its safe return.</alert_message>");
		sb.append(" </module>");
		
		sb.append("<module type=\"action\" active=\"true\" name=\"lock\" version=\"2.6\">");
		sb.append("<unlock_pass>password</unlock_pass>");
		sb.append("</module>");
	    
	    
		sb.append("  </modules>");
		sb.append("</device>");
		responseAsXML=sb.toString();
		*/
		 
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

			ByteArrayInputStream tobeparsed = new ByteArrayInputStream(
					responseAsXML.getBytes("UTF-8"));

			InputSource is = new InputSource(tobeparsed);
			is.setEncoding("UTF-8");
			xr.parse(is);

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
