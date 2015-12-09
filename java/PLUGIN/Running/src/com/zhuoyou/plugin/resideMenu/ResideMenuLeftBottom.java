package com.zhuoyou.plugin.resideMenu;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.running.Main;
import com.zhuoyou.plugin.running.R;

public class ResideMenuLeftBottom extends LinearLayout {
	
	private LinearLayout setting_layout;
	private TextView mVersionView;
	private String mVersion;
	
    public ResideMenuLeftBottom(Context context) {
        super(context);
        mVersion = setAppVersionInfo(context);
        initViews(context);
    }

    private void initViews(final Context context) {
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.residemenu_left_bottom, this);
        setting_layout = (LinearLayout)findViewById(R.id.setting);
        setting_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Main.closeMenu();
				Intent intent = new Intent(context,SettingActivity.class);
				intent.putExtra("version", mVersion);
				context.startActivity(intent);
			}
        	
        });
        mVersionView = (TextView)findViewById(R.id.version);
        if (!mVersion.equals(""))
        	mVersionView.setText(mVersion);
    }
    
    /** 
     * 返回当前程序版本名称 
     */  
    private String setAppVersionInfo(Context context) {
    	String info = "";
        try {  
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);//PackageManager.GET_CONFIGURATIONS  
            String mVerNameString = pi.versionName;
            String mAppNameString = pi.applicationInfo.loadLabel(context.getPackageManager()).toString();
            int mVerCode = pi.versionCode;
            info = mAppNameString + " V" + mVerNameString + "_" + mVerCode;
        } catch (Exception e) {  
        	Log.e("VersionInfo", "Exception", e); 
        }
        return info;
    }

}
