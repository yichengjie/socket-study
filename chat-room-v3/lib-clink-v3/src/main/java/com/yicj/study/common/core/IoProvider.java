package com.yicj.study.common.core;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

//
public interface IoProvider extends Closeable {

    //通过IOProvider去观察SocketChannel里面的可读状态，
    // 当可读的时候通过HandleInputCallback进行回调
    boolean registerInput(SocketChannel channel, HandleInputCallback callback);

    //当前SocketChannel是输出的，通过HandleOutputCallback回调
    boolean registerOutput(SocketChannel channel, HandleOutputCallback callback);

    void unRegisterInput(SocketChannel channel);

    void unRegisterOutput(SocketChannel channel);


    abstract class HandleInputCallback implements Runnable {
        @Override
        public final void run() {
            canProviderInput();
        }

        protected abstract void canProviderInput();
    }

    abstract class HandleOutputCallback implements Runnable {
        private Object attach;

        @Override
        public final void run() {
            canProviderOutput(attach);
        }

        public final void setAttach(Object attach) {
            this.attach = attach;
        }

        public final <T> T getAttach(){
            return (T)attach ;
        }

        protected abstract void canProviderOutput(Object attach);
    }

}
