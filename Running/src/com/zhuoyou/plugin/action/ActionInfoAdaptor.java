package com.zhuoyou.plugin.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhuoyou.plugin.rank.AsyncImageLoader;
import com.zhuoyou.plugin.rank.AsyncImageLoader.ImageCallback;
import com.zhuoyou.plugin.running.R;

public class ActionInfoAdaptor extends BaseAdapter {

	private Context mcontext;
	public AsyncImageLoader mAsyncImageLoader;
	private List<ActParagraph> mActParagraphlist;
	private ListView mListView;
	
	public ActionInfoAdaptor(Context context,ActionPannelItemInfo mm,ListView listview){
		mcontext = context;
		if(mm != null)
			mActParagraphlist = mm.GetActParagraphList();
		mListView = listview;
		mAsyncImageLoader = new AsyncImageLoader();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int count = 0;
		if(mActParagraphlist != null){
			count = mActParagraphlist.size();
		}
		return count;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(mActParagraphlist != null){
			return mActParagraphlist.get(position);
		}else{
			return null;
		}
		
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
			ViewHolder viewHolder;  
			Drawable drawable = null;

	        // TODO Auto-generated method stub  
	        if (convertView == null) {  
	        	convertView = LayoutInflater.from(mcontext).inflate(R.layout.actioninfo_item, null);  
	            viewHolder = new ViewHolder();  
	            
	            viewHolder.Pannel_pre = (ImageView) convertView.findViewById(R.id.introduce_img);  
	            viewHolder.Pannel_ind = (TextView) convertView.findViewById(R.id.introduce);
	            convertView.setTag(viewHolder);  
	        } else {  
	            viewHolder = (ViewHolder) convertView.getTag();  
	        }  
	        ActParagraph mm = mActParagraphlist.get(position);
	        //item display set 
	        if(viewHolder.Pannel_pre != null){
	        	String url = mm.GetImgUrl();
	        	try {
					url = url.substring(0, url.lastIndexOf("/")+1)+URLEncoder.encode(url.substring(url.lastIndexOf("/")+1),"UTF-8").replace("+", "%20");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	viewHolder.Pannel_pre.setTag(url);
				drawable = mAsyncImageLoader.loadDrawable(url, new ImageCallback() {
					@Override
					public void imageLoaded(Drawable imageDrawable, String imageUrl) {
						ImageView imageViewByTag = (ImageView) mListView.findViewWithTag(imageUrl);  
						if (imageViewByTag != null) {
							if (imageDrawable != null)
								imageViewByTag.setImageDrawable(imageDrawable);
							else
								imageViewByTag.setImageResource(R.drawable.action_2);
						}
					}				
				});
				if (drawable == null) {
					viewHolder.Pannel_pre.setImageResource(R.drawable.action_2);
				} else {
					viewHolder.Pannel_pre.setImageDrawable(drawable);
				}

	        	
	        }
	        String title = mm.GetContent();
	        if(title != null&&!title.equals(""))
	        	viewHolder.Pannel_ind.setText(mm.GetContent());  
	        else
	        	viewHolder.Pannel_ind.setVisibility(View.GONE);  	        	
	        return convertView;  
	}

	final static class ViewHolder{
		
		public TextView Pannel_ind;
		
		public ImageView Pannel_pre;
	}	

	
}
