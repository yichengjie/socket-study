package com.yicj.study.server;

import com.yicj.study.server.handler.ClientHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ClassName: TCPServer
 * Description: TODO(描述)
 * Date: 2020/6/14 13:54
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class TCPServer implements ClientHandler.ClientHandlerCallback {
    private final int port ;
    private ClientListener listener;
    private List<ClientHandler> clientHandlerList = new ArrayList<>() ;
    private final ExecutorService forwardingThreadPoolExecutor;


    public TCPServer(int port) {
        this.port = port ;
        this.forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor() ;
    }

    public boolean start(){
        try {
            ClientListener listener = new ClientListener(port) ;
            this.listener = listener ;
            listener.start() ;
        }catch (IOException e){
            e.printStackTrace();
            return false ;
        }
        return true ;
    }

    public void stop(){
        if (listener != null){
            listener.exit() ;
        }
        synchronized (this){
            for (ClientHandler clientHandler: clientHandlerList){
                clientHandler.exit() ;
            }
            clientHandlerList.clear();
        }
        // 停止线程池
        forwardingThreadPoolExecutor.shutdownNow();
    }

    public synchronized void broadcast(String str) {
        for (ClientHandler clientHandler: clientHandlerList){
            clientHandler.send(str) ;
        }
    }

    @Override
    public synchronized void  onSelfClosed(ClientHandler handler) {
        clientHandlerList.remove(handler) ;
    }

    @Override
    public synchronized void onNewMessageArrived(ClientHandler handler, String msg) {
        //打印到屏幕，并回送数据长度
        System.out.println("Received-" + handler.getClientInfo() +": " + msg);
        this.forwardingThreadPoolExecutor.execute(()->{
            for (ClientHandler clientHandler: clientHandlerList){
                if (clientHandler.equals(handler)){
                    // 跳过自己
                    continue;
                }
                //对其他客户端发送消息
                clientHandler.send(msg);
            }
        });
    }

    private class ClientListener extends Thread{
        private ServerSocket server ;
        private boolean done = false ;

        private ClientListener(int port) throws IOException {
            server = new ServerSocket(port);
            System.out.println("服务器信息： " + server.getInetAddress() + " P: " + server.getLocalPort());
        }

        @Override
        public void run() {
            System.out.println("服务器准备就绪");
            // 等待客户端连接
            do {
                Socket client ;
                // 得到客户端
                try {
                    client = server.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                try {
                    //客户端构建异步线程
                    ClientHandler clientHandler = new ClientHandler(client,
                            TCPServer.this);
                    //读取数据并打印
                    clientHandler.readToPrint() ;
                    // 添加到列表中
                    synchronized (TCPServer.this){
                        clientHandlerList.add(clientHandler);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("客户端连接异常：" + e.getMessage());
                }
            }while (!done) ;
            System.out.println("服务器已关闭!");
        }

        void exit(){
            done = true ;
            try {
                server.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}
