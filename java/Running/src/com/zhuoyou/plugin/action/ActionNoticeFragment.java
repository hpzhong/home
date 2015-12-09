package com.zhuoyou.plugin.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhuoyou.plugin.rank.AsyncImageLoader;
import com.zhuoyou.plugin.rank.AsyncImageLoader.ImageCallback;
import com.zhuoyou.plugin.running.R;

public class ActionNoticeFragment extends Fragment{
	
	private View mView;
	
	private ActionInfo actioninfo;
	
	private List<ActionPannelItemInfo> pannellist;
	
	private ActionPannelItemInfo mActionPannelItemInfo;
	
	private ActionInfoAdaptor baseadaptor;
	
	private ListView mList;
	
	private String action_flag=null;  //0 表示为开始  ，1 表示正在进行中  2 表示结束了
	
	private ImageView image;
	
	private  int ActionPannelPosition = 0;
	
	private TextView mtitle;

	
	private ActionImageView url_img;
	AsyncImageLoader mAsyncImageLoader;

	public ActionNoticeFragment(){
		
	}
	
	public ActionNoticeFragment(ActionInfo mm,int position,String flag){
		actioninfo = mm;
		this.action_flag=flag;
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
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView=inflater.inflate(R.layout.notice_fragment, container, false);	
		mtitle = (TextView)mView.findViewById(R.id.mtitle);
		image = (ImageView) mView.findViewById(R.id.my_net_img);
		if (action_flag.equals("0")) {
			mtitle.setText(R.string.action_notice_title);
			image.setVisibility(View.GONE);
		}else{
			mtitle.setVisibility(View.GONE);
			image.setVisibility(View.VISIBLE);
		}
		InitContent();
		return mView;
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
		mAsyncImageLoader = new AsyncImageLoader();
		url_img = (ActionImageView)mView.findViewById(R.id.my_net_img);
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
