package com.zhuoyou.plugin.antilost;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
		if (uri.toString().endsWith("anti_lost"))
		{
			return "anti_lost" ;
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
		Log.i("gchk","anti lost contentprovider create");
		String path = "/data/data/" + getContext().getPackageName() + "/files/anti_lost";
		Log.i("gchk","anti lost file create path = " + path);
		File file = new File(path);
		if(!file.exists()){
			boolean ret  =false;
			try {
				ret = file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.i("gchk","anti lost file create ret = " + ret);
		}
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
		if ("anti_lost".equals(getType(uri)))
		{
			File file = new File("/data/data/" + getContext().getPackageName() + "/files/anti_lost");
			boolean ret = false;
			if (file.exists())
			{
				return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY) ;
			}else{
				try {
					ret=  file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if(ret){
					return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY) ;
				}
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
