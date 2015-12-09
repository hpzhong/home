package com.zhuoyou.plugin.firmware;

public class Firmware {
	
	private String name;
	private String title;
	private String content;
	private String currentVer;
	private String md5;
	private String fileUrl;
	private String description;
	
	public String getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public String getCurrentVer() {
		return currentVer;
	}

	public String getMd5() {
		return md5;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setCurrentVer(String currentVer) {
		this.currentVer = currentVer;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
