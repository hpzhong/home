package com.zhuoyou.plugin.action;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.cloud.GetDataFromNet;
import com.zhuoyou.plugin.cloud.NetMsgCode;
import com.zhuoyou.plugin.cloud.NetUtils;
import com.zhuoyou.plugin.running.Main;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

public class ActionActivity extends Activity implements OnItemClickListener{

	private CacheTool mcachetool;
	private LinearLayout action_container;
	
	//ui add 
    private ListView mListView;
    private PullToRefreshListView mPullListView;
    private ActionAdaptor mAdapter;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");
    private List<ActionListItemInfo> tmp_list;
    private int leastId = -1;
	private boolean Is_From_Welcome = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.action_layout);
		Intent intent=getIntent();
		Is_From_Welcome = intent.getBooleanExtra("is_from_welcome", false);
		mcachetool = ((RunningApp)getApplication()).GetCacheTool();
		InitView();
	}

	
	private void InitView(){
		TextView tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.running_event);
		RelativeLayout im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(Is_From_Welcome){
					Intent intent = new Intent(ActionActivity.this, Main.class);
					startActivity(intent);
				}
				finish();
			}
		});
		action_container = (LinearLayout)findViewById(R.id.action_container);
        mPullListView = new PullToRefreshListView(this);
        action_container.addView(mPullListView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mPullListView.setPullLoadEnabled(false);
        mPullListView.setScrollLoadEnabled(true);
        mListView = mPullListView.getRefreshableView();
        mListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mListView.setDividerHeight(0);
        mAdapter = new ActionAdaptor(ActionActivity.this, mListView, mcachetool);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mPullListView.setOnRefreshListener(new com.zhuoyou.plugin.action.PullToRefreshBase.OnRefreshListener<ListView>() {
        	//下拉刷新，
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                //这里没有设置最大活动ID是因为要更新除了顶端的数据太，好需要同步本地已缓存的数据
                StartRefreshTask(0);
            }

            //触底刷新
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
//              actId	string	向下翻页时：本地当前页最小的活动Id；刷新最新时：传已缓存最大活动Id
//              type	string	0:刷新 1：分页
                StartRefreshTask(1);
            }
        });
        setLastUpdateTime();
        
        mPullListView.doPullRefreshing(true, 500);
	}
	
	public void StartRefreshTask(int type){
		int net = NetUtils.getAPNType(ActionActivity.this);
		int msg_code = 0;
		if (net == -1) {
            mPullListView.onPullDownRefreshComplete();
            mPullListView.onPullUpRefreshComplete();
			Toast.makeText(ActionActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
			mPullListView.setPullRefreshEnabled(false);
			mPullListView.setScrollLoadEnabled(false);
			return;
		}
			
		HashMap<String, String> params = new HashMap<String, String>();
		switch(type){
			case 0:
                //这里没有设置最大活动ID是因为要更新除了顶端的数据太，好需要同步本地已缓存的数据
    			params.put("type", "0");
    			params.put("actId", "0");
    			msg_code = NetMsgCode.ACTION_GET_REFRESHPAGE;
				break;

			case 1:
	  			params.put("type", "1");
	  			if(mcachetool!= null){
	  				int id = mcachetool.GetLocalLittleIdofActionitem();
	  				params.put("actId", String.valueOf(id));
	  				leastId = id;
	  			}else{
	  				params.put("actId", "14");
	  			}
    			msg_code = NetMsgCode.ACTION_GET_NEXTPAGE;
				break;
		}
		params.put("lcd", GetLcdInfo());
    	new GetDataFromNet(msg_code, mhandler, params, ActionActivity.this).execute(NetMsgCode.URL);
	}
	
	
    
    private void setLastUpdateTime() {
        String text = formatDateTime(System.currentTimeMillis());
        mPullListView.setLastUpdatedLabel(text);
    }

    private String formatDateTime(long time) {
        if (0 == time) {
            return "";
        }
        return mDateFormat.format(new Date(time));
    }	
		
	private Handler mhandler = new Handler(){

		@Override
		public void dispatchMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
				case NetMsgCode.The_network_link_failure:
		            mPullListView.onPullDownRefreshComplete();
		            mPullListView.onPullUpRefreshComplete();
		            mPullListView.setHasMoreData(true);
					break;
				case NetMsgCode.The_network_link_success:
					switch(msg.arg1){
					
//						//add by zhouzhongbo@20150129 for action module	start
//						case NetMsgCode.APP_RUN_ACTION_INIT:
//							AppInitForAction mm  = (AppInitForAction)msg.obj;
//							mcachetool.SaveActionInitDate(mm);
//							break;
							
						case NetMsgCode.ACTION_GET_MSG:
							if(mcachetool != null){
								List<MessageInfo> tmp = (List<MessageInfo>) msg.obj;
								mcachetool.SaveMsgList(tmp);
							}
							break;
							
						case NetMsgCode.ACTION_JOIN:
							
							break;
							
						case NetMsgCode.ACTION_GET_IDINFO:
							if(mcachetool != null){
								List<ActionInfo> tmp = (List<ActionInfo>) msg.obj;
								mcachetool.SaveActionInfo(tmp);
							}
							break;
							
						case NetMsgCode.ACTION_GET_REFRESHPAGE:
							tmp_list = (List<ActionListItemInfo>) msg.obj;
							mAdapter.SetMyListItem(tmp_list);
				            mAdapter.notifyDataSetChanged();
				            mPullListView.onPullDownRefreshComplete();
				            mPullListView.onPullUpRefreshComplete();
				            mPullListView.setHasMoreData(true);
							if(mcachetool != null){
								mcachetool.ClearListItem();
								mcachetool.SaveActionListItem(tmp_list);
							}
				            setLastUpdateTime();
							break;
							
						case NetMsgCode.ACTION_GET_NEXTPAGE:
							tmp_list = (List<ActionListItemInfo>) msg.obj;
							if(tmp_list.size() == 0){
								Toast.makeText(ActionActivity.this, R.string.act_text, Toast.LENGTH_SHORT).show();
								mPullListView.setScrollLoadEnabled(false);
							}else{
								mPullListView.setScrollLoadEnabled(true);
							}
							mAdapter.AddListitem(tmp_list);
				            mAdapter.notifyDataSetChanged();
				            mPullListView.onPullDownRefreshComplete();
				            mPullListView.onPullUpRefreshComplete();
				            mPullListView.setHasMoreData(true);
							if(mcachetool != null){
//								mcachetool.ClearCachefile();
								mcachetool.SaveActionListItem(tmp_list);
							}
							break;
				}
				break;
			}
			super.dispatchMessage(msg);
		}
		
	};
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Intent mintent = new Intent(this,ActionDetailActivity.class);
		int index = (int) mAdapter.getItemId(position);
		if(index == -1)
			return;
		String title = ((ActionListItemInfo)mAdapter.getItem(position)).GetActivtyTitle();
		String startTime=((ActionListItemInfo) mAdapter.getItem(position)).GetActivtyStartTime();
		String endTime=((ActionListItemInfo) mAdapter.getItem(position)).GetActivtyEndTime();
		String curTime=((ActionListItemInfo)mAdapter.getItem(position)).GetActivtyCurTime();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date sta_date = null;
		Date cur_date = null;
		Date end_date = null;
		String state = null;
		try {
			sta_date =sdf1.parse(startTime);
			cur_date = sdf1.parse(curTime);
			end_date = sdf1.parse(endTime);
			long st=sta_date.getTime();
			long ct = cur_date.getTime();
			long et =  end_date.getTime();
			if(ct<st){
				//未开始
				state = "0";
			}else if(ct<et){
				//正在进行中
				state = "1";
			}else if (ct>et){
				//已结束
				state = "2";
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mintent.putExtra("id",String.valueOf(index));
		mintent.putExtra("action_title", title);
		mintent.putExtra("action_flag",state);
		startActivity(mintent);
	}
	
	public void GetLocalfile(){
		File cache_path = ActionActivity.this.getCacheDir();
		File file_path = ActionActivity.this.getFilesDir();
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Tools.setActState(this, false);
		if (Main.mHandler != null) {
			Message msg = new Message();
			msg.what = Main.ACT_STATE;
			Main.mHandler.sendMessage(msg);
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(Is_From_Welcome){
			Intent intent = new Intent(this, Main.class);
			startActivity(intent);
		}
		finish();
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
	
}
