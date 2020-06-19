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
public class ClientHandler extends Connector{
    private final ClientHandlerCallback clientHandlerCallback ;
    private final String clientInfo ;

    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallback clientHandlerCallback) throws IOException {
        this.clientHandlerCallback = clientHandlerCallback ;
        this.clientInfo =   socketChannel.getRemoteAddress().toString() ;
        System.out.println("新客户端连接: " + clientInfo);
        setup(socketChannel);
    }


    public void exit() {
        CloseUtils.close(this);
        System.out.println("客户端已退出: " + clientInfo);
    }

    public void exitBySelf(){
        exit();
        this.clientHandlerCallback.onSelfClosed(this);
    }


    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        exitBySelf();
    }

    @Override
    protected void onReceiveNewMessage(String str) {
        super.onReceiveNewMessage(str);
        clientHandlerCallback.onNewMessageArrived(this,str);
    }

    public interface ClientHandlerCallback {
        // 自身关闭通知
        void onSelfClosed(ClientHandler handler) ;
        // 收到消息通知
        void onNewMessageArrived(ClientHandler handler, String msg) ;
    }
}
