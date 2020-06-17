package com.yicj.study.common.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @title: IO 输出和输入的一个类，主要是完成对ByteBuffer的封装
 * @description: TODO(描述)
 * @params
 * @author yicj
 * @date 2020/6/16 20:03
 * @return
 **/
public class IoArgs {
    private byte[] byteBuffer = new byte[256];
    private ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);

    //读并不是说把数据读到socketChannel，而是从socketChannel中读取数据到ByteBuffer
    public int read(SocketChannel channel) throws IOException {
        buffer.clear();
        return channel.read(buffer);
    }

    // 将ByteBuffer写入到channel
    public int write(SocketChannel channel) throws IOException {
        return channel.write(buffer);
    }

    // 将buffer中的数据读到String中
    public String bufferString() {
        // 丢弃换行符
        return new String(byteBuffer, 0, buffer.position() - 1);
    }

    public interface IoArgsEventListener {
        void onStarted(IoArgs args);

        void onCompleted(IoArgs args);
    }
}
