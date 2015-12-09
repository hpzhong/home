package com.zhuoyou.plugin.running;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.album.SportsAlbum;
import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.cloud.NetUtils;
import com.zhuoyou.plugin.fitness.FitnessMain;
import com.zhuoyou.plugin.rank.MotionRank;
import com.zhuoyou.plugin.receiver.DeviceNameReceiver;
import com.zhuoyou.plugin.resideMenu.ResideMenu;
import com.zhuoyou.plugin.resideMenu.ResideMenuItem;
import com.zhuoyou.plugin.resideMenu.ResideMenuLeftBottom;
import com.zhuoyou.plugin.resideMenu.ResideMenuLeftTop;
import com.zhuoyou.plugin.resideMenu.ResideMenuRightItem;
import com.zhuoyou.plugin.resideMenu.ResideMenuRightTop;
import com.zhuoyou.plugin.selfupdate.Constants;
import com.zhuoyou.plugin.selfupdate.SelfUpdateMain;
import com.zhuoyou.plugin.weather.WeatherTools;

public class Main extends FragmentActivity implements OnClickListener {
	
	public TextView title_bar_text;
    private static ResideMenu resideMenu;
    private Main mContext;
    private ResideMenuLeftTop itemLeftTop;
    private ResideMenuItem itemRank;
    private ResideMenuItem itemData;
    private ResideMenuItem itemFitness;
    private ResideMenuItem itemMusic;
    private ResideMenuItem itemPhoto;
    private ResideMenuLeftBottom itemBottom;
    private ResideMenuRightTop itemRightTop;
    private ResideMenuRightItem itemGoal;
    private ResideMenuRightItem itemRemind;
    private ResideMenuRightItem itemGetData;
    private ResideMenuRightItem itemCloud;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
        mContext = this;

		if (MainService.getInstance() != null) {
			Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
			intent.putExtra("plugin_cmd", 0x73);
			intent.putExtra("plugin_content", "himan");
			sendBroadcast(intent);
		}

		// gchk add 每次进入main的时候定位获取天气
		if (Tools.getPm25(Tools.getDate(0)) <= 0) {
			WeatherTools.newInstance().getCurrAqi();
		}
		initView();
        setUpMenu();
        changeFragment(new HomePageFragment());
        
