/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hegdeuday.sharifycore;

import com.hegdeuday.sharifycore.holders.ProgressProp;
import java.io.File;
import java.net.InetAddress;

/**
 *
 * @author hegdeuday
 */
public interface Notifier {
    public void onConnected();
    public void onException(Exception ex,int ERR_NO);
    public boolean multipleClients();
    public void onThirdPartyAccess(InetAddress addr,int port);
    public boolean onVerified();
    public File onIncomming(int fileCount,long length);
    public void onProgress(ProgressProp prop);
    public InetAddress getServerAddress();
}
