package com.zhuoyou.plugin.running;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.database.Cursor;
import android.database.CursorWrapper;
import com.zhuoyou.plugin.running.SortCursor.SortEntry;

public class SortCursor extends CursorWrapper implements Comparator<SortEntry> {
	public Cursor mCursor;

	public static class SortEntry {
		public String key;
		public int order;
	}
	int mpos = 0; 
	ArrayList<SortEntry> sortList = new ArrayList<SortCursor.SortEntry>();

	public SortCursor(Cursor cursor, String columnName) {
		super(cursor);
		// TODO Auto-generated constructor stub
		mCursor = cursor;
		
		if (mCursor != null && mCursor.getCount() > 0) {
			int i = 0;
			int column = cursor.getColumnIndexOrThrow(columnName);
			for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor
					.moveToNext(), i++) {
				SortEntry entry = new SortEntry();
				entry.key = cursor.getString(column);
				entry.order = i;
				sortList.add(entry);
			}
		}
		Collections.sort(sortList,this);
	}
	
	public boolean moveToPosition(int position){
		if(position >= 0 && position < sortList.size()){
			mpos = position;
			int order = sortList.get(position).order;
			return mCursor.moveToPosition(order);
		}
		if(position < 0){
			mpos = -1;
		}
		if(position >= sortList.size()){
			mpos = sortList.size();
		}
		return mCursor.moveToPosition(position);
	}
	
	public boolean moveToFirst(){
		return moveToPosition(0);
	}
	
	public boolean moveToLast(){
		return moveToPosition(getCount() -1); 
	}
	
	public boolean moveToNext(){
		return moveToPosition(mpos + 1);
	}
	
	public boolean moveToPrevious(){
		return moveToPosition(mpos - 1);
	}
	
	public boolean move(int offset){
		return moveToPosition(mpos + offset);
	}
	
	public int getPosition(){
		return mpos;
	}
	
	
	@Override
	public int compare(SortEntry lhs, SortEntry rhs) {
		// TODO Auto-generated method stub
		String[] arr = lhs.key.split(":");
		String[] brr = rhs.key.split(":");
		int arrInt = (Integer.parseInt(arr[0])) * 60 + getStringInt(arr[1]);
		int brrInt = (Integer.parseInt(brr[0])) * 60 + getStringInt(brr[1]);
		if (arrInt > brrInt) {
			return -1;
		} else if (arrInt < brrInt) {
			return 1;
		}
		return 0;
	}

	private int getStringInt(String time) {
		int res = 0;
		StringBuffer buffer = new StringBuffer();
		char c;
		c = time.charAt(0);
		if (c == '0') {
			buffer.append(time.charAt(1));
			res = Integer.parseInt(buffer.toString());
		} else {
			res = Integer.parseInt(time);
		}
		return res;
	}
}
