package com.zhuoyou.plugin.add;

import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddWords extends Activity implements OnClickListener {
	private EditText ed;
	private TextView tv_add_words;

	private ImageView im_complete, im_delete, im_edit_ok,im_cancle;

	private Intent intent;
	private String wordExplain;
	private String date;
	private long id;
	private RelativeLayout im_back;
	private boolean hasChanged = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_word);
		ed = (EditText) findViewById(R.id.ed_some_words);
		im_complete = (ImageView) findViewById(R.id.im_complete);
		im_back = (RelativeLayout) findViewById(R.id.back);
		tv_add_words = (TextView) findViewById(R.id.title);
		tv_add_words.setText(R.string.add_word);
		im_delete = (ImageView) findViewById(R.id.im_delete);
		im_edit_ok = (ImageView) findViewById(R.id.im_edit_ok);
		im_cancle = (ImageView) findViewById(R.id.im_cancle);
		
		im_complete.setOnClickListener(this);
		im_back.setOnClickListener(this);
		im_edit_ok.setOnClickListener(this);
		im_delete.setOnClickListener(this);
		im_cancle.setOnClickListener(this);
		
		ed.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		ed.setGravity(Gravity.TOP);
		ed.setSingleLine(false);
		ed.setHorizontallyScrolling(false);
		ed.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				hasChanged = true;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		
		intent = getIntent();
		wordExplain = intent.getStringExtra("words");
		date = intent.getStringExtra("date");
		id = intent.getLongExtra("id", 0);
		if (wordExplain != null) {
			ed.setText(wordExplain);
			tv_add_words.setText(R.string.edit_words);
			ed.setSelection(wordExplain.length());
			im_complete.setVisibility(View.GONE);
			im_delete.setVisibility(View.VISIBLE);
			im_edit_ok.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.im_edit_ok:
			if(hasChanged = true){
				updateDateBaseExplain(ed.getText().toString(),date);
			}
			finish();
			break;
		case R.id.im_complete:
			if (ed.getText().toString().length() == 0) {
				Toast.makeText(this, "还没添加内容哦~", 2000).show();
			} else {
				insertDataBaseExplain(date, Tools.getStartTime(),
						ed.getText().toString(), 4, 0);
				finish();
			}
			break;
		case R.id.im_delete:
			deleteDateBaseExplain(ed.getText().toString(),date);
			finish();
			break;
		case R.id.back:
			finish();
			break;
		case R.id.im_cancle:
			finish();
			break;
		default:
			break;
		}

	}
	
	private void insertDataBaseExplain(String date, String time,
			String explain, int type, int statistics) {
		ContentValues runningItem = new ContentValues();
		runningItem.put(DataBaseContants.ID, Tools.getPKL());
		runningItem.put(DataBaseContants.DATE, date);
		runningItem.put(DataBaseContants.TIME_START, time);
		runningItem.put(DataBaseContants.EXPLAIN, explain);
		runningItem.put(DataBaseContants.TYPE, type);
		runningItem.put(DataBaseContants.STATISTICS, statistics);
		this.getContentResolver().insert(DataBaseContants.CONTENT_URI,
				runningItem);
	}

	// 更新数据库中的一条 文字信息
	private void updateDateBaseExplain(String explain,String date) {
		ContentResolver cr = this.getContentResolver();
		ContentValues updateValues = new ContentValues();
		updateValues.put(DataBaseContants.EXPLAIN, explain);
		updateValues.put(DataBaseContants.SYNC_STATE, 2);
		cr.update(DataBaseContants.CONTENT_URI, updateValues,
				DataBaseContants.EXPLAIN + " = ? " + " and "+DataBaseContants.DATE + " = ? ", new String[] {wordExplain ,date});
	}

	// 删除数据库中的一条 文字信息
	private void deleteDateBaseExplain(String explain,String date) {
		ContentResolver cr = this.getContentResolver();
		cr.delete(DataBaseContants.CONTENT_URI, DataBaseContants.EXPLAIN + " = ?" + " and " +DataBaseContants.DATE + " = ?", new String[]{wordExplain,date});

		ContentValues values = new ContentValues();
		values.put(DataBaseContants.DELETE_VALUE, id);
		cr.insert(DataBaseContants.CONTENT_DELETE_URI, values);
	}
}
