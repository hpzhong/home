package com.zhuoyou.plugin.synccontacts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import a_vcard.android.syncml.pim.VDataBuilder;
import a_vcard.android.syncml.pim.VNode;
import a_vcard.android.syncml.pim.vcard.ContactStruct;
import a_vcard.android.syncml.pim.vcard.VCardException;
import a_vcard.android.syncml.pim.vcard.VCardParser;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class LoadBackUpTask extends AsyncTask<String, Void, Integer> {
	private CustomProgressDialog mProgressDialog;
	private Context mCtx;
	private List<ContactItem> mListItems = new ArrayList<ContactItem>();

	public LoadBackUpTask(Context context) {
		mCtx = context;
	}

	@Override
	protected void onPreExecute() {
		mProgressDialog = CustomProgressDialog.createDialog(mCtx);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setMessage(mCtx.getString(R.string.restore_wait));
		mProgressDialog.show();
	}

	@Override
	protected Integer doInBackground(String... params) {
		VCardParser parse = new VCardParser();
		VDataBuilder builder = new VDataBuilder();

		// create folder
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		} else {
			return -2;
		}
		String sd = sdDir.toString();
		String folder = sd + "/com.tyd.btsecretary/synccontact/";
		try {
			String filePath = folder;
			File myFilePath = new File(filePath);
			if (!myFilePath.exists()) {
				boolean flag = myFilePath.mkdirs();
				if (flag) {
				} else {
				}
			} else {
				Log.v("gchk", "folderPath is exists " + folder);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -3;
		}
		String path = folder + "contacts.vcf";
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
			String vcardString = "";
			String line;
			while ((line = reader.readLine()) != null) {
				vcardString += line + "\n";
			}
			reader.close();
			boolean parsed = parse.parse(vcardString, "UTF-8", builder);
			if (!parsed) {
				return -1;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return -4;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -4;
		} catch (IOException e) {
			e.printStackTrace();
			return -4;
		} catch (VCardException e) {
			e.printStackTrace();
			return -4;
		}

		List<VNode> pimContacts = builder.vNodeList;

		if (mListItems != null && mListItems.size() > 0) {
			mListItems.clear();
		}

		for (VNode contact : pimContacts) {
			ContactItem item = new ContactItem();
			ContactStruct contactStruct = ContactStruct.constructContactFromVNode(contact, 1);
			item.setName(contactStruct.name);
			item.setNumber(contactStruct.phoneList.get(0).data);
			mListItems.add(item);
		}

		return 0;
	}

	@Override
	protected void onPostExecute(Integer result) {
		mProgressDialog.dismiss();

		if (result == 0) {
			int number = mListItems.size();

			if (number <= 5 && number > 0) {
				String ret = "";
				for (int i = 0; i < mListItems.size(); i++) {
					ContactItem item = mListItems.get(i);
					ret += item.getName();
					ret += "|";
					ret += item.getNumber();
					ret += "|";
				}
				
				Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
				intent.putExtra("plugin_cmd", 0x61);
				char[] tag = new char[4];
				tag[0] = 0x21;
				tag[1] = 0x21;
				tag[2] = 0XFF;
				tag[3] = 0XFF;
				intent.putExtra("plugin_tag", tag);
				intent.putExtra("plugin_content", ret);
				mCtx.sendBroadcast(intent);
			} else if (number > 5 && number <= 10) {
				String ret = "";
				for (int i = 0; i < 5; i++) {
					ContactItem item = mListItems.get(i);
					ret += item.getName();
					ret += "|";
					ret += item.getNumber();
					ret += "|";
				}
				
				Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
				intent.putExtra("plugin_cmd", 0x61);
				char[] tag = new char[4];
				tag[0] = 0x21;
				tag[1] = 0x22;
				tag[2] = 0XFF;
				tag[3] = 0XFF;
				intent.putExtra("plugin_tag", tag);
				intent.putExtra("plugin_content", ret);
				mCtx.sendBroadcast(intent);
				
				ret = "";
				for (int i = 5; i < number; i++) {
					ContactItem item = mListItems.get(i);
					ret += item.getName();
					ret += "|";
					ret += item.getNumber();
					ret += "|";
				}
				
				intent = new Intent("com.tyd.plugin.receiver.sendmsg");
				intent.putExtra("plugin_cmd", 0x61);
				tag = new char[4];
				tag[0] = 0x22;
				tag[1] = 0x22;
				tag[2] = 0XFF;
				tag[3] = 0XFF;
				intent.putExtra("plugin_tag", tag);
				intent.putExtra("plugin_content", ret);
				mCtx.sendBroadcast(intent);
			}

		} else {
			Toast.makeText(mCtx, R.string.quick_contact_empty, Toast.LENGTH_SHORT).show();
		}
	}

}
