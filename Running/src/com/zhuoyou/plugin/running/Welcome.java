package com.zhuoyou.plugin.running;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyi.account.IAccountListener;
import com.zhuoyi.account.ZyAccount;
import com.zhuoyou.plugin.action.ActionActivity;
import com.zhuoyou.plugin.action.ActionWelcomeInfo;
import com.zhuoyou.plugin.action.CacheTool;
import com.zhuoyou.plugin.cloud.AlarmUtils;
import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.cloud.GetDataFromNet;
import com.zhuoyou.plugin.cloud.NetMsgCode;
import com.zhuoyou.plugin.cloud.NetUtils;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.database.DataBaseUtil;
import com.zhuoyou.plugin.gps.OperationTimeModel;
import com.zhuoyou.plugin.weather.WeatherTools;

public class Welcome extends Activity implements OnClickListener {

	private Context mContext;
	private boolean isFirst;
	private RelativeLayout layout1;
	private LinearLayout layout2;
	private Button login;
	private Button skip;
	public static boolean isentry = false;
	private ZyAccount mZyAccount;
	private SharedPreferences sharepreference;
	private java.text.SimpleDateFormat formatter;

	//add by zhouzhongbo@20150129 for action module
	private RelativeLayout action_layout;//
	private ImageView ImageNetShow,ImagePassButton;
	private TextView actiong_query;//action_title,action_time
	private boolean is_go_action = false;
	private boolean is_show_action = false;
	private ActionWelcomeInfo mwelcomedate = null;
	private String action_id = null;
	private CacheTool mcachetool = null;
	private DataBaseUtil mData;
	private Editor editor;
	private Handler mHandler;
	private Bitmap bitmap = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		mContext = this;
		mHandler = new Handler();
		isFirst = Tools.checkIsFirstEntry(mContext);
		mData=new DataBaseUtil(mContext);
		formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
		initViews();
		AppActionNetInit();
		judgeGpsState();
	}

	private void initViews() {
		//add by zhouzhongbo@20150129 for action module start
		action_layout = (RelativeLayout)findViewById(R.id.ad_layout);
		ImageNetShow = (ImageView)findViewById(R.id.netshow);
//		action_title = (TextView)findViewById(R.id.action_title);
//		action_time = (TextView)findViewById(R.id.action_time);
		//add by zhouzhongbo@20150129 for action module end
		layout1 = (RelativeLayout) findViewById(R.id.layout1);
		layout2 = (LinearLayout) findViewById(R.id.layout2);
		login = (Button) findViewById(R.id.login);
		login.setOnClickListener(this);
		skip = (Button) findViewById(R.id.skip);
		skip.setOnClickListener(this);
		String lang = Locale.getDefault().getLanguage();
		if (lang.equals("en")) {
			layout1.setBackgroundResource(R.drawable.logo_text_en);
		} else {
			layout1.setBackgroundResource(R.drawable.logo_text);
		}
		
		if (isFirst) {
			layout2.setVisibility(View.VISIBLE);
			WeatherTools.newInstance();
		} else {
			layout2.setVisibility(View.GONE);
			new InitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		//add by zhouzhongbo@20150205 for ad show start
		mcachetool = ((RunningApp)getApplication()).GetCacheTool();
		mwelcomedate = mcachetool.GetWelcomeDate();
		if(mwelcomedate != null){
			String url = mwelcomedate.GetImgUrl();
			try {
				url = url.substring(0, url.lastIndexOf("/")+1)+URLEncoder.encode(url.substring(url.lastIndexOf("/")+1),"UTF-8").replace("+", "%20");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String filePath = Tools.getSDPath() +"/Running/download/cache/"+GetFileName(url);
	        File file = new File(filePath);
	        if(file.exists()){
				action_layout.setVisibility(View.VISIBLE);
			    try  
			    {  
		            bitmap = BitmapFactory.decodeFile(filePath);  
			    } catch (Exception e)  
			    {  
			        // TODO: handle exception  
			    }  
	        	ImageNetShow.setImageBitmap(bitmap);
	        }else{
	        	ImageNetShow.setImageResource(R.drawable.action_1);
	        }
		}else{
			action_layout.setVisibility(View.GONE);
		}
		//add by zhouzhongbo@20150205 for ad show end
	}
	
	public String GetFileName(String url){
		String filename = "";
		if(url != null){
	        String tmp = url;
	        String file_tmp =url;
			for(int i = 0;i<5;i++){
				tmp = tmp.substring(0,tmp.lastIndexOf("/"));
			}
			for(String aa:file_tmp.substring(tmp.length()+1).split("/")){
				filename +=aa;
			}
			filename = filename.substring(0, filename.lastIndexOf("."));
			
		}
		return filename;
	}
	
	private class InitTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			WeatherTools.newInstance();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {			
			gotoMainScreen();
		}
	}
	
	private void gotoMainScreen(){
		if(!is_go_action){
			isentry = true;
			Intent intent = new Intent(mContext, Main.class);
			startActivity(intent);
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			finish();
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.login:
			mZyAccount = new ZyAccount(getApplicationContext(), "1102927580", "1690816199");
			mZyAccount.login(new IAccountListener() {

				@Override
				public void onCancel() {
					
				}

				@Override
				public void onSuccess(String userInfo) {
					Tools.saveInfoToSharePreference(mContext, userInfo);
					Tools.setLogin(mContext, true);
					gotoMainScreen();
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							AlarmUtils.setAutoSyncAlarm(mContext);
							CloudSync.autoSyncType = 1;
							CloudSync.syncAfterLogin(1);
						}
					}, 1000);
				}
				
			});
			break;
		case R.id.skip:
			isentry = true;
			Intent intent = new Intent(mContext, Main.class);
			intent.putExtra("config_dialog", true);
			startActivity(intent);
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			finish();
			break;
		//add by zhouzhongbo@20150129 for action module start
		case R.id.button_query:
		case R.id.netshow:
			is_go_action = true;
			Intent mquery_action = new Intent(Welcome.this, ActionActivity.class);
			mquery_action.putExtra("is_from_welcome", true);
			startActivity(mquery_action);
			finish();
			break;
		//add by zhouzhongbo@20150129 for action module end
		}
	}

	/*
	 * function for get lcd info
	 * zhouzhongbo@20150130
	 */
	public String GetLcdInfo(){
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			int screenWidth = dm.widthPixels;
			int screenHeigh = dm.heightPixels;
			String mlcd = screenWidth+"x"+ screenHeigh;
			return mlcd;
	}
	
	/*
	 * function for get largest id of action which is ended in local!
	 * zhouzhongbo@20150130
	 * give 0 here ,for query new state for every init
	 */
	public String CheckLocalActionId(){
		//here we will add for local dada decode!
		int action_id = 0;
//		List<ActionListItemInfo> mactionlist = mcachetool.GetActionListItemDate();
//		if(mactionlist != null&&mactionlist.size()>0){
//			for(int i = 0;i<mactionlist.size();i++){
//				ActionListItemInfo tmp = mactionlist.get(i);
//				if(tmp.GetActivtyId()>0&&CheckActionIsEnded(tmp)){
//					action_id = tmp.GetActivtyId();
//				}
//			}			
//		}
		return String.valueOf(action_id);
	}
	
