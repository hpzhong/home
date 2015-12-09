package com.zhuoyou.plugin.info;

import com.zhuoyou.plugin.add.TosGallery;
import com.zhuoyou.plugin.add.TosGallery.OnEndFlingListener;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.view.WheelView;
import android.content.Context;
import android.graphics.drawable.PaintDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.zhuoyou.plugin.info.MoreInformation.ProviceCityAdapter;;
public class CityPopupWindow extends PopupWindow implements OnEndFlingListener{
	private TextView tv_ok;
	private WheelView city;
	private View view;
	int cityIdex,choiceId;
	public CityPopupWindow(Context context,ProviceCityAdapter proviceCity,int index){
		view = LayoutInflater.from(context).inflate(R.layout.choose_city,null);
		tv_ok = (TextView) view.findViewById(R.id.tv_back);
		city = (WheelView) view.findViewById(R.id.city);
		cityIdex = index;
		tv_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				cityIdex = choiceId;
				dismiss();
			}
		});
		city.setSelection(index);
		city.setAdapter(proviceCity);
		city.setOnEndFlingListener(this);
		
		this.setContentView(view);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);
		
		this.setBackgroundDrawable(new PaintDrawable());
		this.setOutsideTouchable(true);
	}

	@Override
	public void onEndFling(TosGallery v) {
		switch (v.getId()) {
		case R.id.city:
			choiceId = city.getSelectedItemPosition();
			break;

		default:
			break;
		}
		
	}
	
	public int getCityIndex(){
		return cityIdex;
	}

}
