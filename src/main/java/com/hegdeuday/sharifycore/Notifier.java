package com.hegdeuday.sharifycore;

import com.hegdeuday.sharifycore.holders.ProgressProp;
import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hegdeuday
 */
public abstract class Notifier {
    
    private List<Controller> controllers;
    public Notifier(){
       controllers=new ArrayList<Controller>();
    }
    
    void addController(Controller controller){
        controllers.add(controller);
    }
    
    public List<Controller> getControllers(){
        return controllers;
    }
    
    public abstract void onConnected();
    public abstract void onException(Exception ex,int ERR_NO);
    public abstract boolean multipleClients();
    public abstract void onThirdPartyAccess(InetAddress addr,int port);
    public abstract boolean onVerified();
    public abstract File onIncomming(int fileCount,long length);
    public abstract void onProgress(ProgressProp prop);
    public abstract InetAddress getServerAddress();
}
