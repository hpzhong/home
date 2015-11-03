package com.zhuoyou.plugin.synccontacts;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class NewEditScreen extends Activity implements OnClickListener {
	private String mAction = "";
	private ContactItem mItem = null;
	private int mCurrSelete = -1;
	private EditText mName;
	private EditText mNumber;
	private ImageView mClearName, mClearNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);

		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);

		Intent intent = getIntent();
		mAction = intent.getStringExtra("action");
		mItem = intent.getParcelableExtra("item");
		mCurrSelete = intent.getIntExtra("index", -1);

		if (mAction.equals("new")) {
			setTitle(R.string.title_create_new);
		} else if (mAction.equals("edit")) {
			setTitle(R.string.title_create_edit);
		}

		initViews();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mflater = new MenuInflater(this);
		mflater.inflate(R.menu.edit, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mAction.equals("new")) {
			menu.findItem(R.id.action_delete).setVisible(false);
		} else if (mAction.equals("edit")) {
			menu.findItem(R.id.action_delete).setVisible(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_delete:
			new AlertDialog.Builder(this).setMessage(getString(R.string.delete_this_contact)).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent();
					intent.putExtra("sub_action", true);
					intent.putExtra("index", mCurrSelete);
					setResult(Activity.RESULT_OK, intent);
					finish();
				}
			}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).create().show();
			break;
		case R.id.action_save:
			doFinish();
			break;
		case android.R.id.home:
			// TODO:need other operations?
			doFinish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initViews() {
		mName = (EditText) findViewById(R.id.name_c_edit);
		mNumber = (EditText) findViewById(R.id.number_c_edit);

		if (mItem != null) {
			mName.setText(mItem.getName());
			mNumber.setText(mItem.getNumber());

			mName.setSelection(mName.getText().length());
			mNumber.setSelection(mNumber.getText().length());
		}

		mClearName = (ImageView) findViewById(R.id.name_r_icon);
		mClearNumber = (ImageView) findViewById(R.id.number_r_icon);
		mClearName.setOnClickListener(this);
		mClearNumber.setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		doFinish();
	}

	private void doFinish() {
		new AlertDialog.Builder(this).setMessage(getString(R.string.msg_save_contacts)).setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				onDone();
			}
		}).setNegativeButton(R.string.giveup, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		}).create().show();
	}

	private void onDone() {
		String name = mName.getEditableText().toString();
		String number = mNumber.getEditableText().toString();
		if (name == null || name.equals("")) {
			Toast.makeText(NewEditScreen.this, R.string.error_input_name, Toast.LENGTH_SHORT).show();
			return;
		}

		if (number == null || number.equals("")) {
			Toast.makeText(NewEditScreen.this, R.string.error_input_number, Toast.LENGTH_SHORT).show();
			return;
		}

		if (mAction.equals("new")) {
			ContactItem item = new ContactItem();
			item.setName(name);
			item.setNumber(number);
			Intent intent = new Intent();
			intent.putExtra("item", item);
			setResult(Activity.RESULT_OK, intent);
		} else if (mAction.equals("edit")) {
			mItem.setName(name);
			mItem.setNumber(number);
			Intent intent = new Intent();
			intent.putExtra("item", mItem);
			intent.putExtra("index", mCurrSelete);
			setResult(Activity.RESULT_OK, intent);
		}
		finish();
	}

	@Override
	public void onClick(View v) {
		if (v == mClearName) {
			if (mName != null) {
				mName.setText("");
			}
		} else if (v == mClearNumber) {
			if (mNumber != null) {
				mNumber.setText("");
			}
		}
	}
}
