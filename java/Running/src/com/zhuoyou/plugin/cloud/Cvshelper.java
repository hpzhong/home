package com.zhuoyou.plugin.cloud;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;

import com.zhuoyou.plugin.database.DBOpenHelper;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.running.Tools;

public class Cvshelper {


	public final static String SPORT_ADD_FILE = "sport_info_add";
	public final static String SPORT_UPDATE_FILE = "sport_info_update";
	public final static String SPORT_LOCAL_FILE = "sport_info_local";
	public final static String SPORT_DELETE_FILE = "sport_info_delete";
	public final static String SPORT_DOWN_FILE = "mars_info_down";

	private static ContentResolver mContentResolver;
	String TAG = "CVS";
	boolean Debug = true;
	SQLiteDatabase database;
	Context mContext;
	String accountid = "";
	//only for steps such as mars3
	private int postSportType;
	
	public Cvshelper(Context context, int type){
		mContext = context;		
		accountid = Tools.getOpenId(mContext);
		mContentResolver = context.getContentResolver();
		postSportType = type;
	}
	
	
	/*
	 * this function is used for export running db data to file
	 * param:type which tab data will be use. 
	 *    0: data add 
	 *    1: data mofefy
	 *    1: data delete
	 *    1: data local
	 */

	public void ExportDateCSVBYTYPE(int type) {
        File saveFileAdd;
        File saveFileUpdate;
        File saveFileLocal;
        File saveFileDelete;
    	Cursor cursor = null;
        FileWriter fw;  
        BufferedWriter bfw;

        //first get update file
        
		ContentResolver cr = mContext.getContentResolver();
    	
		//note:we haven't query with time here,please confirm? 
		saveFileAdd = new File(GetDir(), SPORT_ADD_FILE);
    	saveFileUpdate = new File(GetDir(), SPORT_UPDATE_FILE);
    	saveFileLocal = new File(GetDir(), SPORT_LOCAL_FILE); 
    	saveFileDelete = new File(GetDir(), SPORT_DELETE_FILE); 

    	if(!saveFileAdd.exists()){
    		try {
				saveFileAdd.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	if(!saveFileUpdate.exists()){
    		try {
    			saveFileUpdate.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	if(!saveFileLocal.exists()){
    		try {
    			saveFileLocal.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	if(!saveFileDelete.exists()){
    		try {
    			saveFileDelete.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	if (postSportType != 2)
    		return;
    		  	
		//type which have not commit to cloud
		if(type == 0){
			cursor = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id",
					"date", "time_duration", "time_start", "time_end", "steps",
					"kilometer", "calories", "weight", "bmi", "img_uri",
					"img_explain", "sports_type", "type", "img_cloud", "complete",
					"statistics", "data_from" }, DataBaseContants.SYNC_STATE + "  = ? ",
					new String[] { Integer.toString(0) }, null);	
		}else
		//type which have modefied	
		if(type == 1){
			cursor = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id",
					"date", "time_duration", "time_start", "time_end", "steps",
					"kilometer", "calories", "weight", "bmi", "img_uri",
					"img_explain", "sports_type", "type", "img_cloud", "complete",
					"statistics", "data_from" }, DataBaseContants.SYNC_STATE + "  = ? ",
					new String[] { Integer.toString(2) }, null);
		}else 
		//type which should be deleted	
		if(type == 2){
			cursor = cr.query(DataBaseContants.CONTENT_DELETE_URI,
					new String[] { "delete_value" }, null, null, null);
		}else
		//type which local exist,only id
		if(type == 3){
			cursor = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id" },
					null, null, null);
		}
    	
		int count = cursor.getCount();
		if (count > 0 && cursor.moveToFirst()) {
			if(type == 0){
		        try {
		            fw = new FileWriter(saveFileAdd);  
		            bfw = new BufferedWriter(fw); 
					do {
						for(int i = 0;i<18;i++){
						   	if(i == 0) {
	                            bfw.write(cursor.getString(i) + ','+accountid+',');                      		
	                    	} else if (i == 17) {
	                    		if( cursor.getString(i)!= null&&cursor.getString(i)!=""){                    			
	                                bfw.write(cursor.getString(i));  
	                    		}
                    		} else {
	                    		if( cursor.getString(i)!= null&&cursor.getString(i)!=""){                    			
	                                bfw.write(cursor.getString(i) + ',');  
	                    		}else{
	                                bfw.write(',');                      			
	                    		}
                    		}  
						}
	                    bfw.newLine();  
					} while (cursor.moveToNext());
					bfw.flush();  
		            bfw.close();  
		        } catch (IOException e) {  
		            // TODO Auto-generated catch block  
		            e.printStackTrace();  
		        }
			}else if(type == 1){
		        try {
		            fw = new FileWriter(saveFileUpdate);  
		            bfw = new BufferedWriter(fw); 
					do {
						for(int i = 0;i<18;i++){
						   	if(i == 0) {
						   	 bfw.write(cursor.getString(i) + ','+accountid+',');                  		
	                    	} else if (i == 17) {
	                    		if( cursor.getString(i)!= null&&cursor.getString(i)!=""){                    			
	                                bfw.write(cursor.getString(i));  
	                    		}
                    		} else {
	                    		if( cursor.getString(i)!= null&&cursor.getString(i)!=""){                    			
	                                bfw.write(cursor.getString(i) + ',');  
	                    		}else{
	                                bfw.write(',');                      			
	                    		}
                    		}  
						}
	                    bfw.newLine();  
					} while (cursor.moveToNext());
					bfw.flush();  
		            bfw.close();  
		        } catch (IOException e) {  
		            // TODO Auto-generated catch block  
		            e.printStackTrace();  
		        }
			}else if(type == 2){
		        try {
		            fw = new FileWriter(saveFileDelete);  
		            bfw = new BufferedWriter(fw); 
					for (int i = 0; i < count; i++) {
						String deletesport = cursor.getLong(cursor.getColumnIndex(DataBaseContants.DELETE_VALUE))+"";
	                    bfw.write(deletesport); 
	                    bfw.newLine();                        
						cursor.moveToNext();
					}
					bfw.flush();  
		            bfw.close();  
		        } catch (IOException e) {  
		            // TODO Auto-generated catch block  
		            e.printStackTrace();  
		        }
			}else if(type == 3){        
		        try {
		            fw = new FileWriter(saveFileLocal);  
		            bfw = new BufferedWriter(fw); 
					for (int i = 0; i < count; i++) {
						String getSportDataId = cursor.getLong(cursor.getColumnIndex(DataBaseContants.ID)) + "";
	                    bfw.write(getSportDataId); 
	                    bfw.newLine();                        
						cursor.moveToNext();
					}
					bfw.flush();  
		            bfw.close();  
		        } catch (IOException e) {  
		            // TODO Auto-generated catch block  
		            e.printStackTrace();  
		        }
			}
		}
		cursor.close();
		cursor = null;
    }	
	/*
	 * this function is used to zip local file to named zipFileString
	 * 
	 */
	public void ExportCVSToZip(String zipFileString)throws Exception{  
//		private static void ZipFiles(String path,String zipFileString)
	        String path = GetDir()+"/"+zipFileString;
	        java.util.zip.ZipOutputStream outZip = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(path)); 
	        String filename[] = {SPORT_ADD_FILE,SPORT_UPDATE_FILE,SPORT_LOCAL_FILE,SPORT_DELETE_FILE,
	        		GPSDataSync.GPS_SPORT_ADD,GPSDataSync.GPS_SPORT_DELETE,GPSDataSync.GPS_SPORT_UPDATE,
	        		GPSDataSync.GPS_OPERATION_ADD,GPSDataSync.GPS_OPERATION_DELETE,GPSDataSync.GPS_OPERATION_UPDATE,
	        		GPSDataSync.GPS_POINT_ADD,GPSDataSync.GPS_POINT_DELETE,GPSDataSync.GPS_POINT_UPDATE
	        };
	        if(Debug)
	        	Log.d("zhouzhongbo","path = "+path);

	        String filePath = GetDir();
	        for(int i = 0;i<filename.length;i++){
	        	if(new File(filePath,filename[i]).exists()){
		            java.util.zip.ZipEntry zipEntry =  new java.util.zip.ZipEntry(filename[i]);  
		            java.io.FileInputStream inputStream = new java.io.FileInputStream(new File(GetDir()+"/"+filename[i]));  
		            outZip.putNextEntry(zipEntry);
		            int len;  
		            byte[] buffer = new byte[4096];	              
		            while((len=inputStream.read(buffer)) != -1)  
		            {  
		            	outZip.write(buffer, 0, len);  
		            }       
		            outZip.closeEntry();
		            inputStream.close();
	        	}
	        }
	        outZip.close();
	}  
		
	/*
	 * this funtion is used for import data to db which from cloud,and update msync flag to 1;
	 * pragram: type  whic file will be import
	 *    0:sport_info inport
	 *    1:heartrate_info inport
	 *    2:buddies_info inport
	 */
	public void ImportCSVFile(Context con){
        File downfile;
        BufferedReader br;
        List<String> tmp= new ArrayList<String>(); 
        List<String> ids= new ArrayList<String>(); 
        int last_position = 0;
    	downfile = new File(GetDir()+"/"+SPORT_DOWN_FILE);
        //import data from cloud
        if(downfile == null||!downfile.exists())
        	return;
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		try {
			br = new BufferedReader(new FileReader(downfile));
	        String s = null;			
	        while((s = br.readLine())!=null){
	            System.out.println(s);
	            int i=0;
	            for(;i<s.length();i++){
	            	if(s.charAt(i) ==','){
	            		if(last_position != 0)
	            			tmp.add(s.substring(last_position+1, i));
	            		else
	            			tmp.add(s.substring(last_position, i));		
	            		last_position = i;
	            	}
	            }
	            tmp.add(s.substring(last_position+1, i));
	            String id = tmp.get(0);
	            if (tmp.get(16).equals("1")) {
	            	String day = tmp.get(1);
					// 检查数据库中是否存在该天的数据
					Cursor c = mContentResolver.query(DataBaseContants.CONTENT_URI, new String[] { DataBaseContants.ID }, 
							DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { day, "1" }, null);
					if(c.getCount() > 0 && c.moveToFirst()){
						deleteDateInfo(con,Long.valueOf(tmp.get(0)));
					} else {		
						if (ids.indexOf(id) == -1) {
							ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
									.withValue(DataBaseContants.ID, tmp.get(0))
									.withValue(DataBaseContants.DATE, tmp.get(1))
									.withValue(DataBaseContants.STEPS, (tmp.get(5)!="")?tmp.get(5):null)
									.withValue(DataBaseContants.KILOMETER, (tmp.get(6)!="")?tmp.get(6):null)
									.withValue(DataBaseContants.CALORIES, (tmp.get(7)!="")?tmp.get(7):null)
									.withValue(DataBaseContants.BMI, (tmp.get(9)!="")?tmp.get(9):null)
									.withValue(DataBaseContants.TYPE, (tmp.get(13)!="")?tmp.get(13):null)
									.withValue(DataBaseContants.COMPLETE,  tmp.get(15))
									.withValue(DataBaseContants.SYNC_STATE, 1)
									.withValue(DataBaseContants.STATISTICS, 1)
//									.withValue(DataBaseContants.DATA_FROM,  (tmp.get(17)!="")?tmp.get(17):null)
									.withYieldAllowed(true).build();
							operations.add(op1);	
							ids.add(id);
						}
					}
					c.close();
					c = null;
	            } else if (tmp.get(16).equals("2")) {
	            	String day = tmp.get(1);
	            	String data_from = tmp.get(17);
					// 检查数据库中是否存在该天的数据
					Cursor c = mContentResolver.query(DataBaseContants.CONTENT_URI, new String[] { DataBaseContants.ID }, 
							DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, "2", data_from }, null);
					if(c.getCount() > 0 && c.moveToFirst()){
						deleteDateInfo(con,Long.valueOf(tmp.get(0)));
					} else {		
						if (ids.indexOf(id) == -1) {
							ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
									.withValue(DataBaseContants.ID, tmp.get(0))
									.withValue(DataBaseContants.DATE, tmp.get(1))
									.withValue(DataBaseContants.STEPS, (tmp.get(5)!="")?tmp.get(5):null)
									.withValue(DataBaseContants.KILOMETER, (tmp.get(6)!="")?tmp.get(6):null)
									.withValue(DataBaseContants.CALORIES, (tmp.get(7)!="")?tmp.get(7):null)
									.withValue(DataBaseContants.TYPE, (tmp.get(13)!="")?tmp.get(13):null)
									.withValue(DataBaseContants.COMPLETE,  tmp.get(15))
									.withValue(DataBaseContants.SYNC_STATE, 1)
									.withValue(DataBaseContants.STATISTICS, 2)
									.withValue(DataBaseContants.DATA_FROM,  data_from)
									.withYieldAllowed(true).build();
							operations.add(op1);
							ids.add(id);
						}
					}
					c.close();
					c = null;
	            } else {
					int type = Integer.parseInt(tmp.get(13));
					if (type == 1) {
						String day = tmp.get(1);
						Cursor c = mContentResolver.query(DataBaseContants.CONTENT_URI, new String[] { DataBaseContants.ID }, DataBaseContants.DATE
								+ " = ? AND " + DataBaseContants.TYPE + " = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { day, "1", "0" }, null);
						if(c.getCount() > 0 && c.moveToFirst()){
							deleteDateInfo(con,Long.valueOf(tmp.get(0)));
						}else{
							if (ids.indexOf(id) == -1) {
								ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
										.withValue(DataBaseContants.ID, tmp.get(0))
										.withValue(DataBaseContants.DATE, tmp.get(1))
										.withValue(DataBaseContants.TIME_START, (tmp.get(3)!="")?tmp.get(3):null)
										.withValue(DataBaseContants.CONF_WEIGHT, (tmp.get(8)!="")?tmp.get(8):null)
										.withValue(DataBaseContants.BMI, (tmp.get(9)!="")?tmp.get(9):null)
										.withValue(DataBaseContants.TYPE, type)
										.withValue(DataBaseContants.SYNC_STATE, 1)
										.withValue(DataBaseContants.STATISTICS, 0)
										.withYieldAllowed(true).build();
								operations.add(op1);
								ids.add(id);
							}
						}
						
					} else if (type == 2) {
						if(Integer.parseInt(tmp.get(12)) == 0){
							String day = tmp.get(1);
							String start = tmp.get(3);
							String end = tmp.get(4);
							String data_from = tmp.get(17);
							Cursor c = mContentResolver.query(DataBaseContants.CONTENT_URI, new String[] { DataBaseContants.ID, DataBaseContants.STEPS }, DataBaseContants.DATE
									+ "  = ? AND " + DataBaseContants.TIME_START + " = ? AND "+ DataBaseContants.SPORTS_TYPE + " = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, start, "0", "0", data_from }, null);
							if(c.getCount() > 0 && c.moveToFirst()){
								deleteDateInfo(con,Long.valueOf(tmp.get(0)));
							}else{
								if (ids.indexOf(id) == -1) {
									ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
											.withValue(DataBaseContants.ID, tmp.get(0))
											.withValue(DataBaseContants.DATE, tmp.get(1))
											.withValue(DataBaseContants.TIME_DURATION, (tmp.get(2)!="")?tmp.get(2):null)
											.withValue(DataBaseContants.TIME_START, (tmp.get(3)!="")?tmp.get(3):null)
											.withValue(DataBaseContants.TIME_END, (tmp.get(4)!="")?tmp.get(4):null)
											.withValue(DataBaseContants.STEPS, (tmp.get(5)!="")?tmp.get(5):null)
											.withValue(DataBaseContants.KILOMETER, (tmp.get(6)!="")?tmp.get(6):null)
											.withValue(DataBaseContants.CALORIES, (tmp.get(7)!="")?tmp.get(7):null)
											.withValue(DataBaseContants.SPORTS_TYPE, tmp.get(12))
											.withValue(DataBaseContants.TYPE, tmp.get(13))
											.withValue(DataBaseContants.SYNC_STATE, 1)
											.withValue(DataBaseContants.STATISTICS, 0)
											.withValue(DataBaseContants.DATA_FROM, data_from)
											.withYieldAllowed(true).build();
									operations.add(op1);
									ids.add(id);
								}
							}
							c.close();
							c = null;
						}else{
							if (ids.indexOf(id) == -1) {
								ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
										.withValue(DataBaseContants.ID, tmp.get(0))
										.withValue(DataBaseContants.DATE, tmp.get(1))
										.withValue(DataBaseContants.TIME_DURATION, (tmp.get(2)!="")?tmp.get(2):null)
										.withValue(DataBaseContants.TIME_START, (tmp.get(3)!="")?tmp.get(3):null)
										.withValue(DataBaseContants.TIME_END, (tmp.get(4)!="")?tmp.get(4):null)
										.withValue(DataBaseContants.STEPS, (tmp.get(5)!="")?tmp.get(5):null)
										.withValue(DataBaseContants.KILOMETER, (tmp.get(6)!="")?tmp.get(6):null)
										.withValue(DataBaseContants.CALORIES, (tmp.get(7)!="")?tmp.get(7):null)
										.withValue(DataBaseContants.SPORTS_TYPE, tmp.get(12))
										.withValue(DataBaseContants.TYPE, type)
										.withValue(DataBaseContants.SYNC_STATE, 1)
										.withValue(DataBaseContants.STATISTICS, 0)
										.withYieldAllowed(true).build();
								operations.add(op1);
								ids.add(id);
							}
						}
					} else if (type == 3) {
						if (ids.indexOf(id) == -1) {
							ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
									.withValue(DataBaseContants.ID, tmp.get(0))
									.withValue(DataBaseContants.DATE, tmp.get(1))
									.withValue(DataBaseContants.TIME_START, (tmp.get(3)!="")?tmp.get(3):null)
									.withValue(DataBaseContants.IMG_URI, (tmp.get(10)!="")?tmp.get(10):null)
									.withValue(DataBaseContants.EXPLAIN, (tmp.get(11)!="")?tmp.get(11):null)
									.withValue(DataBaseContants.TYPE, type)
									.withValue(DataBaseContants.SYNC_STATE, 1)
									.withValue(DataBaseContants.STATISTICS, 0)
									.withYieldAllowed(true).build();
							operations.add(op1);
							ids.add(id);
						}
					} else if (type == 4) {
						if (ids.indexOf(id) == -1) {
							ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
									.withValue(DataBaseContants.ID, tmp.get(0))
									.withValue(DataBaseContants.DATE, tmp.get(1))
									.withValue(DataBaseContants.TIME_START, (tmp.get(3)!="")?tmp.get(3):null)
									.withValue(DataBaseContants.EXPLAIN, (tmp.get(11)!="")?tmp.get(11):null)
									.withValue(DataBaseContants.TYPE, type)
									.withValue(DataBaseContants.SYNC_STATE, 1)
									.withValue(DataBaseContants.STATISTICS, 0)
									.withYieldAllowed(true).build();
							operations.add(op1);
							ids.add(id);
						}
					} else if (type == 5) {
						if (ids.indexOf(id) == -1) {
							ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
									.withValue(DataBaseContants.ID, tmp.get(0))
									.withValue(DataBaseContants.DATE, tmp.get(1))
									.withValue(DataBaseContants.TIME_DURATION, (tmp.get(2)!="")?tmp.get(2):null)
									.withValue(DataBaseContants.TIME_START, (tmp.get(3)!="")?tmp.get(3):null)
									.withValue(DataBaseContants.TIME_END, (tmp.get(4)!="")?tmp.get(4):null)
									.withValue(DataBaseContants.STEPS, (tmp.get(5)!="")?tmp.get(5):null)
									.withValue(DataBaseContants.KILOMETER, (tmp.get(6)!="")?tmp.get(6):null)
									.withValue(DataBaseContants.CALORIES, (tmp.get(7)!="")?tmp.get(7):null)
									.withValue(DataBaseContants.CONF_WEIGHT, (tmp.get(8)!="")?tmp.get(8):null)
									.withValue(DataBaseContants.BMI, (tmp.get(9)!="")?tmp.get(9):null)
									.withValue(DataBaseContants.IMG_URI,  (tmp.get(10)!="")?tmp.get(10):null)
									.withValue(DataBaseContants.EXPLAIN, (tmp.get(11)!="")?tmp.get(11):null)
									.withValue(DataBaseContants.TYPE, type)
									.withValue(DataBaseContants.SYNC_STATE, 1)
									.withValue(DataBaseContants.STATISTICS, 0)
									.withValue(DataBaseContants.DATA_FROM, (tmp.get(17)!="")?tmp.get(17):null)
									.withYieldAllowed(true).build();
							operations.add(op1);
							ids.add(id);	
						}
					} 
				
	            }
	            last_position = 0;
	            tmp.clear();
	        }
	        br.close();
	        ids.clear();
	        if (operations != null && operations.size() > 0)
	        	mContentResolver.applyBatch(DataBaseContants.AUTHORITY, operations);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}

	
	/*
	 *function : update local data flag:msync =1 and delete data in tab_delete 
	 * 
	 */
	public void UpdataLocalDate(){
    	if (postSportType != 2)
    		return;
		emove_db_update();
		emove_db_delete();
	}	
	
	/*
	 * function:update local data flag :msync = 1
	 */
	private void emove_db_update(){
		ContentResolver cr = mContext.getContentResolver();
		ContentValues updateValues = new ContentValues();
		updateValues.put(DataBaseContants.SYNC_STATE, 1);
		cr.update(DataBaseContants.CONTENT_URI, updateValues, null, null);
	}
	
	/*
	 * function:delete data in tab_delete
	 */
	private void emove_db_delete(){
		DBOpenHelper mDBOpenHelper = new DBOpenHelper(mContext);
		String sql = "DELETE FROM " + DataBaseContants.TABLE_DELETE_NAME +";";
		SQLiteDatabase db =  mDBOpenHelper.getWritableDatabase();
		db.execSQL(sql);
		String sql2 = "update sqlite_sequence set seq=0 where name='"+DataBaseContants.TABLE_DELETE_NAME+"'";
		db.execSQL(sql2);
		mDBOpenHelper.close();
	}
	
	
	/*
	 * function :get useful dir
	 */
	public String GetDir(){
		String MOFEI_DIR;
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			File sdCardDir = Environment.getExternalStorageDirectory();  
			MOFEI_DIR = Environment.getExternalStorageDirectory()+"/emove_tmp";
		} else {
			MOFEI_DIR = "com/zhuoyou/plugin/running/emove_tmp";
		}
        if(Debug)
        	Log.d("zhouzhongbo","my dir = "+MOFEI_DIR);
		File destDir = new File(MOFEI_DIR);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		return MOFEI_DIR;
	}   
	
	
	public void CVSUnzipFile(String zipFileString, String outPathString)throws Exception {
//	    public static void UnZipFolder(String zipFileString, String outPathString)
	        java.util.zip.ZipInputStream inZip = new java.util.zip.ZipInputStream(new java.io.FileInputStream(zipFileString));  
	        java.util.zip.ZipEntry zipEntry;  
	        String szName = "";
	        File decodepath = new File(outPathString);
	        //make sure outpath is exist,and is a directory!
	        if(decodepath.exists()){
	        	if(!decodepath.isDirectory()){
	        		decodepath.delete();
	        		decodepath.mkdir();
	        	}
	        }else{
	        	decodepath.mkdirs();
	        }
	        
	        while ((zipEntry = inZip.getNextEntry()) != null) {  
	            szName = zipEntry.getName();
		        if(Debug)
		        	Log.d("zhouzhongbo","szName ="+szName);
	            
	            if (zipEntry.isDirectory()) {            
	                // get the folder name of the widget  
	                szName = szName.substring(0, szName.length() - 1);  
	                File folder = new File(outPathString + File.separator + szName);  
	                folder.mkdirs();  
	          
	            } else {  
	                File file = new File(outPathString + File.separator + szName);  
	                file.createNewFile();  
	                // get the output stream of the file  
	                java.io.FileOutputStream out = new java.io.FileOutputStream(file);  
	                int len;  
	                byte[] buffer = new byte[1024];  
	                // read (len) bytes into buffer  
	                while ((len = inZip.read(buffer)) != -1) {  
	                    // write (len) byte from buffer at the position 0  
	                    out.write(buffer, 0, len);  
	                    out.flush();  
	                }  
	                out.close();  
	            }  
	        }//end of while  
	        inZip.close();       
    }
	
	
	public void CVSCloseDb(){
		database.close();
	}
	
	private static void deleteDateInfo(Context con,long id) {
		ContentValues values = new ContentValues();
		values.put(DataBaseContants.DELETE_VALUE, id);
		con.getContentResolver().insert(DataBaseContants.CONTENT_DELETE_URI, values);
	}
}
