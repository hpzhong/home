package com.zhuoyou.plugin.running;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

public class HomePageAdapter extends FragmentStatePagerAdapter {
	private List<RunningItem> mRunningDates = new ArrayList<RunningItem>();
	public SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

	public void notifyDataSetChanged(List<RunningItem> lists) {
		mRunningDates = lists;
		super.notifyDataSetChanged();
	}

	public HomePageAdapter(FragmentManager fm, List<RunningItem> lists) {
		super(fm);
		mRunningDates = lists;
	}

	@Override
	public int getCount() {
		return mRunningDates.size();
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
		return HomePageItemFragment.newInstance(mRunningDates.get(arg0));
	}
}
