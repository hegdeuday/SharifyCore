package com.hegdeuday.sharifycore;

import com.hegdeuday.sharifycore.holders.ProgressProp;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

/**
 *
 * @author hegdeuday
 */
public class SocketHandler extends ProgressProp implements Constants{
    
    private BufferedReader buffRdr;
    private PrintWriter prntWrtr;
    private final Connector connector;
    private final Notifier notifier;
    private final ThreadHandler threadHandler;
    
    private DataInputStream dis;
    private DataOutputStream dos;
    
    public SocketHandler(Notifier notifier,ThreadHandler threadHandler){
        this.notifier=notifier;
        connector=new Connector();
        this.threadHandler=threadHandler;
    }
    
    public boolean verifyClient
        (java.net.Socket socket){
        try{
            buffRdr=new BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
            prntWrtr=new PrintWriter(socket.getOutputStream(),true);
            
            String authStr=buffRdr.readLine();
            
            prntWrtr.println("some Authentication string");
            
            java.net.Socket fSock=new java.net.Socket(socket.getInetAddress(),socket.getPort());
            dos=new DataOutputStream(fSock.getOutputStream());
            dis=new DataInputStream(fSock.getInputStream());
            createThreads();
            
        } catch (IOException ex) {
            notifier.onException(ex,excpt.ERR_CLIENT_VERIFY);
            return false;
        }
        return notifier.onVerified();
    }
    
    public boolean verifyServer(Socket socket){
        try(ServerSocket fServer=new ServerSocket(TRANSFER_PORT)){
            buffRdr=new java.io.BufferedReader(new InputStreamReader(socket.getInputStream()));
            prntWrtr=new java.io.PrintWriter(socket.getOutputStream(),true);
            
            prntWrtr.println("some authentication string");
            String authStr=buffRdr.readLine();
            
            
            Socket fSock;
            do{
                fSock=fServer.accept();
                if(fSock.getInetAddress().getHostAddress().equals(socket.getInetAddress().getHostAddress()))break;
                else fSock.close();
            }while(true);
            
            dos=new DataOutputStream(fSock.getOutputStream());
            dis=new DataInputStream(fSock.getInputStream());
            createThreads();
            
        }catch(IOException ex){
            notifier.onException(ex,excpt.ERR_CLIENT_VERIFY);
            return false;
        }
        return notifier.onVerified();
    }
    
    public void createThreads(){
        threadHandler.add(new ThreadHandler.InfiniteThread(), ThreadHandler.DATA_OUT, "data-out");
        threadHandler.add(new ThreadHandler.InfiniteThread(), ThreadHandler.DATA_IN, "data-in");
        threadHandler.add(new ThreadHandler.InfiniteThread(), ThreadHandler.TIMER, "timer");
    }

    public void beginListener(){        
        String str;
        try {
            while((str=buffRdr.readLine())!=null){
                process(str);
            }
        } catch(IOException ex) {
            notifier.onException(ex,excpt.ERR_LISTENER);
        }
    }
    
    private void process(String line){
        String[] arr=line.split(strings.SEPERATOR);
        switch(arr[0]){
            case "FILES":
                        int count=Integer.parseInt(arr[1]);long length=Long.parseLong(arr[2]);
                        File root=notifier.onIncomming(count,length );
                        threadHandler.runOnThread(ThreadHandler.DATA_IN,()->writeFiles(count,length,root));
                break;
            case "MSG":
                break;
            case "LINK":
                break;
            case "INTERN":connector.putString(java.util.Arrays.copyOfRange(arr, 1, arr.length-1));
                break;
            default:
                
        }
    }
    
