package com.zhuoyou.plugin.base;

import java.util.Map;

import android.graphics.drawable.Drawable;

public interface PlugInterface {
	/**
	 * @name :getSupportCmd
	 * 		��ȡ�ò��֧�ֵ�����.����ľ��嶨��,��������|����
	 * EX: 20|21
	 */
	public String getSupportCmd();
	
	/**
	 * @name getName
	 * 		�õ��ò��������
	 */
	public String getName();
	
	/**
	 * @name getIcon
	 * 		�õ��ò����logo
	 */
	public Drawable getIcon();
	
	/**
	 * @name getEntryMethodName
	 * 		�õ��ò������ں���,���û���¼�����Ļ�,����null
	 * 		���ﶨ�����activity��action.�ǵø�manifest��Ҫͬ��.
	 */
	public String getEntryMethodName();
	
	/**
	 * @name getWorkMethodName
	 * 		�Ѷ�Ӧ�������ִ�к���
	 */
	public Map<String , String> getWorkMethodName();
}
