/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hegdeuday.sharifycore.holders;

import java.io.File;

/**
 *
 * @author hegdeuday
 */
public class ProgressProp {
    protected int fTotal=0,curFCount=0;
    protected long lenTotal=0,curLen=0,lastLen=0,fLen=0,curFLen=0,speed=0,upTime=0;
    protected File curFile;
    
    public int getTotalFileCount(){
        return fTotal;
    }
    public int getCurrentFileCount(){
        return curFCount;
    }
    public long getUpTime(){
        return upTime;
    }
    public String getUptimeString(){
        return formTimeString(getUpTime());
    }
    
    public long getRemainingTime(){
        return (lenTotal-curLen)/getSpeed();
    }
    
    public String getRemainingTimeString(){
        return formTimeString(getRemainingTime());
    }
    
    public long getTotalLength(){
        return lenTotal;
    }
    public long getCurrentLength(){
        return curLen;
    }
    public long getTotalFileLength(){
        return fLen;
    }
    public long getCurrentFileLength(){
        return curFLen;
    }
    public File getCurrentFile( ){
        return curFile;
    }
    
    
    public long getSpeed(){
        return speed;
    }
    
    public String getSpeedString(){
        return byteToString(getSpeed())+"ps";
    }
    
    protected void calcSpeed(int msecLaps){
        this.speed=(curLen-lastLen)*1000/msecLaps;
        lastLen=curLen;
    }
    
    private String formTimeString(long sec){
        StringBuilder builder=new StringBuilder("");
        int scnd=(int) sec%60,min=0,hr=0,days=0,years=0;
        if(sec>=60){
            min=(int) ((sec-scnd)/60)%60;
            if(sec>=3600){
                hr=(int) ((((sec-scnd)/60)-min)/60)*24;
                if(sec>=3600*24){
                    days=(int) ((((((sec-scnd)/60)-min)/60)-hr)/24)%365;
                    if(sec>=3600*24*365)
                        years=(int) (((((((sec-scnd)/60)-min)/60)-hr)/24)-days)/365;
                }
            }
        }
        if(years!=0)builder.append(years).append(" years ");
        if(days!=0)builder.append(days).append(" days ");
        if(hr!=0)builder.append(hr).append(" hrs ");
        if(min!=0)builder.append(min).append(" mins ");
        builder.append(scnd).append(" secs");
        
        return builder.toString();
    }
    
    private String byteToString(long total) {
        double bytes=(double)total;
        int count=0;
	String unit="B";
	while(bytes >= 1000){
            bytes=bytes/1024;
            count++;
	}
	switch(count){
            case 0:unit="B";
		break;
            case 1:unit="KB";
		break;
            case 2:unit="MB";
		break;
            case 3:unit="GB";
		break;
            case 4:unit="TB";
		break;
            case 5:unit="EB";
	}
	return Math.round(bytes*100.0)/100.0+" "+unit;
    }
    
}
