package com.zhuoyou.plugin.resideMenu;


import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;

public class ApplicationIntroduingActivity extends Activity {
	private List<View> viewGroup = new ArrayList<View>();
	private View appView1,appView2,appView3,appView4;
	private ViewPager viewPager;
	private TextView tvExperienceRightNow;
	@SuppressLint("InflateParams") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.application_introducing);
		
		viewPager = (ViewPager) findViewById(R.id.app_introduce_viewpage);
		@SuppressWarnings("static-access")
		LayoutInflater layoutInflater = getLayoutInflater().from(this);
		appView1 = layoutInflater.inflate(R.layout.app_view1, null);
		appView2 = layoutInflater.inflate(R.layout.app_view2, null);
		appView3 = layoutInflater.inflate(R.layout.app_view3, null);
		appView4 = layoutInflater.inflate(R.layout.app_view4, null);
		tvExperienceRightNow = (TextView) appView4.findViewById(R.id.experience_rightnow);
		
		viewGroup.add(appView1);
		viewGroup.add(appView2);
		viewGroup.add(appView3);
		viewGroup.add(appView4);
		viewPager.setAdapter(new MyPagerAdapter(viewGroup));
		tvExperienceRightNow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {//界面返回上一级
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	private class MyPagerAdapter extends PagerAdapter{
		public List<View> mlist;

		public MyPagerAdapter(List<View> mlist) {
			this.mlist = mlist;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager)container).removeView(mlist.get(position));
		}

		@Override
		public void finishUpdate(ViewGroup container) {
			super.finishUpdate(container);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			((ViewPager)container).addView(mlist.get(position), 0);
			return mlist.get(position);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
			super.restoreState(state, loader);
		}

		@Override
		public int getCount() {
			return mlist.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

	}
	
}
