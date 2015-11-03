package com.zhuoyou.plugin.mainFrame;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.action.ActionActivity;
import com.zhuoyou.plugin.fitness.FitnessMain;
import com.zhuoyou.plugin.resideMenu.BluetoothHeadset;
import com.zhuoyou.plugin.resideMenu.BluetoothHeadsetActivity;
import com.zhuoyou.plugin.resideMenu.BluetoothWatchActivity;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.view.BadgeView;

public class FinderFragment extends Fragment {
	BadgeView badge3;
	private View mView;
	private RelativeLayout qumeLayout;
	private RelativeLayout eameyLayout;
	private RelativeLayout watchLayout;
	private RelativeLayout fitnessLayout;
	private RelativeLayout actionLayout;
	private RelativeLayout circleLayout;
	private Context mContext = RunningApp.getInstance().getApplicationContext();
	private BadgeView badgeView;
	private TextView actionText;
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView=inflater.inflate(R.layout.finder_fragment, container, false);	
		initView();
		return mView;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(Tools.getActState(mContext)){
			drawCircle();
		}else{
			cancleDrawCircle();
		}
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private void initView(){
//		qumeLayout=(RelativeLayout) mView.findViewById(R.id.facility_qume);
//		eameyLayout=(RelativeLayout) mView.findViewById(R.id.facility_eamey);
//		watchLayout=(RelativeLayout) mView.findViewById(R.id.facility_watch);
		fitnessLayout=(RelativeLayout) mView.findViewById(R.id.discover_fitness);
//		actionLayout=(RelativeLayout) mView.findViewById(R.id.action);
	    circleLayout=(RelativeLayout) mView.findViewById(R.id.discover_circle);
//		actionText=(TextView) mView.findViewById(R.id.action_text);
/*		qumeLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext,BluetoothHeadset.class);
				startActivity(intent);
			}
		});
		
		eameyLayout.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext,BluetoothHeadsetActivity.class);
				startActivity(intent);
			}
		});

		watchLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext,BluetoothWatchActivity.class);
				startActivity(intent);
			}
		});*/

		fitnessLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext,FitnessMain.class);
	        	startActivity(intent);
			}
		});
		

		
		/*actionLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(mContext,ActionActivity.class);
				startActivity(intent);
			}
		});
*/		
		circleLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(mContext, getResources().getString(R.string.discover_show),Toast.LENGTH_SHORT).show();
			}
		});
		
		badgeView = new BadgeView(mContext, actionText);
	}
	
	public void drawCircle(){
		badgeView.setBackgroundResource(R.drawable.remind_circle);
		badgeView.setBadgeMargin(0, 0);
		badgeView.setWidth(8);
		badgeView.setHeight(8);
		badgeView.toggle(false);
	}
	
	public void cancleDrawCircle(){
		if (badgeView != null) {
			badgeView.toggle(true);
			badgeView = null;
		}
	}
	
}
