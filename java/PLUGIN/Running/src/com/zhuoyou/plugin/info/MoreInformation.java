package com.zhuoyou.plugin.info;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningTitleBar;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.weather.WeatherTools;

public class MoreInformation extends Activity implements OnClickListener {

	private LinearLayout rLayout;
	private EditText phoneNum, email;
	private TextView proviceTv, cityTv;
	private CityPopupWindow mProvice, mCity;
	String[] provice;
	String[] city;
	private int proviceIndex, cityIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_information);
		RunningTitleBar.getTitleBar(this, getResources().getString(R.string.more_information));
		initView();
		initDate();
	}

	private void initView() {
		rLayout = (LinearLayout) findViewById(R.id.more_info);
		phoneNum = (EditText) findViewById(R.id.phone_number);
		email = (EditText) findViewById(R.id.email);
		proviceTv = (TextView) findViewById(R.id.provice_tv);
		cityTv = (TextView) findViewById(R.id.city_tv);
	}

	private void initDate() {
		provice = WeatherTools.newInstance().selectProvice();
		proviceIndex = Tools.getProviceIndex(MoreInformation.this);
		cityIndex = Tools.getCityIndex(MoreInformation.this);
		if (proviceIndex != 10000) {
			city = WeatherTools.newInstance().selectCity(provice[proviceIndex]);
		}else{
			city = WeatherTools.newInstance().selectCity(provice[0]);
		}
		if (!getResources().getString(R.string.phone_hint).equals(
				Tools.getUsrName(MoreInformation.this))) {
			phoneNum.setText(Tools.getPhoneNum(MoreInformation.this));
			phoneNum.setSelection(phoneNum.getText().length());
		}
		if (!getResources().getString(R.string.email_hint).equals(
				Tools.getSignature(MoreInformation.this))) {
			email.setText(Tools.getEmail(MoreInformation.this));
			email.setSelection(email.getText().length());
		}
		if (proviceIndex != 10000) {
			proviceTv.setText(provice[proviceIndex]);
			proviceTv.setTextColor(Color.BLACK);
		}
		if (cityIndex != 10000) {
			cityTv.setText(city[cityIndex]);
			cityTv.setTextColor(Color.BLACK);
		}
	}
	
	private void hideKeyboard() {
		View focusView = MoreInformation.this.getCurrentFocus();
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
		final WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = 0.7f;
		switch (v.getId()) {
		case R.id.choice_provice:
			hideKeyboard();
			mProvice = new CityPopupWindow(this, new ProviceCityAdapter(this, provice), proviceIndex);
			mProvice.showAtLocation(rLayout, Gravity.BOTTOM
					| Gravity.CENTER_HORIZONTAL, 0, 0);
			getWindow().setAttributes(lp);
			mProvice.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					lp.alpha = 1.0f;
					getWindow().setAttributes(lp);
					proviceIndex = mProvice.getCityIndex();
					proviceTv.setText(provice[proviceIndex]);
					proviceTv.setTextColor(Color.BLACK);
				}
			});
			break;
		case R.id.choice_city:
			hideKeyboard();
			city = WeatherTools.newInstance().selectCity(provice[proviceIndex]);
			mCity = new CityPopupWindow(this, new ProviceCityAdapter(this, city), cityIndex);
			mCity.showAtLocation(rLayout, Gravity.BOTTOM
					| Gravity.CENTER_HORIZONTAL, 0, 0);
			getWindow().setAttributes(lp);
			mCity.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					lp.alpha = 1.0f;
					getWindow().setAttributes(lp);
					cityIndex = mCity.getCityIndex();
					cityTv.setText(city[cityIndex]);
					cityTv.setTextColor(Color.BLACK);
				}
			});
			break;
		case R.id.tBack:
			finish();
			break;
		case R.id.tDone:
			Tools.setPhoneNum(MoreInformation.this, phoneNum.getText()
					.toString());
			Tools.setEmail(MoreInformation.this, email.getText().toString());
			Tools.setProviceIndex(MoreInformation.this, proviceIndex);
			Tools.setCityIndex(MoreInformation.this, cityIndex);
			CloudSync.startSyncInfo();
			finish();
			break;
		default:
			break;
		}
	}

	public class ProviceCityAdapter extends BaseAdapter {

		String[] proviceCity;

		public ProviceCityAdapter(Context con, String[] pc) {
			proviceCity = pc;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return proviceCity.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return proviceCity[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView txtView = null;
			if (null == convertView) {
				convertView = new TextView(MoreInformation.this);
				txtView = (TextView) convertView;
				txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
				txtView.setPadding(10, 10, 10, 10);
				txtView.setTextColor(Color.BLACK);
				txtView.setGravity(Gravity.CENTER);
			}
			if (null == txtView) {
				txtView = (TextView) convertView;
			}

			txtView.setText(proviceCity[position]);
			return convertView;
		}

	}

}