//	
//	/*
//	 * check action state:is ended
//	 * 
//	 */
//	private boolean CheckActionIsEnded(ActionListItemInfo action){
//		boolean state = false;
//		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");  
//		String endtime = action.GetActivtyEndTime();
//		String curtime = action.GetActivtyCurTime();
//		long localtime = System.currentTimeMillis();
//		Date end_date = null;
//		Date cur_date = null;
//		
//			try {
//				cur_date = sdf1.parse(curtime);
//				end_date = sdf1.parse(endtime);
//				long ct = cur_date.getTime();
//				long et =  end_date.getTime();
//				if( ct>et || localtime>et){
//					state = true;
//				}
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		return state;
//	}
	
	/*
	 * app step in ,get net info for ad,msg,action listview etc..
	 * 	 zhouzhongbo@20150130
	 */
	private void AppActionNetInit(){
		int net = NetUtils.getAPNType(Welcome.this);
		if (net != -1) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					HashMap<String, String> params = new HashMap<String, String>();
					String openId;
					if(Tools.getLogin(mContext)){
						openId = Tools.getOpenId(mContext);
					}else{
						openId = "0";
					}
					params.put("openId", openId);
					params.put("lcd", GetLcdInfo());
					params.put("actIds", CheckLocalActionId());
					new GetDataFromNet(NetMsgCode.APP_RUN_ACTION_INIT, ((RunningApp) getApplication()).GetAppHandler(), params, getApplication()).execute(NetMsgCode.URL);
				}
			}).start();	
		}
	}
	
	private void judgeGpsState(){
		sharepreference = this.getSharedPreferences("gaode_location_info", Context.MODE_PRIVATE);
		int operation_state=sharepreference.getInt("map_activity_state", 0);
		if(operation_state>1){
			long startTime=mData.selectLastOperation(OperationTimeModel.BEGIN_GPS_GUIDE);
			deletefromDatabase(startTime);
		}
	}
	
	 private void deletefromDatabase(long startTime){
		 List<Integer> listGpsId = mData.selectPointID(startTime, conversTime(System.currentTimeMillis()), 0);
		 List<Integer> listOperationId = mData.selectOperationId(startTime, conversTime(System.currentTimeMillis()), 0);
		 if(listGpsId.size()>0){
			 for(int i=0;i<listGpsId.size();i++){
				 ContentValues runningItem = new ContentValues();
				 runningItem.put(DataBaseContants.GPS_TABLE, DataBaseContants.TABLE_POINT_NAME);
				 runningItem.put(DataBaseContants.GPS_DELETE, listGpsId.get(i));	
				 Welcome.this.getContentResolver().insert(DataBaseContants.CONTENT_URI_GPSSYNC,runningItem);
			 }
		 }
		 if(listOperationId.size()>0){
			 for(int i=0;i<listOperationId.size();i++){
				 ContentValues runningItem = new ContentValues();
				 runningItem.put(DataBaseContants.GPS_TABLE, DataBaseContants.TABLE_OPERATION_NAME);
				 runningItem.put(DataBaseContants.GPS_DELETE, listOperationId.get(i));	
				 Welcome.this.getContentResolver().insert(DataBaseContants.CONTENT_URI_GPSSYNC,runningItem);
			 }
		 }
		 editor = sharepreference.edit();
		 editor.putInt("map_activity_state",0 );
		 if(sharepreference.contains("is_have_beginaddr")){
				editor.remove("is_have_beginaddr");
			}
		 if(sharepreference.contains("gps_sport_id")){
			    mData.deleteGpsFromID(sharepreference.getLong("gps_sport_id", -1));
				editor.remove("gps_sport_id");
			}
		 editor.putInt("save_service_step", 0);
		 editor.putInt("gps_singal_state", 0);
		 editor.putFloat("save_service_distance", 0);
		 editor.putBoolean("is_begin_point", true);
		 editor.commit();
		 mData.deleteOperation(startTime, conversTime(System.currentTimeMillis()));
		 mData.deletePoint(startTime, conversTime(System.currentTimeMillis()));
	 }
	 
	 public long conversTime(long systemTime){
			Date date = new Date(systemTime);
			String str_time = formatter.format(date);
			Long pointTime = Long.parseLong(str_time);
			return pointTime;
		}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(bitmap != null){
			ImageNetShow.setImageBitmap(null);
			//Log.d("zzb1","ondestory bitmap recycle");
			bitmap.recycle();
			bitmap = null;
		}
	}
	 
	 @Override
	public void onBackPressed() {
	}
	 
}
