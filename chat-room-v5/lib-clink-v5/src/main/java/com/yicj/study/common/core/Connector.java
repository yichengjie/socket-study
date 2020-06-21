package com.yicj.study.common.core;


import com.yicj.study.common.box.BytesReceivePacket;
import com.yicj.study.common.box.FileReceivePacket;
import com.yicj.study.common.box.StringReceivePacket;
import com.yicj.study.common.box.StringSendPacket;
import com.yicj.study.common.impl.SocketChannelAdapter;
import com.yicj.study.common.impl.async.AsyncReceiveDispatcher;
import com.yicj.study.common.impl.async.AsyncSendDispatcher;
import java.io.Closeable;
import java.io.File;
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
public abstract class Connector implements Closeable ,SocketChannelAdapter.OnChannelStatusChangedListener{
    protected UUID key = UUID.randomUUID();
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

    public void send(SendPacket packet) {
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

    protected void onReceivedPacket(ReceivePacket packet){
        System.out.println(key.toString()
                + ": [New Packet]-Type:" + packet.type() +", Length : " + packet.length);
    }

    protected abstract File createNewReceiveFile() ;

    private ReceiveDispatcher.ReceivePacketCallback receivePacketCallback = new ReceiveDispatcher.ReceivePacketCallback() {
        @Override
        public ReceivePacket<?, ?> onArrivedNewPacket(byte type, long length) {
            switch (type){
                case Packet.TYPE_MEMORY_BYTES:
                case Packet.TYPE_STREAM_DIRECT:
                    return new BytesReceivePacket(length) ;
                case Packet.TYPE_MEMORY_STRING:
                    return new StringReceivePacket(length) ;
                case Packet.TYPE_STREAM_FILE:
                    return new FileReceivePacket(length, createNewReceiveFile()) ;
                default:
                    throw new UnsupportedOperationException("Unsupported packet type: " + type) ;
            }
        }

        @Override
        public void onReceivePacketCompleted(ReceivePacket packet) {
            onReceivedPacket(packet);
        }
    } ;


}
