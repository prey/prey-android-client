package com.prey.actions.file;

import java.util.List;

public class FilePrey {

	private String filename;
	private List<FilePrey> childs;
	private String path;
	private long size;
	private String mimetype;
	private int date;
	private boolean file;
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public List<FilePrey> getChilds() {
		return childs;
	}
	public void setChilds(List<FilePrey> childs) {
		this.childs = childs;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getMimetype() {
		return mimetype;
	}
	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}
	public int getDate() {
		return date;
	}
	public void setDate(int date) {
		this.date = date;
	}
	public boolean isFile() {
		return file;
	}
	public void setFile(boolean file) {
		this.file = file;
	}
 
	
	
}
