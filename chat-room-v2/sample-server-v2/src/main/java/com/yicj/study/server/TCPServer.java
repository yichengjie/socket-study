package com.yicj.study.server;

import com.yicj.study.common.utils.CloseUtils;
import com.yicj.study.server.handler.ClientHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
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
    private Selector selector;
    private ServerSocketChannel server ;



    public TCPServer(int port) {
        this.port = port ;
        this.forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor() ;
    }

    public boolean start(){
        try {
            selector = Selector.open() ;
            server = ServerSocketChannel.open();
            // 设置为非阻塞模式
            server.configureBlocking(false) ;
            // 绑定本地端口
            server.socket().bind(new InetSocketAddress(port));
            // 启动客户端监听
            System.out.println("服务器信息：" + server.getLocalAddress().toString());
            // 注册客户端连接到达监听
            server.register(selector, SelectionKey.OP_ACCEPT) ;

            ClientListener listener = new ClientListener() ;
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
        CloseUtils.close(server);
        CloseUtils.close(selector);
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
        private boolean done = false ;

        @Override
        public void run() {
            Selector selector = TCPServer.this.selector ;
            System.out.println("服务器准备就绪");
            // 等待客户端连接
            do {
                try {
                    if (selector.select() == 0){
                        if (done){
                            break;
                        }
                        continue;
                    }
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()){
                        if (done){
                            break;
                        }
                        SelectionKey key = iter.next();
                        iter.remove();
                        // 检查当前key的状态是否是我们关注的
                        // 客户端到达状态
                        if (key.isAcceptable()){
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
                            // 非阻塞状态拿到客户端连接
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            try {
                                //客户端构建异步线程
                                ClientHandler clientHandler = new ClientHandler(socketChannel,
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
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }while (!done) ;
            System.out.println("服务器已关闭!");
        }

        void exit(){
            done = true ;
            // 唤醒当前的阻塞
            selector.wakeup() ;
        }
    }

}
