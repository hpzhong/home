package com.zhuoyou.plugin.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ActionPannelItemInfo implements Serializable{

	//	--pannelTitle	String	板块标题
	private String mPannelTitle;

	private List<ActParagraph> mActParagraph; 	
	
	public ActionPannelItemInfo(){
		mActParagraph = new ArrayList();
	}
	
	public ActionPannelItemInfo(String pannelTitle){
		mPannelTitle =pannelTitle;
		mActParagraph = new ArrayList();
	}
	
	public void SetPannelTitle(String mtitle){
		mPannelTitle = mtitle;
	}
	
	public String GetPannelTitle(){
		return mPannelTitle;
	}
	
	public void AddPannelParagraph(ActParagraph actparagraph){
		mActParagraph.add(actparagraph);
	}
	
	public void SetPannelParagraph(List<ActParagraph> actparagraphlist){
		mActParagraph = actparagraphlist;
	}
	
	public List<ActParagraph> GetActParagraphList(){
		return mActParagraph;
	}
	
}
