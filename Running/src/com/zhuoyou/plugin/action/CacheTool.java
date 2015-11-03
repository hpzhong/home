package com.zhuoyou.plugin.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Message;

import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.mainFrame.MineFragment;
import com.zhuoyou.plugin.running.Main;
import com.zhuoyou.plugin.running.Tools;

public class CacheTool {
	
	private Context mcontext;
	
	public CacheTool(Context context){
		mcontext = context;
	}
	
	public  boolean saveObject(Serializable ser, String file) {
	    FileOutputStream fos = null;
	    ObjectOutputStream oos = null;
	    try {
	        fos = mcontext.openFileOutput(file, mcontext.MODE_PRIVATE);
	        oos = new ObjectOutputStream(fos);
	        oos.writeObject(ser);
	        oos.flush();
	        return true;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        try {
	            oos.close();
	        } catch (Exception e) {
	        }
	        try {
	            fos.close();
	        } catch (Exception e) {
	        }
	    }
	}
	
	/**
	 * 读取对象
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public Serializable readObject(String file) {
	    FileInputStream fis = null;
	    ObjectInputStream ois = null;
	    try {
	        fis = mcontext.openFileInput(file);
	        ois = new ObjectInputStream(fis);
	        return (Serializable) ois.readObject();
	    } catch (FileNotFoundException e) {
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            ois.close();
	        } catch (Exception e) {
	        }
	        try {
	            fis.close();
	        } catch (Exception e) {
	        }
	    }
	    return null;
	}
	
	
	class ActionFilefilter implements FilenameFilter{
		 String mfilter = "";
		 
		 public ActionFilefilter(String filter){
			 mfilter = filter;
		 }
		
		 public boolean isAction(String filename){    
			    if (filename.toLowerCase().endsWith(mfilter)){   
			    	//把文件转成小写后看其后缀是否为.jpg
			      return true;    
			    }else{    
			      return false;    
			    }    
		}
		
		@Override
		public boolean accept(File dir, String filename) {
			// TODO Auto-generated method stub
			return isAction(filename);
			//覆写accept方法
		}
	}	
	
	/*
	 * cache welcomde date,
	 * file name :"action id"+".welcome"
	 */
	public void SaveWelcomeDate(ActionWelcomeInfo welcome_data){
		File file_path = mcontext.getFilesDir();
		//before save ,we clear welcome data cache!
		for(File mm :file_path.listFiles(new ActionFilefilter(".welcome"))){
			mm.delete();
		}		
		String filename = welcome_data.GetID()+".welcome";
		saveObject(welcome_data, filename);
	}
	
	/*
	 * Get welcome date
	 * if get more item,we get the biggest id of welcomeinfo item
	 * return:ActionWelcomeInfo
	 */
	public ActionWelcomeInfo GetWelcomeDate(){
		File file_path = mcontext.getFilesDir();
		ActionWelcomeInfo mWelcome =null;
//		List<ActionWelcomeInfo> mlist = new ArrayList<ActionWelcomeInfo>();
		for(File mm :file_path.listFiles(new ActionFilefilter(".welcome"))){
			if(mWelcome == null)
				mWelcome = (ActionWelcomeInfo) readObject(mm.getName());
			else{
				ActionWelcomeInfo tmp = (ActionWelcomeInfo) readObject(mm.getName());
				if (tmp != null && tmp.GetID() > mWelcome.GetID())
					mWelcome = tmp;
			}
		}
		return mWelcome;
	}
	
	/*
	 * in action of pulldown refresh,clear last cache! 
	 */
	public void ClearListItem(){
		File file_path = mcontext.getFilesDir();
		List<ActionListItemInfo> mlist = new ArrayList<ActionListItemInfo>();
		for(File mm :file_path.listFiles(new ActionFilefilter(".actionitem"))){
			mm.delete();
		}

	}
	
