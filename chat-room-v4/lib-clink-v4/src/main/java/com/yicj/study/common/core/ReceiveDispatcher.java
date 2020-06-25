package com.yicj.study.common.core;

import java.io.Closeable;

/**
 * 接收数据的调度封装
 * 把一份或多份IoArgs组合成一份Packet
 */
public interface ReceiveDispatcher extends Closeable {

    void start() ;
    // 接收完成时的回调
    interface ReceivePacketCallback{
        void onReceivePacketCompleted(ReceivePacket packet) ;
    }
}
