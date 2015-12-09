package com.zhuoyou.plugin.info;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.add.TosAdapterView;
import com.zhuoyou.plugin.add.TosAdapterView.OnItemSelectedListener;
import com.zhuoyou.plugin.add.TosGallery;
import com.zhuoyou.plugin.add.TosGallery.OnEndFlingListener;
import com.zhuoyou.plugin.address.CityModel;
import com.zhuoyou.plugin.address.DistrictModel;
import com.zhuoyou.plugin.address.ProvinceModel;
import com.zhuoyou.plugin.address.XmlParserHandler;
import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.view.WheelView;

public class GoodsAddressActivity extends Activity implements OnEndFlingListener, OnClickListener{
	private Context mContext;
	private String[] mProvinceDatas;
	private String[] mCitiyDatas;
	private String[] mDistrictDatas;
	private Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();
	private Map<String, String[]> mDistrictDatasMap = new HashMap<String, String[]>();
	private String mCurrentProviceName = "北京市";
	private String mCurrentCityName = "北京市";
	private String mCurrentDistrictName = "昌平区";	
	private int currentProvince = 0;
	private int currentCity = 0;
	private int currentDistrict = 0;

	private WheelView mViewProvince;
	private WheelView mViewCity;
	private WheelView mViewDistrict;
	
