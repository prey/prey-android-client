/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.ReportActionResponse;

public class ResponsesXMLHandler extends DefaultHandler {

	private boolean inMissing;
	private boolean inDelay;
	private boolean inPostUrl;
	private boolean inModules;
	private boolean inModule;
	private boolean inAlertMessage;
	private boolean inUnlockPass;
	private boolean inWebCamMessage;
	private boolean inFormatSim;
	private String alertMessage = "";
	private String webCamMessage = "";
	private String formatSim="";

	private ReportActionResponse actionResponse = new ReportActionResponse();

	public ReportActionResponse getActionResponses() {
		return this.actionResponse;
	}

	@Override
	public void startDocument() throws SAXException {
		this.actionResponse = new ReportActionResponse();
	}

	@Override
	public void endDocument() throws SAXException {
		// Nothing to do
	}

	/**
	 * Gets be called on opening tags like: <tag> Can provide attribute(s), when
	 * xml was like: <tag attribute="attributeValue">
	 * 
	 * <?xml version="1.0" encoding="UTF-8"?> <device> <status>
	 * <missing>true</missing> <device_type>phone</device_type> </status>
	 * <configuration> <delay>10</delay> <auto_update>false</auto_update>
	 * </configuration> <modules> <module name="network" version="1.0"
	 * active="true"/> <module name="session" version="1.0" active="true"/>
	 * <module name="webcam" version="1.0" active="true"/> </modules> </device>
	 * 
	 * 
	 * */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		if (localName.equals("missing")) {
			this.inMissing = true;
		} else if (localName.equals("delay")) {
			this.inDelay = true;
		} else if (localName.equals("post_url")) {
			this.inPostUrl = true;
		} else if (localName.equals("modules")) {
			this.inModules = true;
		} else if (localName.equals("module")) {
			this.inModule = true;
			if (inModules) {
				// Extract an Attribute
				String name = atts.getValue("name");
				String active = atts.getValue("active");

				actionResponse.addAction(name, active);
				PreyLogger.i("name:"+name+" active:"+active);
			}
		}
		if (inModule) {
			if (localName.equals("alert_message")) {
				inAlertMessage = true;
			} else if (localName.equals("unlock_pass")) {
				inUnlockPass = true;
			} else if (localName.equals("webcam_message")){
				inWebCamMessage = true;
			} else if(localName.equals("format_sim")){
				inFormatSim = true;
			}
		}
	}

	/**
	 * Gets be called on closing tags like: </tag>
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		if (localName.equals("missing")) {
			this.inMissing = false;
		} else if (localName.equals("delay")) {
			this.inDelay = false;
		} else if (localName.equals("post_url")) {
			this.inPostUrl = false;
		} else if (localName.equals("modules")) {
			this.inModules = false;
		} else if (localName.equals("module")) {
			this.inModule = false;
		} else if (localName.equals("alert_message")) {
			this.inAlertMessage = false;
			this.alertMessage = "";
		} else if (localName.equals("unlock_pass")) {
			this.inUnlockPass = false;
		} else if (localName.equals("webcam_message")) {
			this.inWebCamMessage = false;
			this.webCamMessage = "";
		} else if (localName.equals("format_sim")){
			this.inFormatSim = false;
			this.formatSim = "";
		}
	}

	/**
	 * Gets be called on the following structure: <tag>characters</tag>
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		
		if (this.inMissing) {
			String isMissing = new String(ch, start, length);
			actionResponse.setMissing(Boolean.valueOf(isMissing));
		} else if (this.inDelay) {
			String delay = new String(ch, start, length);
			actionResponse.setDelay(Long.valueOf(delay));
		} else if (this.inPostUrl) {
			String postUrl = new String(ch, start, length);
			PreyConfig.postUrl = postUrl;
		} else if (this.inAlertMessage) {
			// alert_message
			// change_wallpaper
			alertMessage = alertMessage.concat(new String(ch, start, length));
			actionResponse.addActionConfigParameter("alert", "alert_message", alertMessage);
		} else if (this.inUnlockPass) {
			String unlockPass = new String(ch, start, length);
			actionResponse.addActionConfigParameter("lock", "unlock_pass", unlockPass);
		} else if (this.inWebCamMessage){
			webCamMessage = webCamMessage.concat(new String(ch, start, length));
			actionResponse.addActionConfigParameter("webcam", "webcam_message", webCamMessage);
		} else if (this.inFormatSim){
			formatSim = formatSim.concat(new String(ch, start, length));
			actionResponse.addActionConfigParameter("wipe", "format_sim", formatSim);
		}

	}

}
