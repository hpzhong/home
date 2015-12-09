package com.zhuoyou.plugin.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.share.ShareToWeixin;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        ShareToWeixin.api.handleIntent(getIntent(), this);  
    }  

	@Override
	public void onReq(BaseReq arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResp(BaseResp resp) {
		switch (resp.errCode) {  
		case BaseResp.ErrCode.ERR_OK:  
			Toast.makeText(this, R.string.share_success, Toast.LENGTH_SHORT).show();
			break;  
		case BaseResp.ErrCode.ERR_USER_CANCEL:  
			break;  
		case BaseResp.ErrCode.ERR_AUTH_DENIED:  
			break;  
		default:  
			Toast.makeText(this, R.string.share_fail, Toast.LENGTH_SHORT).show();
			break;  
		}
		finish();
	}

}
