package com.zhuoyou.plugin.action;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.cloud.GetDataFromNet;
import com.zhuoyou.plugin.cloud.NetMsgCode;
import com.zhuoyou.plugin.cloud.NetUtils;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

public class ActionDetailActivity extends FragmentActivity {
    private Context mContext = RunningApp.getInstance().getApplicationContext();
	private ImageView cursor;
	private ViewPager mPager;
	MyAdapter mPageAdaptor;
	private TextView view1, view2, view3, view4,join_action,mtitle;
	private int currIndex;// 当前页卡编号
	private int bmpWidth;// 横线图片宽度
	private int offset;// 图片移动的偏移量	
	private String action_flag = "0";
	private CacheTool mcachetool;	
	private RelativeLayout back;
	private String actionId="";
	private String ActionTitle = null;
	
	private List<ActionPannelItemInfo> listPannelItem;
	
	private List<ActionRankingItemInfo> listRank;
	
	private ActionRankingItemInfo myRank=new ActionRankingItemInfo();
	private ActionInfo actionInfo;	
    //这个是有多少个 fragment页面  
    static final int NUM_ITEMS = 4;
    
    private String openId;
    
    private int result=-1;
    
    HashMap<String, String> params = new HashMap<String, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_action);
		mcachetool = ((RunningApp)getApplication()).GetCacheTool();
		Intent intent=getIntent();
		actionId=intent.getStringExtra("id");
		ActionTitle = intent.getStringExtra("action_title");
		action_flag=intent.getStringExtra("action_flag");
		openId=Tools.getOpenId(this);
		initView();
		initImage();
		initViewPager();
		
	}
	@Override
	protected void onResume() {
		super.onResume();
		openId=Tools.getOpenId(this);
		
		int net = NetUtils.getAPNType(mContext);
		if(net==-1){
			Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_SHORT).show();
		}else{
			params.put("actId", actionId);
			params.put("openId",openId);
			params.put("lcd", GetLcdInfo());
			new GetDataFromNet(NetMsgCode.ACTION_GET_IDINFO, mhandler,params,mContext).execute(NetMsgCode.URL);
		}
	}
	
	public void initView() {
		join_action=(TextView) findViewById(R.id.join_action);
		
		mtitle = (TextView)findViewById(R.id.title);
		if(ActionTitle != null&&!ActionTitle.equals(""))
			mtitle.setText(ActionTitle);
		
		view1 = (TextView) findViewById(R.id.intro);
		view2 = (TextView) findViewById(R.id.rule);
		view3 = (TextView) findViewById(R.id.rank);
		view4 = (TextView) findViewById(R.id.notice);

		view1.setOnClickListener(new txListener(0));
		view2.setOnClickListener(new txListener(1));
		view3.setOnClickListener(new txListener(2));
		view4.setOnClickListener(new txListener(3));
				
		cursor = (ImageView) findViewById(R.id.cursor);
		back=(RelativeLayout) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		join_action.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!action_flag.equals("2") && actionInfo.GetIsJoin() != 1) {
					int net = NetUtils.getAPNType(mContext);
					if (net == -1) {
						Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_SHORT).show();
					} else {
						if (openId == null || openId.equals("")) {
							Toast.makeText(mContext, R.string.login_before_lookup, Toast.LENGTH_SHORT).show();
						} else {
							new GetDataFromNet(NetMsgCode.ACTION_JOIN, mhandler, params, mContext).execute(NetMsgCode.URL);
						}
					}
				}

			}

		});
	}


	/*
	 * 初始化图片的位移像素
	 */
	public void initImage() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;
		bmpWidth = screenW / 4;
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.red_line);
		bitmap = BitMapTools.zoomImg(bitmap, bmpWidth, bitmap.getHeight());
		cursor.setImageBitmap(bitmap);
		offset = (screenW / 4 - bmpWidth) / 2;
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursor.setImageMatrix(matrix);
	}

	public void initViewPager() {
		mPager = (ViewPager) findViewById(R.id.viewPager);
		mPageAdaptor = new MyAdapter(getSupportFragmentManager(),actionInfo);
		mPager.setAdapter(mPageAdaptor);
		mPager.setCurrentItem(0);// 设置当前显示标签页为第一页
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());// 页面变化时的监听器
	}

	

	public class txListener implements View.OnClickListener {
		private int index = 0;

		public txListener(int i) {
			index = i;
		}
		@Override
		public void onClick(View v) {
			mPager.setCurrentItem(index);
		}
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {
		private int one = offset * 2 + bmpWidth;// 两个相邻页面的偏移量

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageSelected(int arg0) {
			Animation animation = new TranslateAnimation(currIndex * one, arg0* one, 0, 0);// 平移动画
			currIndex = arg0;
			animation.setFillAfter(true);// 动画终止时停留在最后一帧，不然会回到没有执行前的状态
			animation.setDuration(200);// 动画持续时间0.2秒
			cursor.startAnimation(animation);// 是用ImageView来显示动画的
		}
	}

	private Handler mhandler = new Handler(){

		@Override
		public void dispatchMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
				case NetMsgCode.The_network_link_failure:
					Toast.makeText(getApplicationContext(), R.string.network_link_failure, Toast.LENGTH_SHORT).show();;
					break;
				case NetMsgCode.The_network_link_success:
					switch(msg.arg1){
						case NetMsgCode.ACTION_GET_MSG:
							if(mcachetool != null)
								mcachetool.SaveMsgList((List<MessageInfo>) msg.obj);
							break;
						
						case NetMsgCode.ACTION_GET_IDINFO:
							if(mcachetool != null){
								mcachetool.SaveActionInfo((ActionInfo)msg.obj);
								actionInfo=(ActionInfo) msg.obj;
								mPageAdaptor.notifyDataSetChanged(actionInfo);
								Log.d("zzb","action in join flag ="+actionInfo.GetIsJoin());
								int result=actionInfo.GetIsJoin();
								if (action_flag.equals("2")) {
									join_action.setText(R.string.ended);
								} else {
									if(result==1){
										join_action.setText(R.string.running_action_join_in);
									}else{
										join_action.setText(R.string.running_action_join);
									}
								}
							}
							break;
						case NetMsgCode.ACTION_JOIN:
							if(mcachetool != null){
								int result=(Integer) msg.obj;
								if(result==0){
									join_action.setText(R.string.running_action_join_in);
								}else{
									Toast.makeText(mContext, R.string.running_action_apply, Toast.LENGTH_LONG).show();
									join_action.setText(R.string.running_action_join);
								}
							}
				}
					
				break;
			}
			super.dispatchMessage(msg);
		}
		
	};
	
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
	
	/** 
     *  有状态的 ，只会有前3个存在 其他销毁，  前1个， 中间， 下一个 
     */  
    public  class MyAdapter extends   FragmentStatePagerAdapter {  
    	public SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    	private ActionInfo mactioninfo;
    	
    	public void notifyDataSetChanged(ActionInfo actioninfo) {
    		mactioninfo = actioninfo;
    		super.notifyDataSetChanged();
    	}
    	
    	
    	public MyAdapter(FragmentManager fm,ActionInfo actioninfo) {  
            super(fm);  
    		mactioninfo = actioninfo;
        }  
  
        @Override  
        public int getCount() {  
            return NUM_ITEMS;  
        }  
        //得到每个item  
        @Override  
        public Fragment getItem(int position) {
        	Fragment mfragment = null;
	        	switch(position){
	        		case 0:
	        			mfragment = new ActionRuleFragment(mactioninfo,position);
	        			break;
	        		case 1:
	        			mfragment = new ActionRuleFragment(mactioninfo,position);//new ActionRuleFragment();
	        			break;
	        		case 2:
	        			mfragment = new ActionRankFragment(mactioninfo);
	        			break;
	        		case 3:
	        			mfragment = new ActionNoticeFragment(mactioninfo,position-1,action_flag);//new ActionNoticeFragment();
	        			break;
	        	}
            return mfragment;  
        }  
  
    	@Override
    	public int getItemPosition(Object object) {
    		return POSITION_NONE;
    	}
          
        // 初始化每个页卡选项  
        @Override  
        public Object instantiateItem(ViewGroup container, int position) {  
            // TODO Auto-generated method stub  
    		Fragment localFragment = (Fragment)super.instantiateItem(container, position);
    		registeredFragments.put(position, localFragment);
    		return localFragment;
        }  
          
        @Override  
        public void destroyItem(ViewGroup container, int position, Object object) {  
            System.out.println( "position Destory" + position);  
    		this.registeredFragments.remove(position);
            super.destroyItem(container, position, object);  
        }
    }   
}
