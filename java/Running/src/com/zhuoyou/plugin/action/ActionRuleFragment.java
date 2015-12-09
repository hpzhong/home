package com.zhuoyou.plugin.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zhuoyou.plugin.info.GoodsAddressActivity;
import com.zhuoyou.plugin.rank.AsyncImageLoader;
import com.zhuoyou.plugin.rank.AsyncImageLoader.ImageCallback;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

public class ActionRuleFragment extends Fragment{
	
	private Context mContext = RunningApp.getInstance().getApplicationContext();
	
	private RelativeLayout show_warn;
	
	private TextView show_text;
	
	private ImageView image_hide;
	private View mView;
	private ActionImageView url_img;
	private ActionInfo actioninfo;
	private List<ActionPannelItemInfo> pannellist;
	private ActionPannelItemInfo mActionPannelItemInfo;
	private  int ActionPannelPosition = 0;
	private ScrollView mscroll;
	AsyncImageLoader mAsyncImageLoader;
	public ActionRuleFragment(){
		
	}
	
	public ActionRuleFragment(ActionInfo mm,int position){
		actioninfo = mm;
		if(actioninfo != null)
			pannellist = actioninfo.getPannel();
		ActionPannelPosition = position;
	}

	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView=inflater.inflate(R.layout.rule_fragment, container, false);
		mAsyncImageLoader = new AsyncImageLoader();
		mscroll = (ScrollView) mView.findViewById(R.id.myscroll); 
		initView();
		image_hide.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				show_warn.setVisibility(View.GONE);
			}
		});
		
		show_text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(mContext,GoodsAddressActivity.class);
				startActivity(intent);
			}
		});
		InitContent();
		url_img = (ActionImageView)mView.findViewById(R.id.my_net_img);

		return mView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		String phone=Tools.getConsigneePhone(mContext);
		
		String name=Tools.getConsigneeName(mContext);
		
		String address=Tools.getConsigneeAddress(mContext);
		
		if((phone!=null && !phone.equals("")) && ( name !=null && !name.equals("")) && (address!=null && !address.equals(""))){
			show_warn.setVisibility(View.GONE);
		}else{
			show_warn.setVisibility(View.VISIBLE);
		}
		if(mActionPannelItemInfo !=null){
			List<ActParagraph> mTmp = mActionPannelItemInfo.GetActParagraphList();
			if(mTmp != null && mTmp.size()>0){
				ActParagraph mm = mTmp.get(0);
				if(mm != null){
					String url = 	mm.GetImgUrl();
					if(url !=null&& !url.equals("")){

			        	try {
							url = url.substring(0, url.lastIndexOf("/")+1)+URLEncoder.encode(url.substring(url.lastIndexOf("/")+1),"UTF-8").replace("+", "%20");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Drawable drawable = null;
						drawable = mAsyncImageLoader.loadDrawable(url, new ImageCallback() {
							@Override
							public void imageLoaded(Drawable imageDrawable, String imageUrl) {
								if (url_img != null) {
									if (imageDrawable != null){
										url_img.setVisibility(View.VISIBLE);
										url_img.setImageDrawable(imageDrawable);
									}
									else
										url_img.setVisibility(View.GONE);
								}
							}				
						});
						if (drawable == null) {
							url_img.setVisibility(View.GONE);
						} else {
							url_img.setVisibility(View.VISIBLE);
							url_img.setImageDrawable(drawable);
						}		 						
					}
				}
			}
		}			
	}
	
	public void initView(){
		show_warn=(RelativeLayout) mView.findViewById(R.id.rule_perfect_personal_data);
		show_text=(TextView) mView.findViewById(R.id.onclik_personal_data);
		image_hide=(ImageView) mView.findViewById(R.id.hide_lay);
	}
	
	
	public void InitContent(){
		if(pannellist!=null&&pannellist.size()>ActionPannelPosition){
			mActionPannelItemInfo = pannellist.get(ActionPannelPosition);
			List<ActParagraph> mActParagraph = mActionPannelItemInfo.GetActParagraphList();
			if(mActParagraph != null&&mActParagraph.size()>0){
				for(int i = 0;i<mActParagraph.size();i++){
					ActParagraph mm = mActParagraph.get(i);
				}
			}
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
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
}
