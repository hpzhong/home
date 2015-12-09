package com.zhuoyou.plugin.autocamera;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class MyContentProvider extends ContentProvider
{
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		return 0;
	}

	@Override
	public String getType(Uri uri)
	{
		if (uri.toString().endsWith("autocamera"))
		{
			return "autocamera" ;
		}
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		return null;
	}

	@Override
	public boolean onCreate()
	{
		Log.i("gchk","autocamera contentprovider create");
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		return null;
	}
	

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException
	{
		if ("autocamera".equals(getType(uri)))
		{
			File file = new File("/data/data/"+getContext().getPackageName()+"/files/autocamera") ;
			if (file.exists())
			{
				return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY) ;
			}
		}
		throw new FileNotFoundException(uri.getPath()) ;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		return 0;
	}

}
