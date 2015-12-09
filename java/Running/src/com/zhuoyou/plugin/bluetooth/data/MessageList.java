package com.zhuoyou.plugin.bluetooth.data;

import java.util.ArrayList;

public class MessageList {
	private boolean mNewMessage;
	// private String mCurrentTime;
	private int mSize;
	private ArrayList<MessageListItem> mMessageItems;

	public MessageList() {
		reset();
	}

	synchronized void reset() {
		if (mMessageItems == null) {
			mMessageItems = new ArrayList<MessageListItem>();
		} else {
			mMessageItems.clear();
		}

		mSize = 0;
		mNewMessage = false;
	}

	public synchronized boolean addSize(int size) {
		mSize += size;
		return true;
	}

	public synchronized boolean setNewMessage() {
		if (!mNewMessage) {
			mNewMessage = true;
		}
		return true;
	}

	public synchronized boolean addMessageItem(MessageListItem item) {
		if (item != null) {
			mMessageItems.add(item);
		}
		return true;
	}

	public synchronized MessageListItem[] generateMessageItemArray() {
		return mMessageItems.toArray(new MessageListItem[mMessageItems.size()]);
	}

	public int getCurrentSize() {
		return mSize;
	}
}
