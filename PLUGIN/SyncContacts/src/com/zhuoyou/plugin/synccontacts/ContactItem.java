package com.zhuoyou.plugin.synccontacts;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactItem implements Parcelable {
	private String mName;
	private String mNumber;

	public ContactItem() {
		super();
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		// need delete space in name
		String temp = mName.replaceAll(" ", "");
		this.mName = temp;
	}

	public String getNumber() {
		return mNumber;
	}

	public void setNumber(String mNumber) {
		// need delete space in name
		String temp = mNumber.replaceAll(" ", "");
		this.mNumber = temp;
	}

	public static final Parcelable.Creator<ContactItem> CREATOR = new Creator<ContactItem>() {

		@Override
		public ContactItem[] newArray(int size) {
			return new ContactItem[size];
		}

		@Override
		public ContactItem createFromParcel(Parcel source) {
			ContactItem item = new ContactItem();
			item.setName(source.readString());
			item.setNumber(source.readString());
			return item;
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mName);
		dest.writeString(mNumber);
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
