package com.zhuoyou.plugin.info;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.running.InfoDialog;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningTitleBar;
import com.zhuoyou.plugin.running.Tools;

public class EditInformation extends Activity implements OnClickListener {
	LinearLayout editUsrinfo;
	LinearLayout editHead;
	ImageView face;
	TextView name;
	EditText nickname;
	EditText signature;
	GridView head;
	int[] headIcon;
	String[] headName;
	LinearLayout choiceSports;
	LinearLayout likeSports;
	ChoseLikeSportPopoWindow sportChose;
	List<String> sportType;
	int sportIndex;
	String[] sportArray = new String[100];
	int i = 0;
	int headIndex;
	String likeSportIndex=null;
	List<String> selectedIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_information);
		RunningTitleBar.getTitleBar(this, getResources().getString(R.string.edit_info));
		initView();
		initDate();
	}

	void initDate() {
		likeSportIndex=null;

		sportArray = getResources().getStringArray(R.array.whole_sport_type);
		headIcon = Tools.headIcon;
		headName = Tools.headName(this);

		if (Tools.getHead(this) != 6) {
			face.setImageResource(headIcon[Tools.getHead(this)]);
			name.setText(headName[Tools.getHead(this)]);
		}

		if (Tools.getUsrName(this).equals("")) {
			if (!Tools.getLoginName(this).equals("")) {
				nickname.setText(Tools.getLoginName(this));
			} else {
				nickname.setText(null);
			}
		} else {
			nickname.setText(Tools.getUsrName(this));
		}
		nickname.setSelection(nickname.getText().length());

		if (!Tools.getSignature(EditInformation.this).equals("")) {
			signature.setText(Tools.getSignature(EditInformation.this));
			signature.setSelection(signature.getText().length());
		}

		likeSportIndex=Tools.getLikeSportsIndex(EditInformation.this);
		String likeIndex[]=null;
		if(!likeSportIndex.equals("")){
			likeIndex=likeSportIndex.split(",");
			if (likeIndex.length > 0) {
				for (int i = 0; i < likeIndex.length; i++) {
					ImageView img = new ImageView(EditInformation.this);
					img.setImageResource(Tools.sportType[Integer.parseInt(likeIndex[i])]);
					likeSports.addView(img);
				}
			}
		}else{
			ImageView img = new ImageView(EditInformation.this);
			img.setImageResource(Tools.sportType[28]);
			likeSports.addView(img);
		}
	}

	void initView() {
		editUsrinfo = (LinearLayout) findViewById(R.id.edit_usrinfo);
		editHead = (LinearLayout) findViewById(R.id.edit_head);
		face = (ImageView) findViewById(R.id.face_logo);
		name = (TextView) findViewById(R.id.name);
		nickname = (EditText) findViewById(R.id.nickname);
		signature = (EditText) findViewById(R.id.signature);
		choiceSports = (LinearLayout) findViewById(R.id.choice_sports);
		likeSports = (LinearLayout) findViewById(R.id.like_sports);
	}

	void createDialog() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View imgEntryView = inflater.inflate(R.layout.choose_head, null);
		final InfoDialog mDialog = new InfoDialog(EditInformation.this,
				R.style.info_dialog);
		head = (GridView) imgEntryView.findViewById(R.id.head_edit);
		head.setAdapter(new HeadAdapter(EditInformation.this));
		head.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				face.setImageResource(headIcon[position]);
				name.setText(headName[position]);
				headIndex = position;
				mDialog.cancel();
			}
		});
		mDialog.addContentView(imgEntryView, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		mDialog.show();
	}

	void addLikeSports() {
		selectedIndex = new ArrayList<String>();
		final WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = 0.7f;
		sportChose = new ChoseLikeSportPopoWindow(EditInformation.this,likeSportIndex);
		sportChose.showAtLocation(editUsrinfo, Gravity.BOTTOM
				| Gravity.CENTER_HORIZONTAL, 0, 0);
		getWindow().setAttributes(lp);
		sportChose.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				lp.alpha = 1.0f;
				getWindow().setAttributes(lp);
				sportType = sportChose.getSport();
				for(int i=0;i<sportType.size();i++){
					selectedIndex.add(Integer.toString(Tools.getSportIndex(sportArray, sportType.get(i))));
				}
				for(int i=0;i<selectedIndex.size();i++){
					ImageView img = new ImageView(EditInformation.this);
					img.setImageResource(Tools.sportType[Integer.parseInt(selectedIndex.get(i))]);
					likeSports.addView(img);
					if(i== 0){
						likeSportIndex = selectedIndex.get(i);
					}else{
						likeSportIndex=likeSportIndex+","+selectedIndex.get(i);
					}
				}
				while(likeSports.getChildCount()>selectedIndex.size()){
					likeSports.removeViewAt(0);
					if(likeSports.getChildCount()<=selectedIndex.size()){
						break;
					}
				}
			}
		});
	}

	private void hideKeyboard() {
		View focusView = EditInformation.this.getCurrentFocus();
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null && focusView != null) {
			inputMethodManager.hideSoftInputFromWindow(
					focusView.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	@Override
	public void onClick(View v) {

		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.edit_head:
			createDialog();
			break;
		case R.id.choice_sports:
			hideKeyboard();
			addLikeSports();
			break;
		case R.id.tBack:
			finish();
			break;
		case R.id.tDone:
			Tools.setHead(EditInformation.this, headIndex);
			if(!Tools.getLoginName(EditInformation.this).equals("")){
				if (!nickname.getText().toString().equals(Tools.getLoginName(EditInformation.this))) {
					Tools.setUsrName(EditInformation.this, nickname.getText()
							.toString());
				}
			}else{
				Tools.setUsrName(EditInformation.this, nickname.getText()
						.toString());
			}
			
			Tools.setSignature(EditInformation.this, signature.getText()
					.toString());
			Tools.setLikeSportsIndex(EditInformation.this, likeSportIndex);
			CloudSync.startSyncInfo();
			finish();
			break;
		default:
			break;
		}
	}

	class HeadAdapter extends BaseAdapter {

		Context mContext;

		public HeadAdapter(Context c) {
			mContext = c;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return headIcon.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View mList;
			mList = LinearLayout.inflate(mContext, R.layout.choose_head_item,
					null);
			ImageView icon = (ImageView) mList.findViewById(R.id.head_icon);
			TextView name = (TextView) mList.findViewById(R.id.head_name);
			icon.setImageResource(headIcon[position]);
			name.setText(headName[position]);
			return mList;
		}

	}
}
