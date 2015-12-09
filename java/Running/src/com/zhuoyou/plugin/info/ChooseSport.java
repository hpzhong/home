package com.zhuoyou.plugin.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.running.R;

public class ChooseSport extends Activity {
	private ListView listView;
	private String likeSportIndex=null;
	private MyAdapter page_one_Adapter ;
	private String[] wordsData;
	private ArrayList<String> selected;
	private String[] sportArray = new String[100];
	private Map<String, Integer> state;
	private int[] imgData = { R.drawable.paobu, R.drawable.qixing,
			R.drawable.zuqiu, R.drawable.lanqiu, R.drawable.pashan,
			R.drawable.yumaoqiu, R.drawable.palouti, R.drawable.tiaowu,
			R.drawable.buxing, R.drawable.youyong, R.drawable.tiaosheng,
			R.drawable.paiqiu, R.drawable.pingpang, R.drawable.jianshencao,
			R.drawable.lunhua, R.drawable.quanji, R.drawable.wangqiu,
			R.drawable.gaoerfu, R.drawable.bangqiu, R.drawable.huaxue,
			R.drawable.danbanhuaxue, R.drawable.yujia, R.drawable.huachuan,
			R.drawable.baolingqiu,R.drawable.feibiao, R.drawable.tiaoshui,
			R.drawable.menqiu, R.drawable.chonglang, R.drawable.biqiu,
			R.drawable.ganlanqiu, R.drawable.jijian, R.drawable.banqiu,
			R.drawable.binghu, R.drawable.feipan
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_sport);
		TextView tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.chose_sport);
		RelativeLayout im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		Intent intent = this.getIntent();
		likeSportIndex = intent.getStringExtra("sportlike");
		wordsData = this.getResources().getStringArray(R.array.page_sport);
		sportArray = getResources().getStringArray(R.array.whole_sport_type);
		selected= new ArrayList<String>();
		state = new HashMap<String, Integer>();
		String likeIndex[]=null;
		if(!likeSportIndex.equals("")) {
			likeIndex=likeSportIndex.split(",");
			if (likeIndex.length > 0) {
				for (int i = 0; i < likeIndex.length; i++) {
					for (int j = 0; j < wordsData.length; j++) {
						String sport = wordsData[j];
						if(sport.equals(sportArray[Integer.parseInt(likeIndex[i])])) {
							if (!selected.contains(sport)) {
								selected.add(wordsData[j]);
								state.put(j+"", j);
							}
						}
					}
				}
			}
		}
		listView = (ListView) findViewById(R.id.sport_select_listview);
		page_one_Adapter = new MyAdapter(wordsData, imgData, state);
		listView.setAdapter(page_one_Adapter);
	}
	
	public void onClick(View view){
		switch (view.getId()) {
		case R.id.back:
			finish();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void finish() {
		Intent intent = new Intent();
		intent.putStringArrayListExtra("sports", selected);
        setResult(RESULT_OK, intent);
		super.finish();
	}
	
	private class MyAdapter extends BaseAdapter {
		private String[] wordsData;
		private int[] imgData;
		private Map<String, Integer> state;

		public MyAdapter(String[] wordsData, int[] imgData, Map<String, Integer> state) {
			// TODO Auto-generated constructor stub
			this.wordsData = wordsData;
			this.imgData = imgData;
			this.state = state;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return wordsData.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return wordsData[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewCache holder ;
			if (convertView == null) {
				convertView = LayoutInflater.from(ChooseSport.this).inflate(R.layout.list_item_choose_sport, parent, false);
				holder = new ViewCache();
				holder.im = (ImageView) convertView.findViewById(R.id.image_icon);
				holder.tv = (TextView) convertView.findViewById(R.id.sport_name);
				holder.isSelected = (ImageView) convertView.findViewById(R.id.sport_select);
				holder.relative = (RelativeLayout) convertView.findViewById(R.id.listview_item);
				convertView.setTag(holder);
			} else {
				holder = (ViewCache) convertView.getTag();
			}
			holder.im.setImageResource(imgData[position]);
			holder.tv.setText(wordsData[position]);
			if (state.get(String.valueOf(position)) == null) {
				holder.isSelected.setVisibility(View.GONE);
			} else {
				holder.isSelected.setVisibility(View.VISIBLE);
			}
			holder.relative.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(holder.isSelected.getVisibility() == View.VISIBLE){
						holder.isSelected.setVisibility(View.GONE);
						for(int i=0;i<selected.size();i++){
							if(wordsData[position].equals(selected.get(i))){
								selected.remove(i);
								state.remove(position+"");
							}
						}
					}else if(holder.isSelected.getVisibility() == View.GONE){
						if(selected.size()<4){
							holder.isSelected.setVisibility(View.VISIBLE);
							selected.add(wordsData[position]);
							state.put(position+"", position);
						} else {
							Toast.makeText(ChooseSport.this, R.string.like_sport_limite, Toast.LENGTH_SHORT).show();
						}
					}
				}
			});
			return convertView;
		}

		private class ViewCache {
			ImageView im;
			TextView tv;
			ImageView isSelected;
			RelativeLayout relative;
		}

	}
}
