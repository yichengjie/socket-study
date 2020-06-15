package com.yicj.study.server.handler;

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
    private final SocketChannel socketChannel ;
    private final ClientReadHandler readHandler ;
    private final ClientWriteHandler writeHandler ;
    private final ClientHandlerCallback clientHandlerCallback ;
    private final String clientInfo ;

    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallback clientHandlerCallback) throws IOException {
        this.socketChannel = socketChannel ;
        // 设置非阻塞模式
        socketChannel.configureBlocking(false) ;
        Selector readSelector = Selector.open();
        socketChannel.register(readSelector, SelectionKey.OP_READ) ;
        this.readHandler = new ClientReadHandler(readSelector) ;

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
        readHandler.exit();
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

    // 启动读取线程
    public void readToPrint() {
        readHandler.start();
    }

    public interface ClientHandlerCallback {
        // 自身关闭通知
        void onSelfClosed(ClientHandler handler) ;
        // 收到消息通知
        void onNewMessageArrived(ClientHandler handler, String msg) ;
    }

    class ClientReadHandler extends Thread{
        private boolean done =false ;
        private final Selector selector ;
        private final ByteBuffer byteBuffer ;

        public ClientReadHandler(Selector selector) {
            this.selector = selector;
            this.byteBuffer = ByteBuffer.allocate(256) ;
        }
        @Override
        public void run() {
            try {
                do {
                    if (selector.select() == 0){
                        if (done){
                            break;
                        }
                        continue;
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()){
                        if (done){
                            break;
                        }
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isReadable()){
                            SocketChannel client = (SocketChannel)key.channel();
                            //清空操作
                            byteBuffer.clear() ;
                            // 读取
                            int read = client.read(byteBuffer);
                            if (read > 0){
                                // 丢弃换行符
                                String str = new String(byteBuffer.array(), 0, read -1) ;
                                clientHandlerCallback.onNewMessageArrived(ClientHandler.this, str);
                            }else {
                                System.out.println("客户端已无法读取数据！");
                                // 退出当前客户端
                                ClientHandler.this.exitBySelf();
                                break;
                            }
                        }
                    }
                }while (!done) ;
            }catch (Exception e){
                if (!done){
                    System.out.println("连接异常断开!");
                    ClientHandler.this.exitBySelf();
                }
            }finally {
                // 连接关闭
                CloseUtils.close(selector);
            }

        }
        void exit(){
            done = true ;
            // 调用退出时可能处于阻塞状态，需要唤醒
            selector.wakeup() ;
            CloseUtils.close(selector);
        }
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
