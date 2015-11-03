package com.zhuoyou.plugin.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

public class ActionAdaptor extends BaseAdapter{


	private List<ActionListItemInfo> mylist =null;
	private Context mcontext;
	private static final long VALID_VALUE = -1;
	private ListView  mListView;
	private CacheTool mcachetool;
	public AsyncImageLoader mAsyncImageLoader;
    private boolean IS_NOACTION = false;

	public ActionAdaptor(Context context,ListView list,CacheTool cachetool){
		mcontext = context;
		mListView = list;
		mcachetool = cachetool;
		mAsyncImageLoader = new AsyncImageLoader();
		SetMyListItem();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int listitem_count = 0;
		if(mylist != null){
			listitem_count = mylist.size();
			if(listitem_count == 0)
				IS_NOACTION = true;
		}else{
			IS_NOACTION = true;
		}
		if(IS_NOACTION)
			listitem_count = 1;
		return listitem_count;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(mylist!= null && position < mylist.size())
			return mylist.get(position);
		else
			return null;
	}

	@Override
	public long getItemId(int position) {
		//change here for action id??
		if(IS_NOACTION){
			return VALID_VALUE;
		}else{
			if(mylist!= null && position < mylist.size())
				return mylist.get(position).GetActivtyId();
			else{
				return VALID_VALUE;		
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;  
		Drawable drawable = null;

        // TODO Auto-generated method stub  
        if (convertView == null) {  
        	convertView = LayoutInflater.from(mcontext).inflate(R.layout.action_listitem, null);  
            viewHolder = new ViewHolder();  
            
            viewHolder.Action_preview = (ImageView) convertView.findViewById(R.id.action_preview);  
            viewHolder.Action_Join =  convertView.findViewById(R.id.is_join);
            viewHolder.Action_Num = (TextView) convertView.findViewById(R.id.action_num);
            viewHolder.Action_Time = (TextView) convertView.findViewById(R.id.action_time);
            viewHolder.Action_State = (TextView) convertView.findViewById(R.id.action_state);
            viewHolder.Action_FillView = convertView.findViewById(R.id.fill_view);
            convertView.setTag(viewHolder);  
        } else {  
            viewHolder = (ViewHolder) convertView.getTag();  
        }  
        if(IS_NOACTION){
        	viewHolder.Action_preview.setImageResource(R.drawable.action_ready);
            viewHolder.Action_Join.setVisibility(View.GONE);
            viewHolder.Action_Num.setVisibility(View.GONE);
            viewHolder.Action_Time.setVisibility(View.GONE);
            viewHolder.Action_State.setVisibility(View.GONE);
            viewHolder.Action_FillView.setVisibility(View.GONE);
        }else{
	        //item display set 
            ActionListItemInfo mm = mylist.get(position);
	        if(viewHolder.Action_preview != null){
	        	String url = mm.GetActiviyImgUrl();
	        	try {
					url = url.substring(0, url.lastIndexOf("/")+1)+URLEncoder.encode(url.substring(url.lastIndexOf("/")+1),"UTF-8").replace("+", "%20");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	viewHolder.Action_preview.setTag(url);
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
					viewHolder.Action_preview.setImageResource(R.drawable.action_2);
				} else {
					viewHolder.Action_preview.setImageDrawable(drawable);
				}
	
	        	
	        }
	        viewHolder.Action_Time.setText(GetActionTimeString(mm));  
	        switch(GetActionState(mm)){
	        	case 1:
	    	        viewHolder.Action_State.setBackgroundResource(R.drawable.action_state_bebeginning);
	    	        viewHolder.Action_State.setText(R.string.to_start);
	    	        viewHolder.Action_Num.setText(mm.GetActivtyNum()+mcontext.getResources().getString(R.string.people_registered));
	        		break;
	        	case 2:
	    	        viewHolder.Action_State.setBackgroundResource(R.drawable.action_state_ongoing);
	    	        viewHolder.Action_State.setText(R.string.in_progress);
	    	        viewHolder.Action_Num.setText(mm.GetActivtyNum()+mcontext.getResources().getString(R.string.people_joining));
	        		break;
	        	case 3:
	    	        viewHolder.Action_State.setBackgroundResource(R.drawable.action_state_closed);
	    	        viewHolder.Action_State.setText(R.string.ended);
	    	        viewHolder.Action_Num.setText(mm.GetActivtyNum()+mcontext.getResources().getString(R.string.people_involved));
	        		break;
	        }
	    	
        }

//        we can't get this flag here ,please ask miaowenzhi.
//        viewHolder.Action_Join.setVisibility(mm.GetActiviyFlag()?View.VISIBLE:View.GONE);
        return convertView;  
        }
	
	
	public void SetMyListItem(){
		if(mcachetool != null){
			if(mylist !=null&& mylist.size() != 0 )
				mylist.clear();
			mylist = mcachetool.GetActionListItemDate();
			if(mylist != null&&mylist.size()>0){
				IS_NOACTION = false;
			}else{
				IS_NOACTION = true;
			}
		}
	}
	
	public void SetMyListItem(List<ActionListItemInfo> mlist){
		if(mcachetool != null){
			if(mylist !=null&& mylist.size() != 0 )
				mylist.clear();
			mylist.addAll(mlist);
			if(mylist != null&&mylist.size()>0){
				IS_NOACTION = false;
			}else{
				IS_NOACTION = true;
			}
		}
	}
	
	public void AddListitem(List<ActionListItemInfo> mlist){
		if(mcachetool != null){
			mylist.addAll(mlist);
			if(mylist != null&&mylist.size()>0){
				IS_NOACTION = false;
			}else{
				IS_NOACTION = true;
			}
		}
	}
	
	public String GetActionTimeString(ActionListItemInfo mm){
		StringBuilder atcion_time = new StringBuilder();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  

		String start = mm.GetActivtyStartTime();
		String cur = mm.GetActivtyCurTime();
		String end = mm.GetActivtyEndTime();
		Date start_date = null;
		Date cur_date = null;
		Date end_date = null;
		
		try {
			start_date = sdf1.parse(start);
			cur_date = sdf1.parse(cur);
			end_date = sdf1.parse(end);
			
			long st = start_date.getTime();
			long ct = cur_date.getTime();
			long et =  end_date.getTime();
			
			long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
			long nh = 1000 * 60 * 60;// 一小时的毫秒数
			long nm = 1000 * 60;// 一分钟的毫秒数
			long ns = 1000;// 一秒钟的毫秒数
			
			if(ct<st){
				//未开始
				int leave = (int) (st - ct);
				long day = leave / nd;
				long hour = leave % nd / nh + day * 24;
				long min = leave % nd % nh / nm + day * 24 * 60;
				long sec = leave % nd % nh % nm / ns + day * 24 * 60 * 60;
				atcion_time.append(mcontext.getResources().getString(R.string.to_start_have));
				if(day != 0){
					atcion_time.append(day);
					atcion_time.append(mcontext.getResources().getString(R.string.day1));
					atcion_time.append(hour);
					atcion_time.append(mcontext.getResources().getString(R.string.hour));
					atcion_time.append(min);
					atcion_time.append(mcontext.getResources().getString(R.string.minute));
				}else if(hour != 0){
					atcion_time.append(hour);
					atcion_time.append(mcontext.getResources().getString(R.string.hour));
					atcion_time.append(min);
					atcion_time.append(mcontext.getResources().getString(R.string.minute));
				}else if(min != 0){
					atcion_time.append(min);
					atcion_time.append(mcontext.getResources().getString(R.string.minute));
				}else if(sec != 0){
					atcion_time.append(sec);
					atcion_time.append(mcontext.getResources().getString(R.string.gps_second));
				}
				
			}else if(ct<et){
				//正在进行中
				int leave = (int) (et - ct);
				long day = leave / nd;
				long hour = leave % nd / nh + day * 24;
				long min = leave % nd % nh / nm + day * 24 * 60;
				long sec = leave % nd % nh % nm / ns + day * 24 * 60 * 60;
				atcion_time.append(mcontext.getResources().getString(R.string.to_end));
				if(day != 0){
					atcion_time.append(day);
					atcion_time.append(mcontext.getResources().getString(R.string.day1));
					atcion_time.append(hour);
					atcion_time.append(mcontext.getResources().getString(R.string.hour));
					atcion_time.append(min);
					atcion_time.append(mcontext.getResources().getString(R.string.minute));
				}else if(hour != 0){
					atcion_time.append(hour);
					atcion_time.append(mcontext.getResources().getString(R.string.hour));
					atcion_time.append(min);
					atcion_time.append(mcontext.getResources().getString(R.string.minute));
				}else if(min != 0){
					atcion_time.append(min);
					atcion_time.append(mcontext.getResources().getString(R.string.minute));
				}else if(sec != 0){
					atcion_time.append(sec);
					atcion_time.append(mcontext.getResources().getString(R.string.gps_second));
				}
			}else if (ct>et){
				//已结束
				Calendar cld = Calendar.getInstance();
				cld.setTime(end_date);
				int month = (cld.get(Calendar.MONTH)+1);
				int day = cld.get(Calendar.DAY_OF_MONTH);
				int hour = cld.get(Calendar.HOUR_OF_DAY);
		        String language = mcontext.getResources().getConfiguration().locale.getLanguage();
		        if (language.endsWith("en")) {
		        	atcion_time.append(translateToEn(month));
		        	atcion_time.append(" ");
		        	atcion_time.append(day);
		        	atcion_time.append(" ");
					atcion_time.append(hour);
					atcion_time.append(mcontext.getResources().getString(R.string.clock));
		        	atcion_time.append(" ");
					atcion_time.append(mcontext.getResources().getString(R.string.to_finish));
		        } else {
					atcion_time.append(month);
					atcion_time.append(mcontext.getResources().getString(R.string.mouth));
					atcion_time.append(day);
					atcion_time.append(mcontext.getResources().getString(R.string.Sunday));
					atcion_time.append(hour);
					atcion_time.append(mcontext.getResources().getString(R.string.clock));
					atcion_time.append(mcontext.getResources().getString(R.string.to_finish));
		        }
			}	
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return atcion_time.toString();
	}
	
	private String translateToEn(int month) {
		String monthEn = "";
		switch(month) {
		case 1:
			monthEn = "January";
			break;
		case 2:
			monthEn = "February";
			break;
		case 3:
			monthEn = "March";
			break;
		case 4:
			monthEn = "April";
			break;
		case 5:
			monthEn = "May";
			break;
		case 6:
			monthEn = "June";
			break;
		case 7:
			monthEn = "July";
			break;
		case 8:
			monthEn = "August";
			break;
		case 9:
			monthEn = "September";
			break;
		case 10:
			monthEn = "October";
			break;
		case 11:
			monthEn = "November";
			break;
		case 12:
			monthEn = "December";
			break;
		}
		return monthEn;
	}

	/*
	 * @param:ActionListItemInfo
	 * return: state of action:
	 * 					1:还未开始;
	 * 					2:正在进行
	 * 					3:已经结束
	 */
	public int GetActionState(ActionListItemInfo mm){
		int state  = 0;
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  

		String start = mm.GetActivtyStartTime();
		String cur = mm.GetActivtyCurTime();
		String end = mm.GetActivtyEndTime();
		Date start_date = null;
		Date cur_date = null;
		Date end_date = null;
		
		try {
			start_date = sdf1.parse(start);
			cur_date = sdf1.parse(cur);
			end_date = sdf1.parse(end);

			long st = start_date.getTime();
			long ct = cur_date.getTime();
			long et =  end_date.getTime();
			if(ct<st){
				//未开始
				state = 1;
			}else if(ct<et){
				//正在进行中
				state = 2;
			}else if (ct>et){
				//已结束
				state = 3;
			}	
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return state;
	}
	
	static class ViewHolder{
		//活动预览图
		public ImageView Action_preview;
		//活动参与人数
		public TextView Action_Num;
		//活动开始/结束时间
		public TextView Action_Time;
		//活动的状态：即将开始，正在进行，已结束
		public TextView Action_State;
		//活动才加标示，标示已参加
		public View Action_Join;
		//填充视图
		public View Action_FillView;
	}	
}
