package com.zhuoyou.plugin.action;

public class ActParagraph {
	//	----description	String	描述
	private String mDescription;
	//	----paragraphNum	Int	段落号
	private int mParagraphNum;
	//	----img	String 	图片
	private String mImgUrl;
	//	----content	String	段落内容
	private String mContent;

	public ActParagraph(String description,int paragraphnum,String imgUrl,String content){
		mDescription = description;
		mParagraphNum = paragraphnum;
		mImgUrl = imgUrl;
		mContent = content;
	}
	
	/*
	 *get pannel content description 
	 */
	public String GetDescription(){
		return mDescription;
	}
	
	/*
	 * get paragraph Num
	 */
	public int GetParagraphNum(){
		return mParagraphNum;
	}
	
	/*
	 *get img url 	
	 */
	public String GetImgUrl(){
		return mImgUrl;
	}
	
	/*
	 * get content
	 */
	public String GetContent(){
		return mContent;
	}		
	
}
