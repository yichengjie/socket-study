package com.yicj.study.common.core;

import java.io.Closeable;

/**
 * 接收数据的调度封装
 * 把一份或多份IoArgs组合成一份Packet
 */
public interface ReceiveDispatcher extends Closeable {

    void start() ;

    void stop() ;

    // 接收完成时的回调
    interface ReceivePacketCallback{
        ReceivePacket<?,?> onArrivedNewPacket(byte type, long length) ;
        void onReceivePacketCompleted(ReceivePacket packet) ;
    }
}