	/*
	 * cache ActionListItemInfo date
	 * file name:"action id"+".actionitem"
	 */
	public void SaveActionListItem(List<ActionListItemInfo> mlist){
		if (mlist.size() > 0) {
			int bId = GetBiggestActionIdofLocal();
			boolean state = false;
			for (int i = 0; i < mlist.size(); i++) {
				if (mlist.get(i).GetActivtyId() > bId) {
					state = true;
				}
				String filename = mlist.get(i).GetActivtyId() + ".actionitem";
				boolean flag = saveObject(mlist.get(i), filename);
			}
			if (state) {
				Tools.setActState(mcontext, true);
			}
		}
	}

	/*
	 * Get ActionList item date
	 * return:List<ActionListItemInfo>
	 */
	public List<ActionListItemInfo> GetActionListItemDate(){
		File file_path = mcontext.getFilesDir();
		List<ActionListItemInfo> mlist = new ArrayList<ActionListItemInfo>();
		for(File mm :file_path.listFiles(new ActionFilefilter(".actionitem"))){
			mlist.add((com.zhuoyou.plugin.action.ActionListItemInfo) readObject(mm.getName()));
		}
		return mlist;
	}
	
	/*
	 * cache  ActionInfo
	 * care:if action <0,we don't cache it
	 * filename: "action_id"+".actioninfo"
	 */
	public void SaveActionInfo(ActionInfo mactioninfo){
		int actionid = mactioninfo.GetActionId();
		if(actionid>0){
			String filename = actionid+".actioninfo";
			saveObject(mactioninfo, filename);
		}
	}
	
	/*
	 * cache list of ActionInfo
	 * care:if action <0,we don't cache it
	 * filename: "action_id"+".actioninfo"
	 */
	public void SaveActionInfo(List<ActionInfo> mactioninfo){
		for(int i = 0;i<mactioninfo.size();i++){
			SaveActionInfo(mactioninfo.get(i));
		}
	}

	/*
	 * Get welcome date
	 * if get more item,we get the biggest id of welcomeinfo item
	 * return:ActionWelcomeInfo
	 */
//	public List<ActionInfo> GetActionInfoDate(){
//		File file_path = mcontext.getFilesDir();
//		List<ActionInfo> mlist = new ArrayList<ActionInfo>();
//		for(File mm :file_path.listFiles(new ActionFilefilter(".actioninfo"))){
//				ActionWelcomeInfo tmp = (ActionWelcomeInfo) readObject(mm.getName());
//				mlist.add= ((ActionInfo) readObject(mm.getName()));
//		}
//		return mlist;
//	}	
	
	
	public ActionInfo GetActionInfoDates(){
		File file_path = mcontext.getFilesDir();
		ActionInfo mlist = new ActionInfo();
		for(File mm :file_path.listFiles(new ActionFilefilter(".actioninfo"))){
				ActionWelcomeInfo tmp = (ActionWelcomeInfo) readObject(mm.getName());
				mlist= (ActionInfo) readObject(mm.getName());
		}
		return mlist;
	}	
	/*
	 * insert msg date into db;
	 *  check msg read(1)/unread(0) state
	 */
	public void SaveMsgList(List<MessageInfo> mlist){
		ContentResolver cr = mcontext.getContentResolver();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		String str;
		int y = 0;
		if (mlist.size() > 0) {
			String msgId = initListData();
			String msgIndex[] = msgId.split(",");
			for (int i = 0; i < mlist.size(); i++) {
				if (!arryContains(msgIndex, String.valueOf(mlist.get(i).GetMsgId()))) {
					y = 1;
					ContentValues values = new ContentValues();
					values.put(DataBaseContants.ACTIVITY_ID, mlist.get(i).getActivityId());
					values.put(DataBaseContants.MSG_ID, mlist.get(i).GetMsgId());
					values.put(DataBaseContants.MSG_CONTENT, mlist.get(i).GetMsgContent());
					values.put(DataBaseContants.MSG_TYPE, mlist.get(i).getmMsgType());
					Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
					str = formatter.format(curDate);
					values.put(DataBaseContants.MSG_TIME, str);
					values.put(DataBaseContants.MSG_STATE, 0);
					cr.insert(DataBaseContants.CONTENT_MSG_URI, values);
				}
			}
			if (y > 0) {
				Tools.setMsgState(mcontext, true);

				if (Main.mHandler != null) {
					Message msg = new Message();
					msg.what = Main.MSG_UNREAD;
					Main.mHandler.sendMessage(msg);
				}
				if (MineFragment.mHandler != null) {
					Message msg = new Message();
					msg.what = MineFragment.MSG_UNREAD;
					MineFragment.mHandler.sendMessage(msg);
				}
			}
		}
	}
	
