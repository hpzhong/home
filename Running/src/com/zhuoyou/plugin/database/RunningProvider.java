package com.zhuoyou.plugin.database;

import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
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
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "point_message2", 5);
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "point_message2/#", 6);
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "operation_time2", 7);
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "operation_time2/#", 8);
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "gps_sport2", 9);
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "gps_sport2/#", 10);
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "gps_sync", 11);
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "gps_sync/#", 12);
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "action_msg_info", 13);
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "action_msg_info/#", 14);
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "point_temp", 15);
		uriMatcher.addURI(DataBaseContants.AUTHORITY, "point_temp/#", 16);
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
		case 5:
			count = mWriteable.delete(DataBaseContants.TABLE_POINT_NAME, where, whereArgs);
			break;
		case 6:
			id = uri.getPathSegments().get(1);
			if(TextUtils.isEmpty(where))
			{
				where = DataBaseContants.GPS_ID + "=" + id;
			}
			else
			{
				where = DataBaseContants.GPS_ID + "=" + id + " AND (" + where + ")";
			}
			count = mWriteable.delete(DataBaseContants.TABLE_POINT_NAME, where, whereArgs);
			break;
		case 7:
			count = mWriteable.delete(DataBaseContants.TABLE_OPERATION_NAME, where, whereArgs);
			break;
		case 8:
			id = uri.getPathSegments().get(1);
			if(TextUtils.isEmpty(where))
			{
				where = DataBaseContants.GPS_ID + "=" + id;
			}
			else
			{
				where = DataBaseContants.GPS_ID + "=" + id + " AND (" + where + ")";
			}
			count = mWriteable.delete(DataBaseContants.TABLE_OPERATION_NAME, where, whereArgs);
			break;
		case 9:
			count = mWriteable.delete(DataBaseContants.TABLE_GPSSPORT_NAME, where, whereArgs);
			break;
		case 10:
			id = uri.getPathSegments().get(1);
			if(TextUtils.isEmpty(where))
			{
				where = DataBaseContants.GPS_ID + "=" + id;
			}
			else
			{
				where = DataBaseContants.GPS_ID + "=" + id + " AND (" + where + ")";
			}
			count = mWriteable.delete(DataBaseContants.TABLE_GPSSPORT_NAME, where, whereArgs);
			break;
		case 11:
			count = mWriteable.delete(DataBaseContants.TABLE_GPS_SYNC, where, whereArgs);
			break;
		case 12:
			id = uri.getPathSegments().get(1);
			if(TextUtils.isEmpty(where))
			{
				where = DataBaseContants.GPS_ID + "=" + id;
			}
			else
			{
				where = DataBaseContants.GPS_ID + "=" + id + " AND (" + where + ")";
			}
			count = mWriteable.delete(DataBaseContants.TABLE_GPS_SYNC, where, whereArgs);
			break;
		case 13:
			count = mWriteable.delete(DataBaseContants.TABLE_ACTION_MSG, where, whereArgs);
			break;
		case 14:
			id = uri.getPathSegments().get(1);
			if(TextUtils.isEmpty(where))
			{
				where = DataBaseContants.ID + "=" + id;
			}
			else
			{
				where = DataBaseContants.ID + "=" + id + " AND (" + where + ")";
			}
			count = mWriteable.delete(DataBaseContants.TABLE_ACTION_MSG, where, whereArgs);
			break;
		case 15:
			count = mWriteable.delete(DataBaseContants.TEMP_POINT_NAME, where, whereArgs);
			break;
		case 16:
			id = uri.getPathSegments().get(1);
			if(TextUtils.isEmpty(where))
			{
				where = DataBaseContants.GPS_ID + "=" + id;
			}
			else
			{
				where = DataBaseContants.GPS_ID + "=" + id + " AND (" + where + ")";
			}
			count = mWriteable.delete(DataBaseContants.TEMP_POINT_NAME, where, whereArgs);
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
		case 5:
			return "vnd.android.cursor.dir/point";
		case 6:
			return "vnd.android.cursor.item/point";
		case 7:
			return "vnd.android.cursor.dir/operation";
		case 8:
			return "vnd.android.cursor.item/operation";
		case 9:
			return "vnd.android.cursor.dir/gpssport";
		case 10:
			return "vnd.android.cursor.item/gpssport";
		case 11:
			return "vnd.android.cursor.dir/gpssync";
		case 12:
			return "vnd.android.cursor.item/gpssync";
		case 13:
			return "vnd.android.cursor.dir/actionmsg";
		case 14:
			return "vnd.android.cursor.item/actionmsg";
		case 15:
			return "vnd.android.cursor.dir/temppoint";
		case 16:
			return "vnd.android.cursor.item/temppoint";
		default:
			throw new IllegalArgumentException("Unknown URI" + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (uriMatcher.match(uri) != 1 && uriMatcher.match(uri) != 3 && uriMatcher.match(uri) != 5 
				&& uriMatcher.match(uri) != 7 && uriMatcher.match(uri) != 9 && uriMatcher.match(uri) != 11  
				&& uriMatcher.match(uri) != 13 && uriMatcher.match(uri) != 15) {  
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
		case 5:
			rowId = mWriteable.insert(DataBaseContants.TABLE_POINT_NAME, "point", values);
			if(rowId > 0)
			{
				Uri newUri = ContentUris.withAppendedId(DataBaseContants.CONTENT_URI_POINT, rowId);
				getContext().getContentResolver().notifyChange(newUri, null);
				return newUri;
			}
			else
			{
				throw new IllegalArgumentException("Faied to insert row into " + uri);
			}			
		case 7:
			rowId = mWriteable.insert(DataBaseContants.TABLE_OPERATION_NAME, "operation", values);
			if(rowId > 0)
			{
				Uri newUri = ContentUris.withAppendedId(DataBaseContants.CONTENT_URI_OPERATION, rowId);
				getContext().getContentResolver().notifyChange(newUri, null);
				return newUri;
			}
			else
			{
				throw new IllegalArgumentException("Faied to insert row into " + uri);
			}
		case 9:
			rowId = mWriteable.insert(DataBaseContants.TABLE_GPSSPORT_NAME, "gpssport", values);
			if(rowId > 0)
			{
				Uri newUri = ContentUris.withAppendedId(DataBaseContants.CONTENT_URI_GPSSPORT, rowId);
				getContext().getContentResolver().notifyChange(newUri, null);
				return newUri;
			}
			else
			{
				throw new IllegalArgumentException("Faied to insert row into " + uri);
			}
		case 11:
			rowId = mWriteable.insert(DataBaseContants.TABLE_GPS_SYNC, "gpssync", values);
			if(rowId > 0)
			{
				Uri newUri = ContentUris.withAppendedId(DataBaseContants.CONTENT_URI_GPSSPORT, rowId);
				getContext().getContentResolver().notifyChange(newUri, null);
				return newUri;
			}
			else
			{
				throw new IllegalArgumentException("Faied to insert row into " + uri);
			}
		case 13:
			rowId = mWriteable.insert(DataBaseContants.TABLE_ACTION_MSG, "actionmsg", values);
			if(rowId > 0)
			{
				Uri newUri = ContentUris.withAppendedId(DataBaseContants.CONTENT_MSG_URI, rowId);
				getContext().getContentResolver().notifyChange(newUri, null);
				return newUri;
			}
			else
			{
				throw new IllegalArgumentException("Faied to insert row into " + uri);
			}
		case 15:
			rowId = mWriteable.insert(DataBaseContants.TEMP_POINT_NAME, "temppoint", values);
			if(rowId > 0)
			{
				Uri newUri = ContentUris.withAppendedId(DataBaseContants.CONTENT_URI_POINT, rowId);
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
		case 5:
			qb.setTables(DataBaseContants.TABLE_POINT_NAME);
			break;
		case 6:
			qb.setTables(DataBaseContants.TABLE_POINT_NAME);
			qb.appendWhere(DataBaseContants.GPS_ID + "=");
			qb.appendWhere(uri.getPathSegments().get(1));
			break;
		case 7:
			qb.setTables(DataBaseContants.TABLE_OPERATION_NAME);
			break;
		case 8:
			qb.setTables(DataBaseContants.TABLE_OPERATION_NAME);
			qb.appendWhere(DataBaseContants.GPS_ID + "=");
			qb.appendWhere(uri.getPathSegments().get(1));
			break;
		case 9:
			qb.setTables(DataBaseContants.TABLE_GPSSPORT_NAME);
			break;
		case 10:
			qb.setTables(DataBaseContants.TABLE_GPSSPORT_NAME);
			qb.appendWhere(DataBaseContants.GPS_ID + "=");
			qb.appendWhere(uri.getPathSegments().get(1));
			break;
		case 11:
			qb.setTables(DataBaseContants.TABLE_GPS_SYNC);
			break;
		case 12:
			qb.setTables(DataBaseContants.TABLE_GPS_SYNC);
			qb.appendWhere(DataBaseContants.GPS_ID + "=");
			qb.appendWhere(uri.getPathSegments().get(1));
			break;
		case 13:
			qb.setTables(DataBaseContants.TABLE_ACTION_MSG);
			break;
		case 14:
			qb.setTables(DataBaseContants.TABLE_ACTION_MSG);
			qb.appendWhere(DataBaseContants.ID + "=");
			qb.appendWhere(uri.getPathSegments().get(1));
			break;
		case 15:
			qb.setTables(DataBaseContants.TEMP_POINT_NAME);
			break;
		case 16:
			qb.setTables(DataBaseContants.TEMP_POINT_NAME);
			qb.appendWhere(DataBaseContants.GPS_ID + "=");
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
		case 5:
			count = mWriteable.update(DataBaseContants.TABLE_POINT_NAME, values, where, whereArgs);
			break;
		case 6:
			id = uri.getPathSegments().get(1);
			count = mWriteable.update(DataBaseContants.TABLE_POINT_NAME, values, DataBaseContants.GPS_ID + "=" + id + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
		case 7:
			count = mWriteable.update(DataBaseContants.TABLE_OPERATION_NAME, values, where, whereArgs);
			break;
		case 8:
			id = uri.getPathSegments().get(1);
			count = mWriteable.update(DataBaseContants.TABLE_OPERATION_NAME, values, DataBaseContants.GPS_ID + "=" + id + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
		case 9:
			count = mWriteable.update(DataBaseContants.TABLE_GPSSPORT_NAME, values, where, whereArgs);
			break;
		case 10:
			id = uri.getPathSegments().get(1);
			count = mWriteable.update(DataBaseContants.TABLE_GPSSPORT_NAME, values, DataBaseContants.GPS_ID + "=" + id + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
		case 11:
			count = mWriteable.update(DataBaseContants.TABLE_GPS_SYNC, values, where, whereArgs);
			break;
		case 12:
			id = uri.getPathSegments().get(1);
			count = mWriteable.update(DataBaseContants.TABLE_GPS_SYNC, values, DataBaseContants.GPS_ID + "=" + id + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
		case 13:
			count = mWriteable.update(DataBaseContants.TABLE_ACTION_MSG, values, where, whereArgs);
			break;
		case 14:
			id = uri.getPathSegments().get(1);
			count = mWriteable.update(DataBaseContants.TABLE_ACTION_MSG, values, DataBaseContants.ID + "=" + id + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
		case 15:
			count = mWriteable.update(DataBaseContants.TEMP_POINT_NAME, values, where, whereArgs);
			break;
		case 16:
			id = uri.getPathSegments().get(1);
			count = mWriteable.update(DataBaseContants.TEMP_POINT_NAME, values, DataBaseContants.GPS_ID + "=" + id + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknow URL "+ uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override 
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException{ 
		SQLiteDatabase db = mDBOpenHelper.getWritableDatabase(); 
		db.beginTransaction();//开始事务 
		try{ 
			ContentProviderResult[]results = super.applyBatch(operations); 
			db.setTransactionSuccessful();//设置事务标记为successful 
			return results; 
		}finally { 
			db.endTransaction();//结束事务 
		} 
	} 
}
