package com.zhuoyou.plugin.cloud;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Message;

import com.zhuoyou.plugin.running.HomePageFragment;

public class HttpConnect {
    
    /**
     * sdcard is invalidated if return it
     */
    public static final int DOWN_RESULT_SDCARD_LOST = 0;
    /**
     * you can use Thread.interrupt() to stop download thread, now it should be return
     */
    public static final int DOWN_RESULT_USER_PAUSED = 1;
    /**
     * if http timeout or http error while downloading, it should be return
     */
    public static final int DOWN_RESULT_HTTP_TIMEOUT_OR_ERROR = 2;
    /**
     * if download complete, it will be return
     */
    public static final int DOWN_RESULT_SUCCESS = 3;
    
    
    private DefaultHttpClient mHttpClient = null;
    
    /**
     * fault tolerant buffer
     */
    private static final long FAULT_TOLERANT_BEFFER = 1024;
    
    
    /**
     * if http timeout or http error while uploading, it should be return
     */
    public static final int UPLOAD_RESULT_HTTP_ERROR = 0;
    /**
     * if upload success, it will be return
     */
    public static final int UPLOAD_RESULT_SUCCESS = 1;
    /**
     * if can't get the md5 of upload file, it will be return, please check file
     */
    public static final int UPLOAD_RESULT_FILE_MD5_NULL = 2;