	private WheelTextAdapter provinceAdapter;
	private WheelTextAdapter cityAdapter;
	private WheelTextAdapter districtAdapter;	
	private RelativeLayout showAddress;	
	private EditText name;
	private EditText mobilePhone;
	private EditText detailed_address;
	private TextView addressText;
	private String consigneeName;
	private String consigneePhone;
	private String consigneeLocation;
	private String consigneeAddress;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.goods_address);
		mContext = RunningApp.getInstance().getApplicationContext();
		mHandler = new Handler();
		initView();
		initDate();
		mViewProvince.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,
					int position, long id) {
				currentProvince = mViewProvince.getSelectedItemPosition();
				provinceAdapter.SetSelecttion(currentProvince);
				provinceAdapter.notifyDataSetChanged();
				
			}
			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {

			}

		});
		mViewCity.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,
					int position, long id) {
				currentCity = mViewCity.getSelectedItemPosition();
				cityAdapter.SetSelecttion(currentCity);
				cityAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {
				
			}

		});
		
		mViewDistrict.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,
					int position, long id) {
				currentDistrict = mViewDistrict.getSelectedItemPosition();
				districtAdapter.SetSelecttion(currentDistrict);
				districtAdapter.notifyDataSetChanged();				
			}

			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
	}
		
	private void initView() {
		mViewProvince = (WheelView) findViewById(R.id.id_province);
		mViewCity = (WheelView) findViewById(R.id.id_city);
		mViewDistrict = (WheelView) findViewById(R.id.id_district);
		showAddress = (RelativeLayout) findViewById(R.id.show_address_select);
		addressText = (TextView) findViewById(R.id.show_select_address);
		name = (EditText) findViewById(R.id.consignee_name);
		mobilePhone = (EditText) findViewById(R.id.phone_number);
		detailed_address = (EditText) findViewById(R.id.detailed_address);
	}

	private void initDate(){
		consigneeName = Tools.getConsigneeName(mContext);
		name.setText(consigneeName);
		consigneePhone = Tools.getConsigneePhone(mContext);
		mobilePhone.setText(consigneePhone);
		consigneeLocation = Tools.getConsigneeAddress(mContext);
		if (!consigneeLocation.equals("")) {
			String[] temp = consigneeLocation.split(":");
			mCurrentProviceName = temp[0];
			mCurrentCityName = temp[1];
			mCurrentDistrictName = temp[2];	
			addressText.setText(mCurrentProviceName+mCurrentCityName+mCurrentDistrictName);
			if (temp.length == 4) {
				consigneeAddress = temp[3];	
				if (!consigneeAddress.equals(""))
					detailed_address.setText(consigneeAddress);
			}
		}
		initProvinceDatas();
		updateProvince();
	}

    protected void initProvinceDatas()
	{
		List<ProvinceModel> provinceList = null;
    	AssetManager asset = getAssets();
        try {
            InputStream input = asset.open("province_data.xml");
            // 创建一个解析xml的工厂对象
			SAXParserFactory spf = SAXParserFactory.newInstance();
			// 解析xml
			SAXParser parser = spf.newSAXParser();
			XmlParserHandler handler = new XmlParserHandler();
			parser.parse(input, handler);
			input.close();
			//获取解析出来的数据
			provinceList = handler.getDataList();
			//*/ 初始化默认选中的省、市、区
			if (provinceList!= null && !provinceList.isEmpty()) {
				for (int i = 0; i < provinceList.size(); i++) {
					if (provinceList.get(i).getName().equals(mCurrentProviceName)) {
						currentProvince = i;
					}
				}
				List<CityModel> cityList = provinceList.get(currentProvince).getCityList();
				if (cityList!= null && !cityList.isEmpty()) {
					for (int i = 0; i < cityList.size(); i++) {
						if (cityList.get(i).getName().equals(mCurrentCityName)) {
							currentCity = i;
						}
					}
					List<DistrictModel> districtList = cityList.get(currentCity).getDistrictList();
					if (districtList!= null && !districtList.isEmpty()) {
						for (int i = 0; i < districtList.size(); i++) {
							if (districtList.get(i).getName().equals(mCurrentDistrictName)) {
								currentDistrict = i;
							}
						}
					}
				}
			}
			mProvinceDatas = new String[provinceList.size()];
			for (int i = 0; i < provinceList.size(); i++) {
				mProvinceDatas[i] = provinceList.get(i).getName();
				List<CityModel> cityList = provinceList.get(i).getCityList();
				String[] cityNames = new String[cityList.size()];
				for (int j = 0; j < cityList.size(); j++) {
					cityNames[j] = cityList.get(j).getName();
					List<DistrictModel> districtList = cityList.get(j).getDistrictList();
					String[] distrinctNameArray = new String[districtList.size()];
					DistrictModel[] distrinctArray = new DistrictModel[districtList.size()];
					for (int k = 0; k < districtList.size(); k++) {
						DistrictModel districtModel = new DistrictModel(districtList.get(k).getName(), districtList.get(k).getZipcode());
						distrinctArray[k] = districtModel;
						distrinctNameArray[k] = districtModel.getName();
					}
					mDistrictDatasMap.put(cityNames[j], distrinctNameArray);
				}
				mCitisDatasMap.put(provinceList.get(i).getName(), cityNames);
        	}
        } catch (Throwable e) {  
            e.printStackTrace();  
        } finally {
        	
        } 
	}

	public void updateProvince(){
		mViewProvince.setOnEndFlingListener(this);
		mViewProvince.setSoundEffectsEnabled(true);
		provinceAdapter = new WheelTextAdapter(this, mProvinceDatas, 20);
		provinceAdapter.SetSelecttion(currentProvince);
		mViewProvince.setAdapter(provinceAdapter);
		mViewProvince.setSelection(currentProvince);
		mCitiyDatas = mCitisDatasMap.get(mCurrentProviceName);
		mViewCity.setOnEndFlingListener(this);
		mViewCity.setSoundEffectsEnabled(true);
		cityAdapter = new WheelTextAdapter(this, mCitiyDatas, 20);
		cityAdapter.SetSelecttion(currentCity);
		mViewCity.setAdapter(cityAdapter);
		mViewCity.setSelection(currentCity);
		mDistrictDatas = mDistrictDatasMap.get(mCurrentCityName);
		mViewDistrict.setOnEndFlingListener(this);
		mViewDistrict.setSoundEffectsEnabled(true);
		districtAdapter = new WheelTextAdapter(this, mDistrictDatas, 20);
		districtAdapter.SetSelecttion(currentDistrict);
		mViewDistrict.setAdapter(districtAdapter);
		mViewDistrict.setSelection(currentDistrict);
	}
	
	/**
	 * 根据当前的省，更新市WheelView 的信息
	 */
	private void updateCities() {
		mCurrentProviceName = mProvinceDatas[currentProvince];
		mCitiyDatas = mCitisDatasMap.get(mCurrentProviceName);
		currentCity = 0;
		cityAdapter.SetSelecttion(0);
		cityAdapter.setData(mCitiyDatas);
		cityAdapter.notifyDataSetChanged();
		mViewCity.setSelection(0);
		updateAreas();
	}

	/**
	 * 根据当前的市，更新区WheelView 的信息
	 */
	private void updateAreas() {
		mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[currentCity];
		mDistrictDatas = mDistrictDatasMap.get(mCurrentCityName);
		currentDistrict = 0;
		districtAdapter.SetSelecttion(0);
		districtAdapter.setData(mDistrictDatas);
		districtAdapter.notifyDataSetChanged();
		mViewDistrict.setSelection(0);
		mCurrentDistrictName = mDistrictDatasMap.get(mCurrentCityName)[currentDistrict];
	}

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_address:
			finish();
			break;
		case R.id.title_address:
			finish();
			break;
		case R.id.click_address:
			InputMethodManager inputMethodManager =(InputMethodManager)this.getApplicationContext().getSystemService(this.INPUT_METHOD_SERVICE); 
			inputMethodManager.hideSoftInputFromWindow(name.getWindowToken(), 0); 
			inputMethodManager.hideSoftInputFromWindow(mobilePhone.getWindowToken(), 0); 
			inputMethodManager.hideSoftInputFromWindow(detailed_address.getWindowToken(), 0); 
			
			if (showAddress.getVisibility() == View.GONE) {
				showAddress.setVisibility(View.VISIBLE);
			} else {
				showAddress.setVisibility(View.GONE);
			}
			break;
		case R.id.save_address:
			consigneeName = name.getText().toString();
			consigneePhone = mobilePhone.getText().toString();
			consigneeLocation = mCurrentProviceName + ":" + mCurrentCityName + ":" + mCurrentDistrictName + ":" + detailed_address.getText().toString();
			Tools.saveConsigneeInfo(consigneeName, consigneePhone, consigneeLocation);
			CloudSync.startSyncInfo();
			finish();
			break;
		case R.id.empty_select:
			if (showAddress.getVisibility() == View.VISIBLE) {
				showAddress.setVisibility(View.GONE);
			}
		default:
			break;
		}
	}
	
	@Override
	public void onBackPressed(){
		if (showAddress.getVisibility() == View.VISIBLE) {
			showAddress.setVisibility(View.GONE);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onEndFling(TosGallery v) {
		switch (v.getId()) {
		case R.id.id_province:
			currentProvince = mViewProvince.getSelectedItemPosition();
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					updateCities();
					addressText.setText(mCurrentProviceName+mCurrentCityName+mCurrentDistrictName);
				}
			}, 1000);
			break;
		case R.id.id_city:
			currentCity = mViewCity.getSelectedItemPosition();
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					updateAreas();
					addressText.setText(mCurrentProviceName+mCurrentCityName+mCurrentDistrictName);
				}
			}, 1000);
			break;
		case R.id.id_district:
			currentDistrict = mViewDistrict.getSelectedItemPosition();
			mCurrentDistrictName = mDistrictDatasMap.get(mCurrentCityName)[currentDistrict];
			addressText.setText(mCurrentProviceName+mCurrentCityName+mCurrentDistrictName);
			break;
		default:
			break;
		}
		
	}
	
}
