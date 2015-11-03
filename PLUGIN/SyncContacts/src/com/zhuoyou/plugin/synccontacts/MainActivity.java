package com.zhuoyou.plugin.synccontacts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import a_vcard.android.syncml.pim.VDataBuilder;
import a_vcard.android.syncml.pim.VNode;
import a_vcard.android.syncml.pim.vcard.ContactStruct;
import a_vcard.android.syncml.pim.vcard.ContactStruct.PhoneData;
import a_vcard.android.syncml.pim.vcard.VCardComposer;
import a_vcard.android.syncml.pim.vcard.VCardException;
import a_vcard.android.syncml.pim.vcard.VCardParser;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {
	private int mPhoneNumber = 0;
	private List<ContactItem> mListItems = new ArrayList<ContactItem>();
	private DraggableListView mListView;
	private MainAdapter mAdapter;
	private static final int ACTIVITY_RESLUT_PICK_CONTACT = 1997;
	private static final int ACTIVITY_RESLUT_NEW_CONTACT = 1998;
	private static final int ACTIVITY_RESLUT_EDIT_CONTACT = 1999;
	private static final String mActionGet = "com.zhuoyou.plugin.synccontacts.get";
	private CustomProgressDialog mDialog;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1008) {
				if (mDialog != null) {
					mDialog.dismiss();
					Toast.makeText(MainActivity.this, R.string.not_find_contacts_in_remote_device, Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		getOverflowMenu();

		mPhoneNumber = getPhoneContactsNumber(this);

		findViews();
	}

	private void findViews() {
		mListView = (DraggableListView) findViewById(android.R.id.list);
		mAdapter = new MainAdapter(this, R.layout.item, mListItems);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		mListView.setDropListener(onDrop);
		mListView.setRemoveListener(onRemove);

		updateAdapter();

		mDialog = CustomProgressDialog.createDialog(MainActivity.this);
		mDialog.setCancelable(false);
		mDialog.setMessage(getString(R.string.loading_contacts));

		registerBc();

		getDataFromRemote();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unRegisterBc();
	}

	private void registerBc() {
		IntentFilter intentF = new IntentFilter();
		intentF.addAction(mActionGet);
		registerReceiver(mGetDataReceiver, intentF);
	}

	private void unRegisterBc() {
		unregisterReceiver(mGetDataReceiver);
	}

	private BroadcastReceiver mGetDataReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			char[] c_tag = intent.getCharArrayExtra("tag");
			String content = intent.getStringExtra("content");
			Log.i("gchk", "mGetDataReceiver 0X61 TAG[0] =" + c_tag[0] + "TAG[1]= " + c_tag[1] + "||| c= " + content);
			int curr_index = c_tag[0] - 0x20;
			int totle_number = c_tag[1] - 0x20;
			Log.i("gchk", "curr_index = " + curr_index + " |||totle_number =" + totle_number);
			String[] s = content.split("\\|");

			if (curr_index == 0 && mListItems.size() > 0) {
				mListItems.clear();
			}

			for (int i = 0; i < (s.length / 2); i++) {
				ContactItem item = new ContactItem();
				item.setName(s[i * 2]);
				item.setNumber(s[i * 2 + 1]);
				mListItems.add(item);
			}
			if (curr_index == totle_number) {
				mDialog.dismiss();
				updateAdapter();
				mHandler.removeMessages(1008);
			}
		}
	};

	private void getOverflowMenu() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mflater = new MenuInflater(this);
		mflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_backup:
			backup();
			break;
		case R.id.action_load:
			load();
			break;
		case R.id.action_send:
			send();
			break;
		case R.id.action_add_contact:
			showAddContactDialog();
			break;
		case android.R.id.home:
			// TODO:need other operations?
			finish();
			break;
		default:
			building();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private DraggableListView.DropListener onDrop = new DraggableListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			ContactItem item = mAdapter.getItem(from);

			mAdapter.remove(item);
			mAdapter.insert(item, to);
		}
	};

	private DraggableListView.RemoveListener onRemove = new DraggableListView.RemoveListener() {
		@Override
		public void remove(int which) {
			mAdapter.remove(mAdapter.getItem(which));
		}
	};

	private void showAddContactDialog() {
		CharSequence[] items = new CharSequence[2];
		items[0] = getString(R.string.item_1_add_contact_from_system);
		items[1] = getString(R.string.item_2_create_new_contact);
		new AlertDialog.Builder(this).setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (mListItems.size() >= Constants.MAX_QUICK_CONTACTS) {
					Toast.makeText(MainActivity.this, R.string.max_contact_number, Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					return;
				}

				
				if (which == 1) {
					Intent intent = new Intent(MainActivity.this, NewEditScreen.class);
					intent.putExtra("action", "new");
					startActivityForResult(intent, ACTIVITY_RESLUT_NEW_CONTACT);
				} else if (which == 0) {
					if (mPhoneNumber <= 0) {
						// TODO: need call back
						Intent intent = new Intent(MainActivity.this, NewEditScreen.class);
						intent.putExtra("action", "new");
						startActivityForResult(intent, ACTIVITY_RESLUT_NEW_CONTACT);
					} else {
						// sdk didn't provider multi choose screen.
						Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
						startActivityForResult(intent, ACTIVITY_RESLUT_PICK_CONTACT);
					}
				}
				dialog.dismiss();
			}
		}).setCancelable(true).create().show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case ACTIVITY_RESLUT_PICK_CONTACT: {
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				ContentResolver resolver = getContentResolver();
				Cursor c = resolver.query(contactData, null, Contacts.HAS_PHONE_NUMBER + " = 1", null, null);
				if (c.moveToFirst()) {
					final List<String> allPhoneNum = new ArrayList<String>();
					// get display name
					String name = c.getString(c.getColumnIndex(CommonDataKinds.Phone.DISPLAY_NAME));
					// get id
					String _id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
					// get numbers
					final Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + _id, null,
							null);
					if (phones.moveToFirst()) {
						for (; !phones.isAfterLast(); phones.moveToNext()) {
							String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							allPhoneNum.add(phoneNumber);
						}
						phones.close();
					}

					final ContactItem item = new ContactItem();
					item.setName(name);
					Log.i("gchk", "pick a contact , name = " + name);
					// TODO: if number > 1,show selete dialog.
					if (allPhoneNum.size() > 1) {
						CharSequence[] items = new CharSequence[allPhoneNum.size()];
						allPhoneNum.toArray(items);
						new AlertDialog.Builder(this).setTitle(R.string.selete_number_title).setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								item.setNumber(allPhoneNum.get(which));
								mListItems.add(item);
								dialog.dismiss();
								updateAdapter();
							}
						}).setCancelable(false).create().show();
					} else {
						item.setNumber(allPhoneNum.get(0));
						mListItems.add(item);
						updateAdapter();
					}
				} else {
					Toast.makeText(this, R.string.not_contain_number, Toast.LENGTH_SHORT).show();
				}
				c.close();
			}
		}
			break;
		case ACTIVITY_RESLUT_NEW_CONTACT:
			if (resultCode == Activity.RESULT_OK) {
				ContactItem item = data.getParcelableExtra("item");
				mListItems.add(item);
				updateAdapter();
			}
			break;
		case ACTIVITY_RESLUT_EDIT_CONTACT:
			if (resultCode == Activity.RESULT_OK) {
				boolean sub_action = data.getBooleanExtra("sub_action", false);
				int index = data.getIntExtra("index", -1);
				if (sub_action) {
					if (index != -1) {
						mListItems.remove(index);
						updateAdapter();
					} else {
						Log.e("gchk", "index == -1 ERROR");
					}
				} else {
					ContactItem item = data.getParcelableExtra("item");
					if (index != -1) {
						mListItems.set(index, item);
						updateAdapter();
					} else {
						Log.e("gchk", "index == -1 ERROR");
					}
				}
			}
			break;
		default:
			break;
		}
	}

	private int getPhoneContactsNumber(Context ctx) {
		int num = 0;
		ContentResolver resolver = ctx.getContentResolver();
		Cursor phoneCursor = resolver.query(Contacts.CONTENT_URI, new String[] { Contacts._ID }, Contacts.HAS_PHONE_NUMBER + " = 1", null, null);
		if (phoneCursor != null) {
			num = phoneCursor.getCount();
		}
		phoneCursor.close();
		phoneCursor = null;

		Log.i("gchk", "get all contact numbers = " + num);
		return num;
	}

	private void updateAdapter() {
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged(mListItems);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// enter edit screen
		Intent intent = new Intent(this, NewEditScreen.class);
		intent.putExtra("action", "edit");
		intent.putExtra("item", mListItems.get(arg2));
		intent.putExtra("index", arg2);
		startActivityForResult(intent, ACTIVITY_RESLUT_EDIT_CONTACT);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,final int arg2, long arg3) {
		
		ContactItem item = mListItems.get(arg2);
		String msg = "";
		msg = item.getName();
		msg += "\n";
		msg += item.getNumber();
		
		new AlertDialog.Builder(this).setTitle(R.string.delete_this_contact)
		.setMessage(msg)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListItems.remove(arg2);
				updateAdapter();
				dialog.dismiss();
			}
		}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).setCancelable(true).create().show();
		return true;
	}

	private void backup() {
		new BackUpTask().execute();
	}

	private class BackUpTask extends AsyncTask<String, Void, Integer> {
		private CustomProgressDialog mProgressDialog;

		@Override
		protected void onPreExecute() {
			mProgressDialog = CustomProgressDialog.createDialog(MainActivity.this);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setMessage(getString(R.string.backup_wait));
			mProgressDialog.show();
		}

		@Override
		protected Integer doInBackground(String... params) {
			List<ContactStruct> mContacts = new ArrayList<ContactStruct>();
			if (mListItems == null || mListItems.size() == 0) {
				return -1;
			}

			for (int i = 0; i < mListItems.size(); i++) {
				ContactItem item = mListItems.get(i);
				ContactStruct contact = new ContactStruct();
				contact.name = item.getName();
				List<PhoneData> phoneList = new ArrayList<PhoneData>();
				PhoneData phoneData = new PhoneData();
				phoneData.type = 0;
				phoneData.data = item.getNumber();
				phoneData.label = "custom";
				phoneList.add(phoneData);
				contact.phoneList = phoneList;
				mContacts.add(contact);
			}

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
			try {
				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
				VCardComposer composer = new VCardComposer();
				for (ContactStruct contact : mContacts) {
					String vcardString = composer.createVCard(contact, VCardComposer.VERSION_VCARD30_INT);
					writer.write(vcardString);
					writer.write("\n");
					writer.flush();
				}
				writer.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return -4;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return -4;
			} catch (VCardException e) {
				e.printStackTrace();
				return -4;
			} catch (IOException e) {
				e.printStackTrace();
				return -4;
			}

			return 0;
		}

		@Override
		protected void onPostExecute(Integer result) {
			mProgressDialog.dismiss();
			Toast.makeText(MainActivity.this, R.string.backup_successed, Toast.LENGTH_SHORT).show();
		}
	}

	private void load() {
		new LoadTask().execute();
	}

	private class LoadTask extends AsyncTask<String, Void, Integer> {
		private CustomProgressDialog mProgressDialog;

		@Override
		protected void onPreExecute() {
			mProgressDialog = CustomProgressDialog.createDialog(MainActivity.this);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setMessage(getString(R.string.restore_wait));
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
			mAdapter.notifyDataSetChanged(mListItems);
			Toast.makeText(MainActivity.this, R.string.restore_successed, Toast.LENGTH_SHORT).show();
		}
	}

	private void send() {
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
			sendBroadcast(intent);
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

			sendBroadcast(intent);
			
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
			sendBroadcast(intent);
		}
	}

	private void getDataFromRemote() {
		mDialog.show();

		// 1.send broadcast
		Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
		intent.putExtra("plugin_cmd", 0x60);
		intent.putExtra("plugin_content", "himan");
		sendBroadcast(intent);

		Message msg = new Message();
		msg.what = 1008;
		mHandler.sendMessageDelayed(msg, 10000);
	}

	private void building() {
		Toast.makeText(MainActivity.this, "building now", Toast.LENGTH_SHORT).show();
	}

}
