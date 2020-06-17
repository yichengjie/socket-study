package com.yicj.study.server.handler;

import com.yicj.study.common.core.Connector;
import com.yicj.study.common.utils.CloseUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 客户端消息处理
 * ClassName: ClientHandler
 * Description: TODO(描述)
 * Date: 2020/6/14 16:31
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class ClientHandler {
    private final Connector connector ;
    private final SocketChannel socketChannel ;
    private final ClientWriteHandler writeHandler ;
    private final ClientHandlerCallback clientHandlerCallback ;
    private final String clientInfo ;

    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallback clientHandlerCallback) throws IOException {
        this.socketChannel = socketChannel ;
        // 设置非阻塞模式
        this.connector = new Connector(){
            @Override
            public void onChannelClosed(SocketChannel channel) {
                super.onChannelClosed(channel);
                exitBySelf();
            }

            @Override
            protected void onReceiveNewMessage(String str) {
                super.onReceiveNewMessage(str);
                clientHandlerCallback.onNewMessageArrived(ClientHandler.this,str);
            }
        } ;
        this.connector.setup(socketChannel);


        Selector writeSelector = Selector.open() ;
        socketChannel.register(writeSelector,SelectionKey.OP_WRITE) ;
        this.writeHandler = new ClientWriteHandler(writeSelector) ;

        this.clientHandlerCallback = clientHandlerCallback ;
        this.clientInfo =   socketChannel.getRemoteAddress().toString() ;
        System.out.println("新客户端连接: " + clientInfo);
    }

    public String getClientInfo(){
        return clientInfo ;
    }

    public void exit() {
        CloseUtils.close(connector);
        writeHandler.exit() ;
        CloseUtils.close(socketChannel);
        System.out.println("客户端已退出: " + clientInfo);
    }

    public void exitBySelf(){
        exit();
        this.clientHandlerCallback.onSelfClosed(this);
    }

    public void send(String str) {
        this.writeHandler.send(str) ;
    }


    public interface ClientHandlerCallback {
        // 自身关闭通知
        void onSelfClosed(ClientHandler handler) ;
        // 收到消息通知
        void onNewMessageArrived(ClientHandler handler, String msg) ;
    }

    class ClientWriteHandler{
        private boolean done = false ;
        private final Selector selector ;
        private final ByteBuffer byteBuffer ;
        private final ExecutorService executorService ;

        ClientWriteHandler(Selector selector) {
            this.selector = selector ;
            byteBuffer = ByteBuffer.allocate(256) ;
            this.executorService = Executors.newSingleThreadExecutor() ;
        }

        void exit(){
            done = true ;
            CloseUtils.close(selector);
            executorService.shutdownNow() ;
        }

        void send(String str) {
            // 如果客户端已经下线
            if (done){
                return;
            }
            executorService.execute(new WriteRunnable(str));
        }

        class WriteRunnable implements Runnable{
            private final String msg ;

            WriteRunnable(String msg) {
                this.msg = msg +'\n';
            }
            @Override
            public void run() {
                if (ClientWriteHandler.this.done){
                    return;
                }
                byteBuffer.clear() ;
                byteBuffer.put(msg.getBytes()) ;
                // 反转操作，重点
                byteBuffer.flip() ;
                while (!done && byteBuffer.hasRemaining()){
                    try {
                        int len = socketChannel.write(byteBuffer);
                        if (len < 0){
                            System.out.println("客户端已无法发送数据");
                            ClientHandler.this.exitBySelf();
                            break;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
