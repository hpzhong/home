package com.zhuoyou.plugin.bluetooth.data;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;

import android.content.Context;

import com.zhuoyou.plugin.running.RunningApp;

public class IgnoreList {

    private static final String SAVE_FILE_NAME = "IgnoreList";
    private static final IgnoreList INSTANCE = new IgnoreList();
    private HashSet<String> mIgnoreList = null;
    private Context mContext = null;
    private static final String[] EXCLUSION_LIST = { 
        "android",
        "com.android.mms",
        "com.android.phone",
        "com.android.providers.downloads", 
        "com.android.bluetooth",
        "com.mediatek.bluetooth",
        "com.htc.music",
        "com.lge.music",
        "com.sec.android.app.music",
        "com.sonyericsson.music",
        "com.ijinshan.mguard" ,
        "com.android.music",
        "com.android.dialer",
        "com.google.android.music"
    };

    private IgnoreList() {
        mContext = RunningApp.getInstance().getApplicationContext();
    }

    public static IgnoreList getInstance() {
        return INSTANCE;
    }

    public HashSet<String> getIgnoreList() {
        if (mIgnoreList == null) {
            loadIgnoreListFromFile();
        }

        return mIgnoreList;
    }

    @SuppressWarnings("unchecked")
    private void loadIgnoreListFromFile() {
        if (mIgnoreList == null) {
            try {
                Object obj = (new ObjectInputStream(mContext.openFileInput(SAVE_FILE_NAME))).readObject();
                mIgnoreList = (HashSet<String>) obj;
            } catch (ClassNotFoundException exception) {
                exception.printStackTrace();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        if (mIgnoreList == null) {
            mIgnoreList = new HashSet<String>();
        }
    }

    public void addIgnoreItem(String name) {
    	if (mIgnoreList == null) {
    		loadIgnoreListFromFile();
    	}
    	if (!mIgnoreList.contains(name)) {
    		mIgnoreList.add(name);
    	}
    }
    
    public void removeIgnoreItem(String name) {
    	if (mIgnoreList == null) {
    		loadIgnoreListFromFile();
    	}
    	if (mIgnoreList.contains(name)) {
    		mIgnoreList.remove(name);
    	}
    	
    }
    
    
    
    public void saveIgnoreList() {
    	FileOutputStream fileoutputstream;
        ObjectOutputStream objectoutputstream;

        try {
            fileoutputstream = mContext.openFileOutput(SAVE_FILE_NAME, Context.MODE_PRIVATE);
            objectoutputstream = new ObjectOutputStream(fileoutputstream);
            objectoutputstream.writeObject(mIgnoreList);
            objectoutputstream.close();
            fileoutputstream.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return;
        }
    }

    public void saveIgnoreList(HashSet<String> ignoreList) {
        FileOutputStream fileoutputstream;
        ObjectOutputStream objectoutputstream;

        try {
            fileoutputstream = mContext.openFileOutput(SAVE_FILE_NAME, Context.MODE_PRIVATE);
            objectoutputstream = new ObjectOutputStream(fileoutputstream);
            objectoutputstream.writeObject(ignoreList);
            objectoutputstream.close();
            fileoutputstream.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return;
        }

        mIgnoreList = ignoreList;
    }

    public HashSet<String> getExclusionList() {
        HashSet<String> exclusionList = new HashSet<String>();
        for (String exclusionPackage : EXCLUSION_LIST) {
            exclusionList.add(exclusionPackage);
        }

        return exclusionList;
    }

}