    @SuppressWarnings("SleepWhileInLoop")
    private void writeFiles(int count,long lengthTotal,File root){
        fTotal=count;lenTotal=lengthTotal;curFCount=0;
        threadHandler.runOnThread(ThreadHandler.TIMER, () -> {
            while(curLen<lenTotal){
                try {
                    Thread.sleep(UPDATE_INTERVAL_MSEC);
                    calcSpeed(UPDATE_INTERVAL_MSEC);
                    notifier.onProgress(this);
                    Thread.sleep(UPDATE_INTERVAL_MSEC);
                    calcSpeed(UPDATE_INTERVAL_MSEC);
                    notifier.onProgress(this);
                    upTime++;
                }catch (InterruptedException ex) {}
            }
        });
        
        while(hasNextFile()){
             String[] str=recvMsg();
             curFile=new File(root.getAbsolutePath()+File.pathSeparator+str[0]);
             fLen=Long.parseLong(str[1]);
             sendMsg("INTERN","CONFIRM");
             try(FileOutputStream fos=new FileOutputStream(curFile)){
                curFCount++;
                byte[] buffer=new byte[BUF_SIZE];
                curFLen=0;
                while(curFLen<fLen){
                    int bufLen=dis.read(buffer, 0, buffer.length);
                    fos.write(buffer, 0, bufLen);
                    curFLen+=bufLen;
                    curLen+=bufLen;
                }
                fos.close();
             } catch (IOException ex){ 
                notifier.onException(ex, excpt.ERR_WRITE_FILE);
             } 
        }
    }
    
    private boolean hasNextFile(){
        return recvMsg()[0].equals("HAS_NEXT");
    }
    
    private void sendMsg(String ... str){
        threadHandler.runOnThread(ThreadHandler.MSG_SENDER, ()->{ prntWrtr.println(withSeperator(str));});
    }
    
    private String withSeperator(String[] str){
        StringBuilder sb=new StringBuilder();
        for(String st:str)
            sb.append(st).append(strings.SEPERATOR);

        return sb.toString();
    }
    
    private String[] recvMsg(){
        return connector.getString();
    }
    
    private long fsize(File dir){
        if(!dir.isDirectory())return dir.length();
        long length=0;
        for(File file:dir.listFiles()){length+=fsize(file);}
        return length;
    }
    
    private long fsize(File[] dir){
        long length=0;
        for(File file:dir){length+=fsize(file);}
        return length;
    }
    
    private int fcount(File dir){
        if(!dir.isDirectory())return 1;
        int count=0;
        for(File file:dir.listFiles()){count+=fcount(file);}
        return count;
    }
    
    private int fcount(File[] dir){
        int count=0;
        for(File file:dir){count+=fcount(file);}
        return count;
    }
    
    public void send(File[] files,File root){
        sendMsg("FILES",
                String.valueOf(fcount(files)),
                String.valueOf(fsize(files)));
        if(waitForConfirm())
            for(File file:files)
                sendFiles(file,root);
    }
    
    private void sendFiles(File dir,File root){
        if(!dir.isDirectory())
            threadHandler.runOnThread(ThreadHandler.DATA_OUT, ()->{
                sendMsg("INTERN","HAS_NEXT");
                String fname=root.toURI().relativize(dir.toURI()).getPath();
                sendMsg("INTERN",fname,String.valueOf(dir.length()));
                if(waitForConfirm()){
                    try(FileInputStream fis=new FileInputStream(dir)){
                        byte[] buffer=new byte[BUF_SIZE];
                        int len;
                        while((len=fis.read(buffer,0,buffer.length))>0){
                            dos.write(buffer, 0, len);
                        }
                        dos.flush();
                        fis.close();
                    } catch (IOException ex) {
                        notifier.onException(ex, excpt.ERR_FILE_SEND);
                    }
                }
            });
        else
            for(File file:dir.listFiles())
                sendFiles(file,root);
    }
    
    public boolean waitForConfirm(){
        return recvMsg()[0].equals("CONFIRM");
    }
    
    public void send(URL url){
        
    }
    
    public void send(String msg){
        
    }
    
    private class Connector{
        private boolean isPut=false;
        private String[] data;
        
        synchronized public void putString(String[] str){
            while(isPut){
                try {
                    wait();
                } catch (InterruptedException ex) {}
            }
            this.data=str;
            isPut=true;
            notifyAll();
        }
        
        synchronized public String[] getString(){
            while(!isPut){
                try {
                    wait();
                } catch (InterruptedException ex) {}
            }
            isPut=false;
            notifyAll();
            return data;
        }
    }
}
