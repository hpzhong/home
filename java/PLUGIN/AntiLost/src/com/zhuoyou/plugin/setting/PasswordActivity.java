package com.zhuoyou.plugin.setting;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.zhuoyou.plugin.antilost.PlugTools;
import com.zhuoyou.plugin.antilost.R;

public class PasswordActivity extends Activity {

	private Context mContext;
	private ImageView mBack;
	private EditText mOld_password;
	private EditText mNew_password;
	private EditText mNew_again;
	private Button mButton;
	private String password = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_password);
		mContext = this;
		password = PlugTools.getDataString(this, "password");
		initView();
	}

	private void initView() {
		mBack = (ImageView)findViewById(R.id.back);
		mBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mOld_password = (EditText)findViewById(R.id.old_password);
		if (password == null || password.equals("****")) {
			mOld_password.setVisibility(View.GONE);
		}
		mNew_password = (EditText)findViewById(R.id.new_password);
		mNew_again = (EditText)findViewById(R.id.new_again);
		mButton = (Button)findViewById(R.id.set_done);
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (password == null || password.equals("****")) {
					String temp1 = mNew_password.getText().toString();
					String temp2 = mNew_again.getText().toString();
					if (temp1.equals("") || temp2.equals("")) {
						Toast.makeText(mContext, R.string.password_empty, Toast.LENGTH_SHORT).show();
					} else if (temp1.length() != 4 || temp2.length() != 4) {
						Toast.makeText(mContext, R.string.password_length, Toast.LENGTH_SHORT).show();
					} else if (!temp1.equals("") && !temp2.equals("") && temp1.equals(temp2)) {
						if (PlugTools.saveDataString(mContext, "password", temp1))
							finish();
					} else {
						Toast.makeText(mContext, R.string.password_different, Toast.LENGTH_SHORT).show();
					}
				} else {
					String temp1 = mOld_password.getText().toString();
					String temp2 = mNew_password.getText().toString();
					String temp3 = mNew_again.getText().toString();
					if (temp1.equals("") || temp2.equals("") || temp3.equals("")) {
						Toast.makeText(mContext, R.string.password_empty, Toast.LENGTH_SHORT).show();
					} else if (temp1.equals(password)) {
						if (temp2.length() != 4 || temp3.length() != 4) {
							Toast.makeText(mContext, R.string.password_length, Toast.LENGTH_SHORT).show();
						} else if (!temp2.equals("") && !temp3.equals("") && temp2.equals(temp3)) {
							if (PlugTools.saveDataString(mContext, "password", temp2))
								finish();
						} else {
							Toast.makeText(mContext, R.string.password_different, Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(mContext, R.string.password_error, Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}
}
