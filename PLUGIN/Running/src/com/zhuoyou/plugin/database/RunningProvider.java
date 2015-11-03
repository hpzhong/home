package com.zhuoyou.plugin.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class RunningProvider extends ContentProvider {
	private DBOpenHelper mDBOpenHelper;
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "data", 1);
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "data/#", 2);
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "cloud", 3);
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "cloud/#", 4);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase mWriteable = mDBOpenHelper.getWritableDatabase();
		int count = 0;
		String id = null;
		switch (uriMatcher.match(uri)) {
		case 1:
			count = mWriteable.delete(DataBaseContants.TABLE_DATA_NAME, where, whereArgs);
			break;
		case 2:
			id = uri.getPathSegments().get(1);
			if(TextUtils.isEmpty(where))
			{
				where = DataBaseContants.ID + "=" + id;
			}
			else
			{
				where = DataBaseContants.ID + "=" + id + " AND (" + where + ")";
			}
			count = mWriteable.delete(DataBaseContants.TABLE_DATA_NAME, where, whereArgs);
			break;
		case 3:
			count = mWriteable.delete(DataBaseContants.TABLE_DELETE_NAME, where, whereArgs);
			break;
		case 4:
			id = uri.getPathSegments().get(1);
			if(TextUtils.isEmpty(where))
			{
				where = DataBaseContants.DELETE_ID + "=" + id;
			}
			else
			{
				where = DataBaseContants.DELETE_ID + "=" + id + " AND (" + where + ")";
			}
			count = mWriteable.delete(DataBaseContants.TABLE_DELETE_NAME, where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Cannot delete for URL:" + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case 1:
			return "vnd.android.cursor.dir/data";
		case 2:
			return "vnd.android.cursor.item/data";
		case 3:
			return "vnd.android.cursor.dir/delete";
		case 4:
			return "vnd.android.cursor.item/delete";
		default:
			throw new IllegalArgumentException("Unknown URI" + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (uriMatcher.match(uri) != 1 && uriMatcher.match(uri) != 3) {  
			throw new IllegalArgumentException();  
		}
		SQLiteDatabase mWriteable = mDBOpenHelper.getWritableDatabase();
		long rowId = 0;
		switch (uriMatcher.match(uri)) {
		case 1:
			rowId = mWriteable.insert(DataBaseContants.TABLE_DATA_NAME, "data", values);
			if(rowId > 0)
			{
				Uri newUri = ContentUris.withAppendedId(DataBaseContants.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(newUri, null);
				return newUri;
			}
			else
			{
				throw new IllegalArgumentException("Faied to insert row into " + uri);
			}			
		case 3:
			rowId = mWriteable.insert(DataBaseContants.TABLE_DELETE_NAME, "delete", values);
			if(rowId > 0)
			{
				Uri newUri = ContentUris.withAppendedId(DataBaseContants.CONTENT_DELETE_URI, rowId);
				getContext().getContentResolver().notifyChange(newUri, null);
				return newUri;
			}
			else
			{
				throw new IllegalArgumentException("Faied to insert row into " + uri);
			}			
		default:
			throw new IllegalArgumentException("Faied to insert row into " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		mDBOpenHelper = new DBOpenHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (uriMatcher.match(uri)) {
		case 1:
			qb.setTables(DataBaseContants.TABLE_DATA_NAME);
			break;
		case 2:
			qb.setTables(DataBaseContants.TABLE_DATA_NAME);
			qb.appendWhere(DataBaseContants.ID + "=");
			qb.appendWhere(uri.getPathSegments().get(1));
			break;
		case 3:
			qb.setTables(DataBaseContants.TABLE_DELETE_NAME);
			break;
		case 4:
			qb.setTables(DataBaseContants.TABLE_DELETE_NAME);
			qb.appendWhere(DataBaseContants.DELETE_ID + "=");
			qb.appendWhere(uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);		
		}
		SQLiteDatabase mReadable = mDBOpenHelper.getReadableDatabase();
		Cursor c = qb.query(mReadable, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase mWriteable = mDBOpenHelper.getWritableDatabase();
		int count = 0;
		String id = null;
		switch (uriMatcher.match(uri)) {
		case 1:
			count = mWriteable.update(DataBaseContants.TABLE_DATA_NAME, values, where, whereArgs);
			break;
		case 2:
			id = uri.getPathSegments().get(1);
			count = mWriteable.update(DataBaseContants.TABLE_DATA_NAME, values, DataBaseContants.ID + "=" + id + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
		case 3:
			count = mWriteable.update(DataBaseContants.TABLE_DELETE_NAME, values, where, whereArgs);
			break;
		case 4:
			id = uri.getPathSegments().get(1);
			count = mWriteable.update(DataBaseContants.TABLE_DELETE_NAME, values, DataBaseContants.DELETE_ID + "=" + id + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknow URL "+ uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
