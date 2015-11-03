package com.zhuoyou.plugin.selfupdate;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class ApkCheckSelfUpdateCodec implements DataCodec
{
	@Override
	public HashMap<String, Object> splitMySelfData(String result) {
		
		JSONObject jsonObject,bodyJSONObject;
		String headResult = "";
		String bodyResult = "";
		HashMap<String, Object> map = null;
		if (TextUtils.isEmpty(result))
				return null;
		
		try 
		{
			jsonObject = new JSONObject(result);
			headResult = jsonObject.getString("head");
			bodyResult = jsonObject.getString("body");
			bodyJSONObject = new JSONObject(bodyResult);
			if(bodyJSONObject!=null && bodyJSONObject.has("errorCode")&& bodyJSONObject.getInt("errorCode")==0)
			{
			    Log.i("gchk", "bodyResult = " + bodyResult);
				if(bodyJSONObject.getInt("policy")==3)
					return null;
				
				map = new HashMap<String, Object>();
				map.put("title", bodyJSONObject.getString("title"));
				map.put("content", bodyJSONObject.getString("content"));
				map.put("policy", bodyJSONObject.getInt("policy"));
				map.put("pName",bodyJSONObject.getString("pName"));
				map.put("ver",bodyJSONObject.getString("ver"));
				map.put("fileUrl",bodyJSONObject.getString("fileUrl"));
				map.put("md5",bodyJSONObject.getString("md5"));
				map.put("errorCode",bodyJSONObject.getInt("errorCode"));
			}			
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return map;
	}

}
