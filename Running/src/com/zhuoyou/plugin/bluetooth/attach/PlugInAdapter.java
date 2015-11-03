package com.zhuoyou.plugin.bluetooth.attach;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;

public class PlugInAdapter extends BaseAdapter {
	private Context mCtx;
	private List<PlugBean> mPlugLists = new ArrayList<PlugBean>();
	private static final int MAX_H_NUM = 3;

	public PlugInAdapter(Context ctx, List<PlugBean> lists) {
		mCtx = ctx;
		mPlugLists = lists;
	}

	@Override
	public int getCount() {
		int size = mPlugLists.size();
		size = (size + 2) / MAX_H_NUM;
		return size;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		View root = LayoutInflater.from(mCtx).inflate(R.layout.main_plug_gv_item, null);
		RelativeLayout[] roots = new RelativeLayout[MAX_H_NUM];
		ImageView[] icons = new ImageView[MAX_H_NUM];
		ImageView[] status = new ImageView[MAX_H_NUM];
		TextView[] names = new TextView[MAX_H_NUM];
		ImageView[] uninstalls = new ImageView[MAX_H_NUM];
		
		findViews(root, roots, icons, status, names, uninstalls);

		int max = MAX_H_NUM;
		if (getCount() == (arg0 + 1)) {
			max = mPlugLists.size() % MAX_H_NUM;
			if (max == 0) {
				max = MAX_H_NUM;
			}
		}

		for (int i = 0; i < max; i++) {
			final int index = arg0 * MAX_H_NUM + i;
			PlugBean item = mPlugLists.get(index);

			if (item.isPreInstall && !item.isSystem) {
				if (!item.isInstalled) {
					status[i].setVisibility(View.VISIBLE);
				}
			}

			if (item.isSystem) {
				item.setBitmapId(icons[i]);
			} else {
				item.setBitmap(icons[i]);
			}
			item.setTitle(names[i]);

			if (BTPluginActivity.isEditMode) {
				if (item.isPreInstall && !item.isSystem && item.isInstalled)
					uninstalls[i].setVisibility(View.VISIBLE);
			} else {
				uninstalls[i].setVisibility(View.GONE);
			}
			uninstalls[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					BTPluginActivity.onUninstallClick(index);
				}				
			});
			
			roots[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					BTPluginActivity.onClick(index);
				}
			});
		}

		return root;
	}

	private void findViews(View root, RelativeLayout[] roots, ImageView[] icons, ImageView[] status, TextView names[], ImageView[] uninstalls) {
		int index = 0;

		roots[index] = (RelativeLayout) root.findViewById(R.id.gv_item_root1);
		icons[index] = (ImageView) root.findViewById(R.id.gv_item_icon1);
		status[index] = (ImageView) root.findViewById(R.id.gv_item_staus1);
		status[index].setVisibility(View.GONE);
		names[index] = (TextView) root.findViewById(R.id.gv_item_name1);
		uninstalls[index++] = (ImageView) root.findViewById(R.id.gv_item_delete1);

		roots[index] = (RelativeLayout) root.findViewById(R.id.gv_item_root2);
		icons[index] = (ImageView) root.findViewById(R.id.gv_item_icon2);
		status[index] = (ImageView) root.findViewById(R.id.gv_item_staus2);
		status[index].setVisibility(View.GONE);
		names[index] = (TextView) root.findViewById(R.id.gv_item_name2);
		uninstalls[index++] = (ImageView) root.findViewById(R.id.gv_item_delete2);

		roots[index] = (RelativeLayout) root.findViewById(R.id.gv_item_root3);
		icons[index] = (ImageView) root.findViewById(R.id.gv_item_icon3);
		status[index] = (ImageView) root.findViewById(R.id.gv_item_staus3);
		status[index].setVisibility(View.GONE);
		names[index] = (TextView) root.findViewById(R.id.gv_item_name3);
		uninstalls[index++] = (ImageView) root.findViewById(R.id.gv_item_delete3);
	}
	
	public void notify(List<PlugBean> lists) {
		mPlugLists = lists;
		notifyDataSetChanged();
	}
}
