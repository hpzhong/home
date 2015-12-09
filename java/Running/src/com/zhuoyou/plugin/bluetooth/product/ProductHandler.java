package com.zhuoyou.plugin.bluetooth.product;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ProductHandler extends DefaultHandler {

	private static final String GRANDPA_ROOT = "grandpa";
	private static final String GRANDPA_ATTR_MODIFYTIME = "modify_time";
	private static final String FATHER_ROOT = "father";
	private static final String FATHER_ATTR_NAME = "name";
	private static final String FATHER_ATTR_TYPE = "type";
	private static final String SON_ROOT = "son";
	private static final String SON_ATTR_PACKAGENAME = "package_name";

	private Grandpa mGrandPa = null;
	private Father mFather = null;
	private Son mSon = null;
	private StringBuilder builder;

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		mGrandPa = new Grandpa();
		builder = new StringBuilder();
	}

	public Grandpa getRoot(){
		return mGrandPa;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equals(GRANDPA_ROOT)) {
			String time = attributes.getValue(GRANDPA_ATTR_MODIFYTIME);
			mGrandPa.setModifyTime(time);
		} else if (localName.equals(FATHER_ROOT)) {
			mFather = new Father();
			String name = attributes.getValue(FATHER_ATTR_NAME);
			mFather.setName(name);
			String type = attributes.getValue(FATHER_ATTR_TYPE);
			mFather.setCategory(type);
		} else if (localName.equals(SON_ROOT)) {
			mSon = new Son();
			String packagename = attributes.getValue(SON_ATTR_PACKAGENAME);
			mSon.setPackageName(packagename);
			mSon.setProductName(mFather.getName());
		}
		builder.setLength(0);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		builder.append(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		if (localName.equals(SON_ROOT)) {
			mFather.addSon(mSon);
		}else if(localName.equals(FATHER_ROOT)){
			mGrandPa.addFather(mFather);
		}
	}

}
