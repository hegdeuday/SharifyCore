package com.hegdeuday.sharifycore;

/**
 *
 * @author hegdeuday
 */
public interface Constants {
    public static final int MAIN_PORT=28137;
    public static final int TRANSFER_PORT=28138;
    
    public static final int BUF_SIZE=1024;
    public static final int UPDATE_INTERVAL_MSEC=500;
    
    public static final class excpt{
        public static final int ERR_SERVER_BIND=0;
        public static final int ERR_CLIENT_VERIFY=1;
        public static final int ERR_LISTENER=2;
        public static final int ERR_FILE_SEND=3;
        public static final int ERR_WRITE_FILE=4;
        public static final int ERR_CLIENT_CONNECT=5;
        public static final int ERR_SERVER_VERIFY=6;
    }
    
    public static final class strings{
        public static final String SEPERATOR="####";
    }
}
