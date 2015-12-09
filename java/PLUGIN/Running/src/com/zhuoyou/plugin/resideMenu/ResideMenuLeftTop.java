package com.zhuoyou.plugin.resideMenu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuoyi.account.IAccountListener;
import com.zhuoyi.account.ZyAccount;
import com.zhuoyou.plugin.cloud.AlarmUtils;
import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.info.InformationActivity;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;

public class ResideMenuLeftTop extends LinearLayout {

	private LinearLayout user_layout;
	private ImageView drawer_face;
	private Button log_in;
	private LinearLayout log_on;
	private TextView usrname,signature;
	private Context mContext;
	private ZyAccount mZyAccount;
	private int[] headIcon;

	public ResideMenuLeftTop(Context context) {
		super(context);
		mContext = context;
		mZyAccount = new ZyAccount(mContext.getApplicationContext(),
				"1102927580", "1690816199");
		initViews(context);
		initDate(context);
	}
	
	private BroadcastReceiver mGetInfo = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("com.zhuoyou.plugin.updateInfo")) {
				if (Tools.getHead(mContext) != 6) {
					drawer_face.setImageResource(headIcon[Tools.getHead(mContext)]);
				} else {
					drawer_face.setImageResource(R.drawable.logo_default);
				}
				if (!Tools.getSignature(mContext).equals("")) {
					signature.setText(Tools.getSignature(mContext));
				}
				unRegisterBc();
			}
		}
	};
	
	private void registerBc() {
		IntentFilter intentF = new IntentFilter();
		intentF.addAction("com.zhuoyou.plugin.updateInfo");
		mContext.registerReceiver(mGetInfo, intentF);
	}
	
	private void unRegisterBc() {
		mContext.unregisterReceiver(mGetInfo);
	}

    private void initViews(final Context context) {
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.residemenu_left_top, this);
        user_layout = (LinearLayout) findViewById(R.id.user_layout);
        user_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, InformationActivity.class);
				context.startActivity(intent);
			}
        	
        });
		drawer_face = (ImageView) findViewById(R.id.drawer_top_face);
		log_in = (Button) findViewById(R.id.log_in);
		log_in.setOnClickListener(btnListener);
		log_on = (LinearLayout) findViewById(R.id.logged_on);
		usrname = (TextView) findViewById(R.id.usrname);
		signature = (TextView) findViewById(R.id.signature);
    }

	public void initDate(Context context) {
		headIcon = Tools.headIcon;

		if (Tools.getHead(context) != 6) {
			drawer_face.setImageResource(headIcon[Tools.getHead(context)]);
		} else {
			drawer_face.setImageResource(R.drawable.logo_default);
		}

		if (Tools.getLogin(context)) {
			log_on.setVisibility(View.VISIBLE);
			log_in.setVisibility(View.GONE);
		} else {
			log_on.setVisibility(View.GONE);
			log_in.setVisibility(View.VISIBLE);
			usrname.setText(null);
		}

		if (Tools.getUsrName(mContext).equals("")) {
			if (!Tools.getLoginName(mContext).equals("")) {
				usrname.setText(Tools.getLoginName(mContext));
			} else {
				usrname.setText(null);
			}
		} else {
			usrname.setText(Tools.getUsrName(mContext));
		}
		
		if (!Tools.getSignature(mContext).equals("")) {
			signature.setText(Tools.getSignature(mContext));
		}

	}

	OnClickListener btnListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mZyAccount.login(new IAccountListener() {

				@Override
				public void onSuccess(String userInfo) {
					Tools.saveInfoToSharePreference(mContext, userInfo);
					Tools.setLogin(mContext, true);
					log_in.setVisibility(View.GONE);
					log_on.setVisibility(View.VISIBLE);
					if (Tools.getUsrName(mContext).equals("")) {
						usrname.setText(Tools.getLoginName(mContext));
					} else {
						usrname.setText(Tools.getUsrName(mContext));
					}
					registerBc();
					AlarmUtils.setAutoSyncAlarm(mContext);
					CloudSync.autoSyncType = 1;
					CloudSync.downloadData(0);
				}

				@Override
				public void onCancel() {

				}

			});
		}
	};

}
