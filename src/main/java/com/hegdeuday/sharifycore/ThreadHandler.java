package com.hegdeuday.sharifycore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**    
 *
 * @author hegdeuday
 */
final public class ThreadHandler {
    
    public static final int MAIN_THREAD=0;
    public static final int CLIENT_SERVER=1;
    public static final int MSG_LISTENER=2;
    public static final int MSG_SENDER=3;
    public static final int DATA_OUT=4;
    public static final int DATA_IN=5;
    public static final int TIMER=6;
    
    private List<InfiniteThread> threadList;
    public ThreadHandler(Thread parentThread){
        threadList=new ArrayList<InfiniteThread>();
        add(new InfiniteThread(),0,"main");
        runOnMainThread(()->{try {
                parentThread.join();
            }catch (InterruptedException ex) {}
        });
    }
    
    public void add(InfiniteThread thread,int position,String name){
        if(name!=null)thread.setName(name);
        threadList.add(position, thread);
    }
    
    public InfiniteThread getByPos(int position){
        return threadList.get(position);
    }
    
    public InfiniteThread getByName(String name){
        for(InfiniteThread thread:threadList){
            if(thread.getName()!=null && thread.getName().equals(name))
                return thread;
        }
        return null;
    }
    
    public void runOnThread(int pos,Runnable runnable){
        getByPos(pos).run(runnable);
    }
    
    public void runOnFreeThread(Runnable run,int ... except){
        threadList.stream().filter((thread) -> (thread.isFree() && Arrays.binarySearch(except,threadList.indexOf(thread))<0)).forEachOrdered((thread) -> {
            thread.run(run);
        });
    }
    
    public void runOnMainThread(Runnable run){
        runOnThread(0,run);
    }
    
    public void finishAll(){
        for(InfiniteThread thread:threadList)
            thread.finish();
    }
    
    public static final class InfiniteThread extends Thread{
        private boolean flag=true,isPaused=false,isFree=true;
        private List<Runnable> runner=new ArrayList<Runnable>();
        
        public InfiniteThread(){
            super();start();
        }
        
        public InfiniteThread(Runnable run){
            super();start();run(run);
        }
        
        @Override
        public void run(){
            while(flag){
                if(runner.size()>0 && !isPaused){
                    isFree=false;runner.get(0).run();runner.remove(0);
                }else try {
                    isFree=true;
                    Thread.sleep(100);
                } catch (InterruptedException ex) {}
            }
        }
        public boolean isFree(){
            return isFree;
        }
        synchronized public void run(Runnable run){
            runner.add(run);
        }
        public void finish(){
            this.flag=false;
        }
        
        public void pauseThread(){
            this.isPaused=true;
        }
        
        public void resumeThread(){
            this.isPaused=false;
        }
    }
}
