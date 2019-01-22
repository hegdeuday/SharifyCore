package com.hegdeuday.sharifycore;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

/**
 *
 * @author hegdeuday
 */
public final class SharifyCore implements Constants{

    public static final int SERVER_MODE=0;
    public static final int CLIENT_MODE=1;
    
    private ThreadHandler threadHandler;

    private Notifier notifier;
    
    public SharifyCore(int mode,Notifier notifier){
        this.notifier=notifier;
        threadHandler=new ThreadHandler(Thread.currentThread());
        threadHandler.add(new ThreadHandler.InfiniteThread(()->{switch(mode){
            case SERVER_MODE:server();break;
            case CLIENT_MODE:client();break;
            default:
        }}), ThreadHandler.CLIENT_SERVER, "client-server");
    }

    public void server(){
        try(ServerSocket server=new ServerSocket(MAIN_PORT)){
            boolean verified=false;
            do{
                Socket socket=server.accept();
                final SocketHandler handler=new SocketHandler(notifier,threadHandler);
                verified=handler.verifyClient(socket);
                if(verified){ 
                    threadHandler.add(new ThreadHandler.InfiniteThread(()->handler.beginListener()), ThreadHandler.MSG_LISTENER, "msgListener");
                    threadHandler.add(new ThreadHandler.InfiniteThread(), ThreadHandler.MSG_SENDER, "msgSender");
                    addController(handler);
                }else{notifier.onThirdPartyAccess(socket.getInetAddress(),socket.getPort());socket.close();}
            }while(!verified || notifier.multipleClients());
        }catch(IOException ex) {notifier.onException(ex,excpt.ERR_SERVER_BIND);}
    }
    
    public void client(){
        try(Socket socket=new Socket(notifier.getServerAddress(), MAIN_PORT)){
            final SocketHandler handler=new SocketHandler(notifier,threadHandler);
            if(handler.verifyServer(socket)){
                threadHandler.add(new ThreadHandler.InfiniteThread(()->handler.beginListener()), ThreadHandler.MSG_LISTENER, "msgListener");
                threadHandler.add(new ThreadHandler.InfiniteThread(), ThreadHandler.MSG_SENDER, "msgSender");
                addController(handler);
            }
        }catch(IOException ex){notifier.onException(ex,excpt.ERR_CLIENT_CONNECT);}
    }
    
    private void addController(final SocketHandler handler){
        notifier.addController(new Controller(){
                        @Override
                        public void send(File[] files, File root) {
                            handler.send(files, root);
                        }

                        @Override
                        public void send(URL url) {
                            handler.send(url);
                        }

                        @Override
                        public void send(String msg) {
                            handler.send(msg);
                        }
                    });
    }
}