        if(SelfUpdateMain.isDownloading == false)
        	SelfUpdateMain.execApkSelfUpdateRequest(this, Constants.APPID, Constants.CHNID);
	}

	@Override
	public void onResume() {
		super.onResume();
		itemLeftTop.initDate(mContext);
	}

	@Override
	public void onPause() {
		super.onPause();
		if(resideMenu.isOpened()) {
			resideMenu.closeMenu();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void initView() {
		title_bar_text = (TextView) findViewById(R.id.title_bar_text);
		title_bar_text.setText(R.string.today);
		ImageView title_bar_left_menu = (ImageView) findViewById(R.id.title_bar_left_menu);
		title_bar_left_menu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (resideMenu.isOpened())
					return;
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
			}			
		});
		ImageView title_bar_right_menu = (ImageView) findViewById(R.id.title_bar_right_menu);
		title_bar_right_menu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (resideMenu.isOpened())
					return;				
                resideMenu.openMenu(ResideMenu.DIRECTION_RIGHT);
			}			
		});
		
		//进入程序前。保证 目录下存在 running 文件夹
		String filePath = Tools.getSDPath() + "/Running/";
		File dir = new File(filePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}
	
	private void setUpMenu() {
        resideMenu = new ResideMenu(this);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);
        resideMenu.setScaleValue(0.8f);

        itemLeftTop = new ResideMenuLeftTop(this);
        itemRank = new ResideMenuItem(this, R.drawable.my_ranking, R.string.my_ranking);
        itemData = new ResideMenuItem(this, R.drawable.data_center, R.string.data_center);
        itemFitness = new ResideMenuItem(this, R.drawable.fitness_plan, R.string.fitness_plan);
        itemMusic = new ResideMenuItem(this, R.drawable.sports_music, R.string.sports_music);
        itemPhoto = new ResideMenuItem(this, R.drawable.sports_photo, R.string.sports_photo);
        itemBottom = new ResideMenuLeftBottom(this);
        
        itemRightTop = new ResideMenuRightTop(this);
        itemGoal = new ResideMenuRightItem(this, R.drawable.sports_goal, R.string.motion_goal);
        itemRemind = new ResideMenuRightItem(this, R.drawable.sedentary_remind, R.string.sedentary_remind);
        itemGetData = new ResideMenuRightItem(this, R.drawable.syncs_data, R.string.syncs_with_device);
        itemCloud = new ResideMenuRightItem(this, R.drawable.cloud_syncs, R.string.cloud_syncs);
        
        resideMenu.addMenuLeftTop(itemLeftTop);
        resideMenu.addMenuItem(itemRank);
        resideMenu.addMenuItem(itemData);
        resideMenu.addMenuItem(itemFitness);
        //resideMenu.addMenuItem(itemMusic);
        resideMenu.addMenuItem(itemPhoto);
        resideMenu.addMenuBottom(itemBottom);
        
        resideMenu.addMenuRightTop(itemRightTop);
        resideMenu.addMenuRightItem(itemGoal);
        //resideMenu.addMenuRightItem(itemRemind);
        resideMenu.addMenuRightItem(itemGetData);
        resideMenu.addMenuRightItem(itemCloud);
        
        itemLeftTop.setOnClickListener(this);
        itemRank.setOnClickListener(this);
        itemData.setOnClickListener(this);
        itemFitness.setOnClickListener(this);
        itemMusic.setOnClickListener(this);
        itemPhoto.setOnClickListener(this);
        
        itemGoal.setOnClickListener(this);
        itemRemind.setOnClickListener(this);
        itemGetData.setOnClickListener(this);
        itemCloud.setOnClickListener(this);
	}
	
    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
        	
        }

        @Override
        public void closeMenu() {

        }
    };

	@Override
	public void onClick(View view) {
        resideMenu.closeMenu();
		if (view == itemRank) {
			if (!Tools.getLogin(mContext)) {
				Toast.makeText(mContext, R.string.login_before_sync, Toast.LENGTH_SHORT).show();
				return;
			}
			if ( NetUtils.getAPNType(mContext) == -1) {
				Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_SHORT).show();
				return;
			}
			Intent rIntent = new Intent(Main.this, MotionRank.class);
			startActivity(rIntent);
		} else if (view == itemData){
			Intent intent = new Intent(Main.this, MotionDataActivity.class);
			startActivity(intent);
        } else if (view == itemFitness) {
        	Intent intent = new Intent(Main.this,FitnessMain.class);
        	startActivity(intent);
			
		} else if (view == itemMusic) {
			
		} else if (view == itemPhoto) {
			Intent intent = new Intent(Main.this, SportsAlbum.class);
			startActivity(intent);
		} else if (view == itemGoal) {
			Intent intent = new Intent(Main.this, PersonalGoalActivity.class);
			startActivity(intent);
		} else if (view == itemRemind) {
			
		} else if (view == itemGetData) {
			if (DeviceNameReceiver.productName.equals("")) {
				Toast.makeText(mContext, "请连接设备", Toast.LENGTH_SHORT).show();
			} else {
				if (MainService.getInstance() != null) {
					Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
					intent.putExtra("plugin_cmd", 0x73);
					intent.putExtra("plugin_content", "himan");
					sendBroadcast(intent);
				}
			}
        } else if (view == itemCloud) {
        	CloudSync.prepareSync();
		}
	}  

    private void changeFragment(Fragment targetFragment){
        resideMenu.clearIgnoredViewList();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    public ResideMenu getResideMenu(){
        return resideMenu;
    }

    public void updateTitle(String title) {
    	title_bar_text.setText(title);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

	@Override
	public void onBackPressed(){
		if(resideMenu.isOpened()){
			resideMenu.closeMenu();
		}else{
			//直接放入后台
			moveTaskToBack(true);
		}
	}
	
	public static void closeMenu() {
		resideMenu.closeMenu();
	}
	
}
