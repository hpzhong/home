package com.zhuoyou.plugin.resideMenu;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.receiver.DeviceNameReceiver;
import com.zhuoyou.plugin.running.R;

public class ResideMenuRightTop extends LinearLayout implements OnClickListener {
	private Context mContext;
	private String productName = "";
	private ImageView device_logo;
	private LinearLayout connected_device;
	private TextView device_name;
	private TextView device_battery;
	private Button connect_device;
	public static Handler mHandler;
	public static final int REFRESH = 1;
	public static final int BATTERY = 2;

    public ResideMenuRightTop(Context context) {
        super(context);
        mContext = context;
        initViews(context);
        mHandler = new Handler() {
        	public void handleMessage(Message msg) {
        		switch (msg.what) {
        		case REFRESH:
        			productName = (String)msg.obj;
        	        if (productName.equals("")) {
        	        	device_logo.setImageResource(R.drawable.device_add);
        	        	connected_device.setVisibility(View.GONE);
        	        	connect_device.setVisibility(View.VISIBLE);
        	        } else {
        	        	device_logo.setImageResource(R.drawable.device_logo);
        	        	connected_device.setVisibility(View.VISIBLE);
        	        	connect_device.setVisibility(View.GONE);
        	        	device_name.setText(productName);
        	        	device_battery.setText("正在获取电量");
        	        }
        			break;
        		case BATTERY:
        			int status = msg.arg1;
        			int battery_state = msg.arg2;
        			if (battery_state == 0xff) {
        				device_battery.setText("电量低");
        				device_battery.setTextColor(0xFFF81233);
        			} else {
        				int battery = battery_state  - 0x20;
            			if (status == 1) {
            				device_battery.setText("充电中");
            				device_battery.setTextColor(0xFFF81233);
            			} else {
            				if (status == 2)
            					device_battery.setText("充电已完成");
            				else {
            					device_battery.setText("剩余电量：" + battery + "%");
	            				if (battery > 20) {
	            					device_battery.setTextColor(0xffffffff);
	            				} else {
	            					device_battery.setTextColor(0xFFF81233);
	            				}
            				}
            			}
        			}
        			break;
        		}
        	}
        };
    }

    private void initViews(Context context) {
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.residemenu_right_top, this);
        device_logo = (ImageView) findViewById(R.id.device_logo);
        connected_device = (LinearLayout) findViewById(R.id.connected_device);
        device_name = (TextView) findViewById(R.id.device_name);
        device_battery = (TextView) findViewById(R.id.device_battery);
        connect_device = (Button) findViewById(R.id.connect_device);
                
        device_logo.setOnClickListener(this);
        connect_device.setOnClickListener(this);
    }

    private Boolean getBTSecretaryStatus(Context context) {
    	Boolean flag = false;
		PackageManager mPackageManager = context.getPackageManager();
		List<PackageInfo> pkgs = mPackageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for (PackageInfo pkg : pkgs) {
			if(pkg.packageName.equals("com.tyd.btsecretary")) {
				flag = true;
				break;
			} else {
				continue;
			}
		}
		return flag;
    }
    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.device_logo:
		case R.id.connect_device:
			if (getBTSecretaryStatus(mContext)) {
				Intent mIntent = mContext.getPackageManager().getLaunchIntentForPackage("com.tyd.btsecretary");
				mContext.startActivity(mIntent);
			} else {
				Toast.makeText(mContext, "你还没有安装蓝牙秘书", Toast.LENGTH_SHORT).show();
			}
			break;
		}
		
	}

}
