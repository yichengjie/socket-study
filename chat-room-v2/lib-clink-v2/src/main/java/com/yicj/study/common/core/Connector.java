package com.yicj.study.common.core;


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
public class Connector {
    //代表这个连接的唯一性
    private UUID key = UUID.randomUUID();
    // 连接依赖于SocketChannel
    private SocketChannel channel;
    // 发送者
    private Sender sender;
    // 接收者
    private Receiver receiver;

    public void setup(SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;
    }
}
