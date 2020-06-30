package com.yicj.study.common.core;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

//
public interface IoProvider extends Closeable {

    //通过IOProvider去观察SocketChannel里面的可读状态，
    // 当可读的时候通过HandleInputCallback进行回调
    boolean registerInput(SocketChannel channel, HandleProviderCallback callback);

    //当前SocketChannel是输出的，通过HandleOutputCallback回调
    boolean registerOutput(SocketChannel channel, HandleProviderCallback callback);

    void unRegisterInput(SocketChannel channel);

    void unRegisterOutput(SocketChannel channel);


    abstract class HandleProviderCallback implements Runnable {
        protected volatile IoArgs attach;

        @Override
        public final void run() {
            onProviderIo(attach);
        }

        protected abstract void onProviderIo(IoArgs args);

        public void checkAttachNull() {
            if (attach != null) {
                throw new IllegalArgumentException("Current attach is not empty!");
            }
        }
    }

}
