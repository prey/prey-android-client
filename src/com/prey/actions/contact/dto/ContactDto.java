package com.prey.actions.contact.dto;

import java.util.ArrayList;
import java.util.List;

public class ContactDto {
 
	private int contactId;
	
	private String nickName;
	private String displayName;
	private  List<PhoneDto> phones=new ArrayList<PhoneDto>();
	private List<EmailDto> emails=new ArrayList<EmailDto>();
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public List<PhoneDto> getPhones() {
		return phones;
	}
	public void setPhones(List<PhoneDto> phones) {
		this.phones = phones;
	}
	public List<EmailDto> getEmails() {
		return emails;
	}
	public void setEmails(List<EmailDto> emails) {
		this.emails = emails;
	}
	public int getContactId() {
		return contactId;
	}
	public void setContactId(int contactId) {
		this.contactId = contactId;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public void addPhone(PhoneDto phone){
		phones.add(	phone);
	}
	public void addEmail(EmailDto email){
		emails.add(	email);
	}
	
}
