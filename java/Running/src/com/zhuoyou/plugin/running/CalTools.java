package com.zhuoyou.plugin.running;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;

public class CalTools {

	//食物
	private static final int[] mFoods = {R.string.rice, R.string.chocolate, R.string.ice_cream, R.string.fat, 
									R.string.cake, R.string.milk_tea, R.string.hamburger, R.string.large_fries};
	//对应热量单位千卡
	private static final int[] mCal = {210, 6, 300, 8, 174, 160, 400, 600};
    private static ArrayList<FoodItem> mFood_list;
	
	private static void init(Context mContext) {
		mFood_list = new ArrayList<FoodItem>();
		for (int i = 0; i < mFoods.length; i++) {
			FoodItem mItem = new FoodItem();
			mItem.setName(mContext.getString(mFoods[i]));
			mItem.setCal(mCal[i]);
			mFood_list.add(mItem);
		}
	}
	
	public static String getResultFromCal(Context mContext, int cal) {
		String result = "";
		float count = 0.0f;
		init(mContext);
		Random random = new Random();
		int index = random.nextInt(mFood_list.size());
		if (index >= 0 && index < mFood_list.size()) {
			count = (float)cal/mFood_list.get(index).getCal();
			//四舍五入取1位小数
			count = (float)(Math.round(count*10))/10;
			if (count*10%10 == 0) {
				result = (int)count + " " + mFood_list.get(index).getName();
			} else {
				result = count + " " + mFood_list.get(index).getName();
			}
		}
		return result;
	}
}
