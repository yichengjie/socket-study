package com.yicj.study.server;

import com.yicj.study.server.handler.ClientHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: TCPServer
 * Description: TODO(描述)
 * Date: 2020/6/14 13:54
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class TCPServer {
    private final int port ;
    private ClientListener mListener ;
    private List<ClientHandler> clientHandlerList = new ArrayList<>() ;

    public TCPServer(int port) {
        this.port = port ;
    }

    public boolean start(){
        try {
            ClientListener listener = new ClientListener(port) ;
            mListener = listener ;
            listener.start() ;
        }catch (IOException e){
            e.printStackTrace();
            return false ;
        }
        return true ;
    }

    public void stop(){
        if (mListener != null){
            mListener.exit() ;
        }
        for (ClientHandler clientHandler: clientHandlerList){
            clientHandler.exit() ;
        }
        clientHandlerList.clear();
    }

    public void broadcast(String str) {
        for (ClientHandler clientHandler: clientHandlerList){
            clientHandler.send(str) ;
        }
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
                            handler -> clientHandlerList.remove(handler));
                    //读取数据并打印
                    clientHandler.readToPrint() ;
                    // 添加到列表中
                    clientHandlerList.add(clientHandler);
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