    /**
     * 
     * @param url the post address
     * @param params http params
     * @param filePath file path of upload file, it must be the absolute path
     * @return {@link UPLOAD_RESULT_HTTP_ERROR}<br/>
     *         {@link UPLOAD_RESULT_SUCCESS}<br/>
     *         {@link UPLOAD_RESULT_FILE_MD5_NULL}
     */
    public int uploadFile(String url, HashMap<String, String> params, String filePath) {
        try {
            File file = new File(filePath);
            String md5 = getFileMd5(file.getAbsolutePath());
            if(md5 == null) {
                return UPLOAD_RESULT_FILE_MD5_NULL;
            }
            params.put("uploadFileMd5", md5);       //add md5 params
            params.put("uploadFileSize", Long.toString(file.length()));
            params.put("action", Integer.toString(1));
            
            String responseStr = postExternalFile(url, params, null);
            if(responseStr != null) {
                JSONObject jo = new JSONObject(responseStr);
                int result = jo.getInt("result");
                if(result == 0) {
                    int exist = jo.getInt("exist");
                    FormFile formFile = null;
                    if(exist == 1){
                        long uploadedSize = jo.getLong("fileSize");
                        uploadedSize -= FAULT_TOLERANT_BEFFER;
                        if(uploadedSize < 0) {
                            uploadedSize = 0;
                        }
                        formFile = new FormFile(file.getName(), file, "datafile", null, uploadedSize);
                        
                    }else {
                        formFile = new FormFile(file.getName(), file, "datafile", null, 0);
                        
                    }
                    
                    params.put("action", Integer.toString(2));
                    responseStr = postExternalFile(url, params, formFile);
                    if(responseStr != null) {
                        jo = new JSONObject(responseStr);
                        result = jo.getInt("result");
                        if(result == 0) {
                            return UPLOAD_RESULT_SUCCESS;
                        }
                    }
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        
        return UPLOAD_RESULT_HTTP_ERROR;
    }
    
    
    
    /**
     * download file
     * @param url the post address
     * @param headers http headers
     * @param params http params
     * @param filePath file path of download file, it must be the absolute path
     * @return {@link #DOWN_RESULT_SDCARD_LOST}<br/>
     *         {@link #DOWN_RESULT_USER_PAUSED}<br/>
     *         {@link #DOWN_RESULT_HTTP_TIMEOUT_OR_ERROR}<br/>
     *         {@link #DOWN_RESULT_SUCCESS}<br/>
     */
    public int downloadFile(String url, HashMap<String, String> headers, HashMap<String, String> params, String filePath) {
        File file = new File(filePath);
        long currSize = file.length();
        currSize -= FAULT_TOLERANT_BEFFER;
        if(currSize < 0) {
            currSize = 0;
        }
        
        ArrayList<BasicNameValuePair> bnvp = new ArrayList<BasicNameValuePair>();
        BasicNameValuePair pair = null;
        
        for(Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            pair = new BasicNameValuePair(key, value);
            bnvp.add(pair);
        }
        
        HttpResponse response = doPost(url, headers, bnvp);
        if(response == null) {
            onShutdownConn();
            return DOWN_RESULT_HTTP_TIMEOUT_OR_ERROR;
        }
        StatusLine status = response.getStatusLine();
        if(status.getStatusCode() != HttpStatus.SC_OK
                && status.getStatusCode() != HttpStatus.SC_PARTIAL_CONTENT) {
            onShutdownConn();
            return DOWN_RESULT_HTTP_TIMEOUT_OR_ERROR;
        }
        
        long fileSize = getDownloadFileSize(response);
        
        if(headers == null){
            headers = new HashMap<String, String>();
        }
        headers.put("Range", "bytes=" + Long.toString(currSize) + "-" + Long.toString(fileSize));
        
        response = doPost(url, headers, bnvp);
        if(response == null) {
            onShutdownConn();
            return DOWN_RESULT_HTTP_TIMEOUT_OR_ERROR;
        }
        status = response.getStatusLine();
        if(status.getStatusCode() != HttpStatus.SC_OK
                && status.getStatusCode() != HttpStatus.SC_PARTIAL_CONTENT) {
            onShutdownConn();
            return DOWN_RESULT_HTTP_TIMEOUT_OR_ERROR;
        }
        
        int ret = writeFile(response, currSize, fileSize, filePath);
        onShutdownConn();
        return ret;
    }
    
    
    
    
    
    
    private void onShutdownConn(){
        if(mHttpClient != null){
            mHttpClient.getConnectionManager().shutdown();
        }
        mHttpClient = null;
    }
    
    
    private HttpResponse doPost(String url, HashMap<String, String> headers, ArrayList<BasicNameValuePair> bnvp) {
        HttpResponse response = null;
        HttpParams httpParam = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParam, 30*1000);
        HttpConnectionParams.setSoTimeout(httpParam, 30*1000);
        mHttpClient = new DefaultHttpClient(httpParam);
        HttpContext localcontext = new BasicHttpContext();
        try {
            HttpHost host = null;
            HttpPost httpPost = null;
            if(url.contains("https")) {
                Uri u = Uri.parse(url);
                host = new HttpHost(u.getHost(), 443, u.getScheme());
                httpPost = new HttpPost(u.getPath());
            }else {
                httpPost = new HttpPost(url);
            }
            
            if(headers != null) {
                for(Map.Entry<String, String> entry : headers.entrySet()) {
                    String headKey = entry.getKey();
                    String value = entry.getValue();
                    httpPost.addHeader(headKey, value);
                }
            }
            
            if(bnvp != null) {
                httpPost.setEntity(new UrlEncodedFormEntity(bnvp));
            }
            
            if(url.contains("https")) {
                response = mHttpClient.execute(host, httpPost);
            }else{
                response = mHttpClient.execute(httpPost, localcontext);
            }
            
        }catch(Exception e) {
            e.printStackTrace();
        }
        
        return response;
    }
    
    
    private long getDownloadFileSize(HttpResponse response) {
        long fileSize = 0;
        try {
            InputStream in = response.getEntity().getContent();
            fileSize = in.available();
        }catch(Exception e) {
            e.printStackTrace();
        }
        
        return fileSize;
    }
    
    
    private int writeFile(HttpResponse response, long currSize, long totalSize, String filePath) {
        RandomAccessFile otaFile = null;
        try {
            InputStream in = response.getEntity().getContent();
            try{
                otaFile = new RandomAccessFile(filePath, "rws");
                otaFile.seek(currSize);
            }catch(Exception e) {
                e.printStackTrace();
                return DOWN_RESULT_SDCARD_LOST;
            }
            
            byte[] buff = new byte[4096];
            int readCount = 0;
            int downlaodcount = 0;
            while((readCount = in.read(buff, 0, 4096)) > 0) {
                try {
                    if(Thread.interrupted()) {
                        return DOWN_RESULT_USER_PAUSED;
                    }
                    otaFile.write(buff, 0, readCount);
                    downlaodcount += readCount;
                    if (downlaodcount >= totalSize / 2 && downlaodcount < totalSize / 2 + 4096) {
                    	if (HomePageFragment.mHandler != null) {
            				Message message = new Message();
            				message.what = HomePageFragment.SYNC_DEVICE_PROGRESS;
            				message.obj = "80%";
            				HomePageFragment.mHandler.sendMessage(message);
            			}
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                    return DOWN_RESULT_SDCARD_LOST;
                }
            }
            long downSize = otaFile.length();
            otaFile.close();
            if(downSize < totalSize) {
                return DOWN_RESULT_HTTP_TIMEOUT_OR_ERROR;
            }
            return DOWN_RESULT_SUCCESS;
        }catch(Exception e) {
            e.printStackTrace();
        }finally {
            try{
                if(otaFile != null) {
                    otaFile.close();
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        return DOWN_RESULT_HTTP_TIMEOUT_OR_ERROR;
    }
    
    
    
    
    
    
    
    private String postExternalFile(String address, HashMap<String, String> params, FormFile file) throws Exception {
        final String BOUNDARY = "----yphPostBoundaryMakeIn20141023";
        final String endline = "--" + BOUNDARY + "--\r\n";

        int fileDataLength = 0;
        if(file != null) {
            StringBuilder fileExplain = new StringBuilder();
            fileExplain.append("--");
            fileExplain.append(BOUNDARY);
            fileExplain.append("\r\n");
            fileExplain.append("Content-Disposition: form-data;name=\"" + file.getParameterName() + "\";filename=\"" + file.getFilename() + "\"\r\n");
            fileExplain.append("Content-Type: " + file.getContentType() + "\r\n\r\n");
            fileExplain.append("\r\n");
            fileDataLength += fileExplain.length();
            if (file.getInStream() != null) {
                fileDataLength += file.getFile().length();
            } else  {
                fileDataLength += file.getData().length;
            }
        }
        StringBuilder textEntity = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            textEntity.append("--");
            textEntity.append(BOUNDARY);
            textEntity.append("\r\n");
            textEntity.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
            textEntity.append(entry.getValue());
            textEntity.append("\r\n");
        }
        
        int dataLength = textEntity.toString().getBytes().length + fileDataLength + endline.getBytes().length;

        URL url = new URL(address);
        int port = url.getPort() == -1 ? 80 : url.getPort();
        Socket socket = new Socket(InetAddress.getByName(url.getHost()), port);
        OutputStream outStream = socket.getOutputStream();
        
        String requestmethod = "POST " + url.getPath() + " HTTP/1.1\r\n";
        outStream.write(requestmethod.getBytes());
        String accept = "Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*\r\n";
        outStream.write(accept.getBytes());
        String language = "Accept-Language: zh-CN\r\n";
        outStream.write(language.getBytes());
        String contenttype = "Content-Type: multipart/form-data; boundary=" + BOUNDARY + "\r\n";
        outStream.write(contenttype.getBytes());
        String contentlength = "Content-Length: " + dataLength + "\r\n";
        outStream.write(contentlength.getBytes());
        String alive = "Connection: Keep-Alive\r\n";
        outStream.write(alive.getBytes());
        String host = "Host: " + url.getHost() + ":" + port + "\r\n";
        outStream.write(host.getBytes());
        
        outStream.write("\r\n".getBytes());
        
        outStream.write(textEntity.toString().getBytes());
        
        StringBuffer strBuf = new StringBuffer();
        
        if(file != null) {
            StringBuilder fileEntity = new StringBuilder();
            fileEntity.append("--");
            fileEntity.append(BOUNDARY);
            fileEntity.append("\r\n");
            fileEntity.append("Content-Disposition: form-data;name=\"" + file.getParameterName() + "\";filename=\"" + file.getFilename() + "\"\r\n");
            fileEntity.append("Content-Type: " + file.getContentType() + "\r\n\r\n");
            outStream.write(fileEntity.toString().getBytes());
            FileInputStream fis = file.getInStream();
            if (fis != null) {
                fis.skip(file.getUploadedSize());
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = fis.read(buffer, 0, 1024)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                fis.close();
            } else {
                outStream.write(file.getData(), 0, file.getData().length);
            }
            outStream.write("\r\n".getBytes());
        }
        
        outStream.write(endline.getBytes());

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        if ( null != reader && reader.readLine().indexOf("200") == -1) {
            return null;
        } else {
            String readStr;
            
            String contLenLable = "Content-Length: ";
            int contLength = 0;
            boolean readBody = false;
            while (null != reader && ((readStr = reader.readLine()) != null)) {
                if(readStr.startsWith(contLenLable)) {
                    String lenStr = readStr.substring(contLenLable.length());
                    contLength = Integer.parseInt(lenStr);
                }
                if(readStr.equals("")) {
                    readBody = true;
                }
                if(contLength != 0 && readBody && readStr.length() == contLength) {
                    strBuf.append(readStr);
                }
            }
            
        }
        outStream.flush();
        outStream.close();
        reader.close();
        socket.close();
        return strBuf.toString();
    }
    
    
    private String getFileMd5(String fileName) {
        FileInputStream fis = null;
        StringBuffer strBuff = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(fileName);
            byte[] buffer = new byte[1024 * 1024];
            int length = -1;
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            byte[] bytes = md.digest();
            if (bytes == null) {
                return null;
            }
            String md5s = null;
            for (int i = 0; i < bytes.length; i++) {
                md5s = Integer.toHexString(bytes[i] & 0xff);
                if (md5s.length() == 1) {
                    strBuff.append("0");
                }
                strBuff.append(md5s);
            }
            return strBuff.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    /**
     * upload file class
     */
    private class FormFile {
        private byte[] data;
        private FileInputStream inStream;
        private File file;
        
        private String filname;
        
        private String parameterName;
        
//        private String contentType = "application/octet-stream";
        private String contentType = "multipart/form-data";

        private long uploadedSize;
        
        public FormFile(String filname, File file, String parameterName, String contentType, long uploadedSize) {
            this.filname = filname;
            this.parameterName = parameterName;
            this.file = file;
            try {
                this.inStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (contentType != null) {
                this.contentType = contentType;
            }
        }

        public long getUploadedSize() {
            return uploadedSize;
        }
        
        public File getFile() {
            return file;
        }

        public FileInputStream getInStream() {
            return inStream;
        }

        public byte[] getData() {
            return data;
        }

        public String getFilename() {
            return filname;
        }

        public void setFilename(String filname) {
            this.filname = filname;
        }

        public String getParameterName() {
            return parameterName;
        }

        public void setParameterName(String parameterName) {
            this.parameterName = parameterName;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

    }
}