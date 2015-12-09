package com.zhuoyou.plugin.rank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.cloud.NetMsgCode;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningTitleBar;

public class MotionRank extends Activity implements OnClickListener {

	private TextView dayRank, weekRank;
	private View dayV, weekV;
	private ListView moRankList;
	private RankListAdapter mRankListAdapter;
	public static Handler handler = null;
	HashMap<String, List<RankInfo>> resRankList = new HashMap<String, List<RankInfo>>();
	List<RankInfo> sevenDalysList = new ArrayList<RankInfo>();
	List<RankInfo> highestStepList = new ArrayList<RankInfo>();
	List<RankInfo> accountServenData = new ArrayList<RankInfo>();
	List<RankInfo> accountHighestData = new ArrayList<RankInfo>();
	private TextView myRank, myName, mySteps;
	private ImageView myIcon;
	private RelativeLayout myRankLayout;
	private boolean isSync;
	private ProgressDialog m_pDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.motion_rank);
		RunningTitleBar.getTitleBar(this,
				getResources().getString(R.string.my_ranking));
		initView();
	}

	void initView() {
		dayRank = (TextView) findViewById(R.id.dayRank);
		weekRank = (TextView) findViewById(R.id.weekRank);
		dayRank.setTextColor(0xff00a5a7);
		weekRank.setTextColor(0xff868686);
		dayV = (View) findViewById(R.id.dayV);
		dayV.setVisibility(View.VISIBLE);
		weekV = (View) findViewById(R.id.weekV);
		weekV.setVisibility(View.GONE);
		moRankList = (ListView) findViewById(R.id.moRankList);
		myRankLayout = (RelativeLayout) findViewById(R.id.mylayout);
		myRankLayout.setVisibility(View.GONE);
		myRank = (TextView) findViewById(R.id.my_rank);
		myName = (TextView) findViewById(R.id.my_name);
		mySteps = (TextView) findViewById(R.id.step);
		myIcon=(ImageView)findViewById(R.id.my_icon);
		
		m_pDialog = new ProgressDialog(MotionRank.this);
		m_pDialog.setMessage(this.getResources().getString(R.string.progressbar_dialog_txt));
		m_pDialog.setCancelable(false);
		m_pDialog.show();
		
		this.isSync = CloudSync.isSync;
		if(isSync!=true){
			CloudSync.syncData(2);
		}

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Log.d("txhlog", "msg:" + msg);
				switch (msg.what) {
				case NetMsgCode.rank_state_wait:
					break;
				case NetMsgCode.rank_state_request:
					new Thread(CloudSync.getNetRankInfoRunnable).start();
					break;
				case NetMsgCode.The_network_link_success:
					switch (msg.arg1) {

					case NetMsgCode.getNetRankInfo:
						if(m_pDialog!=null && m_pDialog.isShowing()){
							m_pDialog.dismiss();
						}
						resRankList = (HashMap<String, List<RankInfo>>) msg.obj;
						if(resRankList.size()>0){
							sevenDalysList = resRankList.get("sevenDaysStepList");
							highestStepList = resRankList.get("highestStepList");
							accountServenData = resRankList.get("accountSevenData");
							accountHighestData = resRankList.get("accountHighestData");
						}
						initData(sevenDalysList,accountServenData);
						break;
					default:
						break;
					}
					break;
				case NetMsgCode.The_network_link_failure:
					if(m_pDialog!=null && m_pDialog.isShowing()){
						m_pDialog.dismiss();
					}
					Toast.makeText(MotionRank.this, R.string.network_link_failure, Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
	}

	void initData(List<RankInfo> mList, List<RankInfo> myList) {
		if (mList != null) {
			myRankLayout.setVisibility(View.VISIBLE);
			mRankListAdapter = new RankListAdapter(this, mList);
			moRankList.setAdapter(mRankListAdapter);
			myRankInfo(myList);
		}
	}

	void myRankInfo(List<RankInfo> mList) {
		String rank = null;
		String name = null;
		String steps = null;
		Drawable img = null;		
		for (RankInfo info : mList) {
			rank = info.getRank() + "";
			name = info.getName();
			steps = info.getmSteps();
			img = info.getImg();
		}
		myRank.setText(rank);
		myName.setText(name);
		mySteps.setText(steps);
		myIcon.setImageDrawable(img);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.dayLay:
			dayRank.setTextColor(0xff00a5a7);
			weekRank.setTextColor(0xff868686);
			dayV.setVisibility(View.VISIBLE);
			weekV.setVisibility(View.INVISIBLE);

			initData(sevenDalysList,accountServenData);
			break;
		case R.id.weekLay:
			dayRank.setTextColor(0xff868686);
			weekRank.setTextColor(0xff00a5a7);
			dayV.setVisibility(View.INVISIBLE);
			weekV.setVisibility(View.VISIBLE);
			
			initData(highestStepList,accountHighestData);
			break;
		}
	}
}
