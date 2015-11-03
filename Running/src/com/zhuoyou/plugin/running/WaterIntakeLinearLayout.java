package com.zhuoyou.plugin.running;



import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class WaterIntakeLinearLayout extends LinearLayout {
	private ImageView mWaterTenIcon;
	private ImageView mWaterBitIcon;
	private static int[] water_number = { R.drawable.water_zero, R.drawable.water_one, R.drawable.water_two,
		R.drawable.water_three, R.drawable.water_four, R.drawable.water_five, R.drawable.water_six,
		R.drawable.water_seven, R.drawable.water_eight, R.drawable.water_nine };
	public WaterIntakeLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.water_num_layout, this, true);
		mWaterTenIcon = (ImageView) findViewById(R.id.water_ten);
		mWaterBitIcon = (ImageView) findViewById(R.id.water_bit);
		// TODO Auto-generated constructor stub
	}
	
	public void setWaterNumber(int num) {
		// TODO Auto-generated method stub
		if(num < 10){
			mWaterTenIcon.setVisibility(GONE);
			mWaterBitIcon.setVisibility(VISIBLE);
			mWaterBitIcon.setImageResource(water_number[num]);
		}else if(num >= 10 && num < 100){
			mWaterTenIcon.setVisibility(VISIBLE);
			mWaterBitIcon.setVisibility(VISIBLE);
			mWaterTenIcon.setImageResource(water_number[num / 10]);
			mWaterBitIcon.setImageResource(water_number[num % 10]);
		}
	}

}
