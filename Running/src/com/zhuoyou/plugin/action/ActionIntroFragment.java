package com.zhuoyou.plugin.action;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.info.GoodsAddressActivity;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

public  class ActionIntroFragment extends Fragment{
	private Context mContext = RunningApp.getInstance().getApplicationContext();	
	private RelativeLayout show_warn;	
	private TextView show_text;	
	private ImageView image_hide;	
	private View mView;
	private TextView mtitle;
	private ListView mlist;
	private ActionInfoAdaptor baseadaptor;
	private ActionInfo actioninfo;
	private List<ActionPannelItemInfo> pannellist;
	private ActionPannelItemInfo mActionPannelItemInfo;
	private  int ActionPannelPosition = 0;
	public ActionIntroFragment(){
		
	}
	
	public ActionIntroFragment(ActionInfo mm,int position){
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
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	public interface OnnetDateArride{
		public void Updatedate(ActionPannelItemInfo mm);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView=inflater.inflate(R.layout.intro_fragment, container, false);	
		mtitle = (TextView)mView.findViewById(R.id.my_title);
		mlist = (ListView)mView.findViewById(R.id.mylist);
		if(pannellist!=null&&pannellist.size()>ActionPannelPosition){
			mActionPannelItemInfo = pannellist.get(ActionPannelPosition);
			mtitle.setText(mActionPannelItemInfo.GetPannelTitle());
		}
		baseadaptor = new ActionInfoAdaptor(getActivity(),mActionPannelItemInfo,mlist);
		mlist.setAdapter(baseadaptor);
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
	}
	
	public void initView(){
		show_warn=(RelativeLayout) mView.findViewById(R.id.intro_perfect_personal_data);
		show_text=(TextView) mView.findViewById(R.id.onclik_personal_data);
		image_hide=(ImageView) mView.findViewById(R.id.hide_lay);
		
	}
}
