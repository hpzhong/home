package com.zhuoyou.plugin.action;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.mainFrame.MineFragment;
import com.zhuoyou.plugin.running.Main;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;

public class MessageActivity extends Activity{
	
	private List<MessageInfo> mMsgListsRead;
	private List<MessageInfo> mMsgListsUnread;
	private ListView mUnReadList;
	private MessageAdapter unReadAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_layout);
		TextView tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.message);
		RelativeLayout im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		initListData();
		initView();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		Tools.setMsgState(this, false);
		if (MineFragment.mHandler != null) {
			Message msg = new Message();
			msg.what = MineFragment.MSG_UNREAD;
			MineFragment.mHandler.sendMessage(msg);
		}
		if (Main.mHandler != null) {
			Message msg = new Message();
			msg.what = Main.MSG_UNREAD;
			Main.mHandler.sendMessage(msg);
		}
	}

	private void initView(){
        mUnReadList = (ListView) findViewById(R.id.listUnRead);
        mMsgListsUnread.addAll(mMsgListsRead);
		unReadAdapter = new MessageAdapter(mMsgListsUnread);
		mUnReadList.setAdapter(unReadAdapter);
		
		mUnReadList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				MessageInfo info = unReadAdapter.getItem(position);
				Intent intent = new Intent(MessageActivity.this, MessageInfoActivity.class);
				intent.putExtra("msg_content", info.GetMsgContent());
				updateDateBase(info.getId(), 1);
				startActivity(intent);
			}
		});
	}
	
	private void initListData() {
		mMsgListsRead = new ArrayList<MessageInfo>();
		mMsgListsUnread = new ArrayList<MessageInfo>();
		Cursor cursor = getContentResolver().query(DataBaseContants.CONTENT_MSG_URI, new String[] { "_id", "msg_id", "content", "msg_type", "msg_time", "state" },
				null, null, DataBaseContants.ID+" DESC");
		cursor.moveToFirst();
		int count = cursor.getCount();
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				int state =  cursor.getInt(cursor.getColumnIndex(DataBaseContants.MSG_STATE));
				MessageInfo info = new MessageInfo(cursor.getInt(cursor.getColumnIndex(DataBaseContants.ID)), cursor.getInt(cursor.getColumnIndex(DataBaseContants.MSG_ID)), cursor.getString(cursor.getColumnIndex(DataBaseContants.MSG_CONTENT)), cursor.getInt(cursor.getColumnIndex(DataBaseContants.MSG_TYPE)), cursor.getString(cursor.getColumnIndex(DataBaseContants.MSG_TIME)), state);
				if (state == 0) {
					mMsgListsUnread.add(info);
				} else if (state == 1) {
					mMsgListsRead.add(info);
				}
				cursor.moveToNext();
			}
		}
		cursor.close();
		cursor = null;
	}
	
	// 更新数据库中的一条 体重信息
	private void updateDateBase(int id,int state) {
		ContentResolver cr = this.getContentResolver();
		ContentValues updateValues = new ContentValues();
		updateValues.put(DataBaseContants.MSG_STATE, state);
		cr.update(DataBaseContants.CONTENT_MSG_URI, updateValues, DataBaseContants.ID + " = ? ", new String[] { id+"" });
	}
	
	private class MessageAdapter extends BaseAdapter{
		
		private List<MessageInfo> mMsgLists; 
		
		public MessageAdapter(List<MessageInfo> mlist) {
			mMsgLists = mlist;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mMsgLists.size();
		}

		@Override
		public MessageInfo getItem(int position) {
			// TODO Auto-generated method stub
			return mMsgLists.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return mMsgLists.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;	
			if (convertView == null) {
				convertView = LayoutInflater.from(MessageActivity.this).inflate(R.layout.message_item, parent, false);
				holder = new ViewHolder();
				holder.value1 = (TextView) convertView.findViewById(R.id.msg_type);
				holder.value2 = (TextView) convertView.findViewById(R.id.msg_content);
				holder.value3 = (TextView) convertView.findViewById(R.id.msg_time);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (mMsgLists.get(position).getmState() == 0) {
				holder.value1.setTextColor(Color.BLACK);
				holder.value2.setTextColor(Color.BLACK);
				holder.value3.setTextColor(Color.BLACK);
			} else if (mMsgLists.get(position).getmState() == 1) {
				holder.value1.setTextColor(0xffc4c4c4);
				holder.value2.setTextColor(0xffc4c4c4);
				holder.value3.setTextColor(0xffc4c4c4);
			}
			holder.value1.setText(R.string.sys_info);
			holder.value2.setText(mMsgLists.get(position).GetMsgContent());
			holder.value3.setText(mMsgLists.get(position).getmMsgTime());
			return convertView;
		}
		
		private class ViewHolder {
			private TextView value1;
			private TextView value2;
			private TextView value3;
		}
		
	}
	
	
}
