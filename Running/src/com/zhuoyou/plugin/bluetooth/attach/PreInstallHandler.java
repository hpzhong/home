package com.zhuoyou.plugin.bluetooth.attach;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PreInstallHandler extends DefaultHandler {

	private static final String PRODUCCT_ROOT = "product";
	private static final String PRODUCCT_ATTR_NAME = "name";
	private static final String PRODUCCT_ATTR_TYPE = "type";
	private static final String PLUGIN_ROOT = "plugin";
	private static final String PLUGIN_ATTR_PACKAGENAME = "package_name";
	private static final String PLUGIN_ATTR_NICKNAME = "name";

	private List<PreInstallBean> mPreInstallBeans = null;
	private PreInstallBean mPreInstallBean = null;
	private String mPackageName = null;
	private String mNickName = null;
	private StringBuilder builder;

	public List<PreInstallBean> getRoot() {
		return mPreInstallBeans;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		mPreInstallBeans = new ArrayList<PreInstallBean>();
		builder = new StringBuilder();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equals(PRODUCCT_ROOT)) {
			mPreInstallBean = new PreInstallBean();
			String name = attributes.getValue(PRODUCCT_ATTR_NAME);
			mPreInstallBean.setName(name);
			String type = attributes.getValue(PRODUCCT_ATTR_TYPE);
			mPreInstallBean.setCategory(type);
		} else if (localName.equals(PLUGIN_ROOT)) {
			mPackageName = attributes.getValue(PLUGIN_ATTR_PACKAGENAME);
			mNickName = attributes.getValue(PLUGIN_ATTR_NICKNAME);
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
		if (localName.equals(PLUGIN_ROOT)) {
			mPreInstallBean.addPlugName(mNickName);
			mPreInstallBean.addPlugPackageName(mPackageName);
		} else if (localName.equals(PRODUCCT_ROOT)) {
			mPreInstallBeans.add(mPreInstallBean);
		}
	}
}
