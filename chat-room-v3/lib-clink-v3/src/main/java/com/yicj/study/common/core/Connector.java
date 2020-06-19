package com.yicj.study.common.core;


import com.yicj.study.common.box.StringReceivePackage;
import com.yicj.study.common.box.StringSendPacket;
import com.yicj.study.common.impl.SocketChannelAdapter;
import com.yicj.study.common.impl.async.AsyncReceiveDispathcher;
import com.yicj.study.common.impl.async.AsyncSendDispatcher;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;


/** 
 * @title: 代表一个连接，所有的操作都是基于一个连接
 *         发送和接收都封装到Connector中
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
    //发送调度
    private SendDispatcher sendDispatcher ;
    //接收调度
    private ReceiveDispatcher receiveDispatcher ;


    public void setup(SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;
        IoContext context = IoContext.get() ;
        SocketChannelAdapter adapter = new SocketChannelAdapter(channel,context.getIoProvider(),this) ;
        this.sender = adapter ;
        this.receiver = adapter ;
        this.sendDispatcher = new AsyncSendDispatcher(sender) ;
        this.receiveDispatcher = new AsyncReceiveDispathcher(receiver, receivePacketCallback) ;
        // 启动接收
        receiveDispatcher.start();
    }

    public void send(String msg){
        SendPacket packet = new StringSendPacket(msg) ;
        sendDispatcher.send(packet);
    }


    @Override
    public void close() throws IOException {
        receiveDispatcher.close();
        sendDispatcher.close();
        sender.close();
        receiver.close();
        channel.close();
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {

    }

    protected void onReceiveNewMessage(String str){

        System.out.println(key.toString() + " : " + str);
    }


    // 数据接收到的回调
    private ReceiveDispatcher.ReceivePacketCallback receivePacketCallback = new ReceiveDispatcher.ReceivePacketCallback() {
        @Override
        public void onReceivePacketComplete(ReceivePacket packet) {
            if (packet instanceof StringReceivePackage){
                String msg = ((StringReceivePackage) packet).string() ;
                onReceiveNewMessage(msg);
            }
        }
    } ;
}
