package com.yicj.study.server;

import com.yicj.study.common.utils.CloseUtils;
import com.yicj.study.server.handler.ClientHandler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

/**
 * ClassName: ServerAcceptor
 * Description: TODO(描述)
 * Date: 2020/6/30 21:45
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class ServerAcceptor extends Thread{
    private boolean done = false ;
    private final AcceptListener listener ;
    private final Selector selector ;
    private CountDownLatch latch = new CountDownLatch(1) ;

    public ServerAcceptor(AcceptListener listener) throws IOException {
        super("Server-Accept-Thread");
        // 当前有客户端连接成功的时候需要把回调，
        // 回调给当前的TCPServer
        this.listener = listener;
        this.selector = Selector.open();
    }

    /**
     * 等待启动
     * @return
     */
    boolean awaitRunning(){
        try {
            latch.await();
            return true ;
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public void run() {
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
                        System.out.println("成功建立客户端连接......");
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
                        // 非阻塞状态拿到客户端连接
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        listener.onNewSocketArrived(socketChannel);
//                        try {
//                            //客户端构建异步线程
//                            ClientHandler clientHandler = new ClientHandler(socketChannel,
//                                    TCPServer.this, cachePath);
//                            // 添加到列表中
//                            synchronized (TCPServer.this){
//                                clientHandlerList.add(clientHandler);
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            System.out.println("客户端连接异常：" + e.getMessage());
//                        }
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }while (!done) ;
        System.out.println("ServerAcceptor Finished !");
    }

    void exit(){
        done = true ;
        CloseUtils.close(selector);
    }

    public Selector getSelector() {
        return this.selector ;
    }

    interface AcceptListener{
       void onNewSocketArrived(SocketChannel channel) ;
    }
}
