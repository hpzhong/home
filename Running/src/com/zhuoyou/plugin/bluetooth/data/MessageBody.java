package com.zhuoyou.plugin.bluetooth.data;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

public abstract class MessageBody {
    private String mSender = null;
    private int mTimestamp = 0;
    private String mContent = null;

    String getSender() {
        return mSender;
    }

    public void setSender(String sender) {
        this.mSender = sender;
    }

    int getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(int timestamp) {
        this.mTimestamp = timestamp;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public abstract void genXmlBuff(XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException,
            IOException;
}