   public static boolean arryContains(String[] stringArray, String source) {
       // 转换为list
       List<String> tempList = Arrays.asList(stringArray);
       // 利用list的包含方法,进行判断
       if(tempList.contains(source)){
           return true;
       } else {
           return false;
       }
   }
	
	/*
	 *  缓存AppInitForAction
	 */
	public boolean SaveActionInitDate(AppInitForAction mm){
		//first cache welcome data
		if(mm.GetWelcomeInfo() != null)
			SaveWelcomeDate(mm.GetWelcomeInfo());
		//second cache action Listitem data
		SaveActionListItem(mm.GetActionList());
		//third update msg info in db
		SaveMsgList(mm.GetMsgList());
		return false;
	}	
	
	/*
	 * delete file in path of "data/data/com.zhuoyou.plugin.running/files/*"
	 * 
	 */
	public void  ClearCachefile(){
		File file_path = mcontext.getFilesDir();
		for(File mm :file_path.listFiles(new ActionFilefilter(".actionitem"))){
			mm.delete();
		}
	}
	
	/*
	 * to get action id which is most small;
	 * return action id;
	 */
	public int GetLocalLittleIdofActionitem(){
		int action_id = 0;
		File file_path = mcontext.getFilesDir();
		List<ActionInfo> mlist = new ArrayList<ActionInfo>();
		for(File mm :file_path.listFiles(new ActionFilefilter(".actionitem"))){
				if(action_id == 0){
					action_id = Integer.valueOf(mm.getName().split("\\.")[0]);
				}else{
					int mid =  Integer.valueOf(mm.getName().split("\\.")[0]);
					if(action_id > mid)
						action_id = mid;
				}
		}
		return action_id;
	}
	
	
	/*
	 * Get Local biggest action_id which is ended
	 * return:int value of action_id;
	 */
	public int GetBiggestActionIdofLocal(){
		int action_id = -1;
		File file_path = mcontext.getFilesDir();
		List<ActionListItemInfo> mlist = new ArrayList<ActionListItemInfo>();
		for(File mm :file_path.listFiles(new ActionFilefilter(".actionitem"))){
			if(action_id == -1){
				String filename = mm.getName();
				action_id = Integer.valueOf(filename.substring(0,filename.lastIndexOf(".")));
			}else{
				String filename = mm.getName();
				int tmp_action_id = Integer.valueOf(filename.substring(0,filename.lastIndexOf(".")));
				if(tmp_action_id>action_id)
					action_id = tmp_action_id;
			}
		}
		return action_id;
	}

	private String initListData() {
		String msgId = "";
		Cursor cursor = mcontext.getContentResolver().query(DataBaseContants.CONTENT_MSG_URI, new String[] { "msg_id" },
				null, null, null);
		cursor.moveToFirst();
		int count = cursor.getCount();
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				if( i == 0){
					msgId = cursor.getInt(cursor.getColumnIndex(DataBaseContants.MSG_ID)) + "";
				}else{
					msgId = msgId + "," +cursor.getInt(cursor.getColumnIndex(DataBaseContants.MSG_ID));
				}
				cursor.moveToNext();
			}
		}
		cursor.close();
		cursor = null;
		return msgId;
	}
	
}
