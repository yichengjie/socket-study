package com.yicj.study.common.core;


import com.yicj.study.common.impl.SocketChannelAdapter;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;


/** 
 * @title: 代表一个连接，所有的操作都是基于一个连接
 * @description: TODO(描述)
 * @params
 * @author yicj
 * @date 2020/6/16 20:00
 * @return
 **/  
public class Connector implements Closeable ,SocketChannelAdapter.OnChannelStatusChangedListener{
    //代表这个连接的唯一性
    private UUID key = UUID.randomUUID();
    // 连接依赖于SocketChannel
    private SocketChannel channel;
    // 发送者
    private Sender sender;
    // 接收者
    private Receiver receiver;

    private IoArgs.IoArgsEventListener echoReceiveListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {

        }

        @Override
        public void onCompleted(IoArgs args) {
            //打印
            onReceiveNewMessage(args.bufferString()) ;
            //读取下一条数据
            readNextMessage();
        }
    } ;

    public void setup(SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;
        IoContext context = IoContext.get() ;
        SocketChannelAdapter adapter = new SocketChannelAdapter(channel,context.getIoProvider(),this) ;
        this.sender = adapter ;
        this.receiver = adapter ;
        readNextMessage() ;
    }


    private void readNextMessage(){
        if (receiver!=null){
            try {
                receiver.receiveAsync(echoReceiveListener) ;
            } catch (IOException e) {
                System.out.println("开始接收数据异常 ：" + e.getMessage());
            }
        }
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void onChannelClosed(SocketChannel channel) {

    }

    protected void onReceiveNewMessage(String str){

        System.out.println(key.toString() + " : " + str);
    }
}
