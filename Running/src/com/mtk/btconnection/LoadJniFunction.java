package com.mtk.btconnection;

public class LoadJniFunction {
    private static final String LIB_NAME = "Command";    
    public static final int CMD_1 = 1;
    public static final int CMD_2 = 2;
    public static final int CMD_3 = 3;
    public static final int CMD_4 = 4;
    public static final int CMD_5 = 5;
    public static final int CMD_6 = 6;
    public static final int CMD_7 = 7;
    public static final int CMD_8 = 8;
    public static final int CMD_9 = 9;
    // Load native library
    static {  
        System.loadLibrary(LIB_NAME);
    }
    
    public native byte[] getDataCmdFromJni(int len, String arg);

    public native int getCmdTypeFromJni(byte[] command,int commandlenth);
    
    public native int getDataLenthFromJni(byte[] command,int commandlenth);
    
    /**
     * Call JNI function to get data command.
     * 
     * @param len data length
     * @return the command data
     */
    public byte[] getDataCmd(int len, String arg) {
        return getDataCmdFromJni(len,arg);
    }
    
    /**
     * Call JNI function to get operation command.
     * 
     * @param commandlenth data length
     * @return the command data
     */
    public int getCmdType(byte[] command,int commandlenth) {
        return getCmdTypeFromJni(command,commandlenth);
    }
    
    public int getDataLenth(byte[] command,int commandlenth) {
        return getDataLenthFromJni(command,commandlenth);
    }
}
