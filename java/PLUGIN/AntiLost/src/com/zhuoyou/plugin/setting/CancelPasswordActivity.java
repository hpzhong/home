package com.zhuoyou.plugin.setting;

import com.zhuoyou.plugin.antilost.PlugTools;
import com.zhuoyou.plugin.antilost.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class CancelPasswordActivity extends Activity {

	private Context mContext;
	private ImageView mBack;
	private EditText mOld_password;
	private Button mButton;
	private String password = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_cancel_password);
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
		mButton = (Button)findViewById(R.id.set_done);
		mButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String temp = mOld_password.getText().toString();
				if (temp.equals("")) {
					Toast.makeText(mContext, R.string.password_empty, Toast.LENGTH_SHORT).show();
				} else if (temp.equals(password)) {
					PlugTools.saveDataString(mContext, "password", "****");
					Toast.makeText(mContext, R.string.cancel_antilost_password, Toast.LENGTH_SHORT).show();
					finish();
				} else {
					Toast.makeText(mContext, R.string.password_error, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}
