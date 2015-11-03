package com.zhuoyou.plugin.running;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

public class SleepPageAdapter extends FragmentStatePagerAdapter {
	private List<String> mDates = new ArrayList<String>();
	public SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

	public void notifyDataSetChanged(List<String> lists) {
		Log.d("3333","notifyDataSetChanged 1111");
		mDates = lists;
		super.notifyDataSetChanged();
	}

	public SleepPageAdapter(FragmentManager fm, List<String> lists) {
		super(fm);
		mDates = lists;
	}

	@Override
	public int getCount() {
		return mDates.size();
	}

	@Override
	public int getItemPosition(Object object) {
		return -2;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		this.registeredFragments.remove(position);
		super.destroyItem(container, position, object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment localFragment = (Fragment)super.instantiateItem(container, position);
		registeredFragments.put(position, localFragment);
		return localFragment;
	}

	@Override
	public Fragment getItem(int arg0) {
		return SleepPageItemFragment.newInstance(mDates.get(arg0));
	}
}
