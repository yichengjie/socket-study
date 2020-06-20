package com.yicj.study.common.core;


import com.yicj.study.common.box.StringReceivePacket;
import com.yicj.study.common.box.StringSendPacket;
import com.yicj.study.common.impl.SocketChannelAdapter;
import com.yicj.study.common.impl.async.AsyncReceiveDispatcher;
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
    private UUID key = UUID.randomUUID();
    private SocketChannel channel;
    private Sender sender;
    private Receiver receiver;
    private SendDispatcher sendDispatcher;
    private ReceiveDispatcher receiveDispatcher;


    public void setup(SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;

        IoContext context = IoContext.get();
        SocketChannelAdapter adapter = new SocketChannelAdapter(channel, context.getIoProvider(), this);

        this.sender = adapter;
        this.receiver = adapter;

        sendDispatcher = new AsyncSendDispatcher(sender);
        receiveDispatcher = new AsyncReceiveDispatcher(receiver, receivePacketCallback);

        // 启动接收
        receiveDispatcher.start();
    }


    public void send(String msg) {
        SendPacket packet = new StringSendPacket(msg);
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

    protected void onReceiveNewMessage(String str) {
        System.out.println(key.toString() + ":" + str);
    }


    private ReceiveDispatcher.ReceivePacketCallback receivePacketCallback = new ReceiveDispatcher.ReceivePacketCallback() {

        @Override
        public void onReceivePacketComplete(ReceivePacket packet) {
            if (packet instanceof StringReceivePacket) {
                String msg = ((StringReceivePacket) packet).string();
                onReceiveNewMessage(msg);
            }
        }
    };
}
