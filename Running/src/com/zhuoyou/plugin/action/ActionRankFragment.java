package com.zhuoyou.plugin.action;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

public class ActionRankFragment extends Fragment {
	private View mView;
	
	private TextView noRank;

	private TextView myRank_count;
	
	private TextView myName;
	
	private TextView myStep;
	
	private ImageView myHead;
	
	private ListView listView;
	
	private Bitmap bmp = null;
	
	private Typeface mNumberTP;

	private List<ActionRankingItemInfo> rank =new ArrayList<ActionRankingItemInfo>();
	
	private ActionRankingItemInfo myRank=new ActionRankingItemInfo();

	private Context mContext = RunningApp.getInstance().getApplicationContext();
	private ActionInfo mActionInfo;

	public ActionRankFragment(){

	}
	
	public ActionRankFragment(ActionInfo actioninfo){
		mActionInfo = actioninfo;
		if(mActionInfo != null){
			rank = mActionInfo.getRankList();
			myRank = mActionInfo.getMyRankInfo();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.action_rank_fragment, container,false);
		initView();
		return mView;
	}

	public void initView() {
		noRank=(TextView) mView.findViewById(R.id.noRecordRank);
		myRank_count=(TextView) mView.findViewById(R.id.my_rank);
		myStep=(TextView) mView.findViewById(R.id.my_step);
		myName=(TextView) mView.findViewById(R.id.my_name);
		myHead=(ImageView) mView.findViewById(R.id.my_icon);
		Log.i("zoujian", myRank+"myRank");
		if(myRank != null && myRank.GetSteps()!=0){
			mNumberTP = RunningApp.getCustomNumberFont();
			noRank.setVisibility(View.GONE);
			myRank_count.setVisibility(View.VISIBLE);
			myStep.setVisibility(View.VISIBLE);
			myName.setVisibility(View.VISIBLE);
			myHead.setVisibility(View.VISIBLE);
			myRank_count.setText(myRank.GetCount()+"");
			myStep.setText(myRank.GetSteps()+"");
			myName.setText(myRank.GetName()+"");
			int headIndex = Tools.getHead(mContext);
			if (headIndex == 10000) {
				bmp = Tools.convertFileToBitmap("/Running/download/custom");
				myHead.setImageBitmap(bmp);
			} else if (headIndex == 1000) {
				bmp = Tools.convertFileToBitmap("/Running/download/logo");
				myHead.setImageBitmap(bmp);
			} else {
				myHead.setImageResource(Tools.selectByIndex(headIndex));
			}
			myRank_count.setTypeface(mNumberTP);
			myStep.setTypeface(mNumberTP);
			myRank_count.setTextSize(23f);
			myStep.setTextSize(23f);
			myRank_count.setTextColor(0xff97918f);
			myStep.setTextColor(0xff97918f);
		}else{
			noRank.setVisibility(View.VISIBLE);
			myRank_count.setVisibility(View.GONE);
			myStep.setVisibility(View.GONE);
			myName.setVisibility(View.GONE);
			myHead.setVisibility(View.GONE);
			
		}
		if(rank != null&&rank.size()>0){
			listView = (ListView) mView.findViewById(R.id.action_rank_listview);
			listView.setAdapter(new ListViewAdatper(mContext, rank, listView));
		}
	}

//	public void initDate() {
//		ActionRankingItemInfo item = new ActionRankingItemInfo(1, "1", 200, 1,"aa", "22");
//		ActionRankingItemInfo item1 = new ActionRankingItemInfo(2, "2", 300, 2,"bb", "22r");
//		ActionRankingItemInfo item2 = new ActionRankingItemInfo(3, "3", 400, 3,"cc", "22t");
//		ActionRankingItemInfo item3 = new ActionRankingItemInfo(4, "4", 500, 4,"dd", "22d");
//		ActionRankingItemInfo item4 = new ActionRankingItemInfo(5, "5", 600, 5,"ee", "22c");
//		ActionRankingItemInfo item5 = new ActionRankingItemInfo(6, "6", 700, 6,"ff", "22g");
//		ActionRankingItemInfo item6 = new ActionRankingItemInfo(6, "6", 700, 6,"ff", "22g");
//		ActionRankingItemInfo item7 = new ActionRankingItemInfo(6, "6", 700, 6,"ff", "22g");
//		ActionRankingItemInfo item8 = new ActionRankingItemInfo(6, "6", 700, 6,"ff", "22g");
//		ActionRankingItemInfo item9 = new ActionRankingItemInfo(6, "6", 700, 6,"ff", "22g");
//		ActionRankingItemInfo item10 = new ActionRankingItemInfo(6, "6", 700, 6,"ff", "22g");
//		ActionRankingItemInfo item11= new ActionRankingItemInfo(6, "6", 700, 6,"ff", "22g");
//		ActionRankingItemInfo item12= new ActionRankingItemInfo(6, "6", 700, 6,"ff", "22g");
//		rank.add(item);
//		rank.add(item1);
//		rank.add(item2);
//		rank.add(item3);
//		rank.add(item4);
//		rank.add(item5);
//		rank.add(item6);
//		rank.add(item7);
//		rank.add(item8);
//		rank.add(item9);
//		rank.add(item10);
//		rank.add(item11);
//		rank.add(item12);
//	}
}
