package com.yicj.study.common.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.Executors;

/**
 * @title: IO 输出和输入的一个类，主要是完成对ByteBuffer的封装
 * @description: TODO(描述)
 * @params
 * @author yicj
 * @date 2020/6/16 20:03
 * @return
 **/
public class IoArgs {
    private int limit = 256;
    private ByteBuffer buffer = ByteBuffer.allocate(256) ;

    /**
     * 从bytes中读取数据
     */
    public int readFrom(ReadableByteChannel channel) throws IOException {
        startWriting();
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.read(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
        // 数据读取完成将buffer重置到读取模式
        finishWriting();
        return bytesProduced;
    }

    /**
     * 写入数据到bytes中
     * @return
     */
    public int writeTo(WritableByteChannel channel) throws IOException {
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.write(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
        return bytesProduced;
    }

    /**
     * 从SocketChannel读取数据
     */
    public int readFrom(SocketChannel channel) throws IOException {
        // 1. 将buffer重置到写模式
        // 2. 从channel读取数据前将buffer清空并设置好buffer的limit以便读取数据
        startWriting();
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.read(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
        // 数据读取完成将buffer重置到读取模式
        finishWriting();
        return bytesProduced;
    }

    /**
     * 写数据到SocketChannel
     */
    public int writeTo(SocketChannel channel) throws IOException {
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.write(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
        return bytesProduced;
    }

    /**
     * 开始写入数据到IoArgs
     */
    public void startWriting() {
        buffer.clear();
        // 定义容纳区间
        buffer.limit(limit);
    }

    /**
     * 写完数据后调用
     */
    public void finishWriting() {
        buffer.flip();
    }

    /**
     * 设置单次写操作的容纳区间
     *
     * @param limit 区间大小ø
     */
    public void limit(int limit) {
        this.limit = limit;
    }

    public void writeLength(int total) {
        startWriting();
        buffer.putInt(total);
        finishWriting();
    }

    public int readLength() {
        return buffer.getInt();
    }

    public int capacity() {
        return buffer.capacity();
    }


    /**
     * IoArgs 提供者、处理者；数据的生产或消费者
     */
    public interface IoArgsEventProcessor {
        /**
         * 提供一份可消费的IoArgs
         * @return
         */
        IoArgs provideIoArgs() ;

        /**
         * 消费失败时回调
         * @param args
         * @param e
         */
        void onConsumeFailed(IoArgs args, Exception e) ;

        /**
         * 消费成功时回调
         * @param args
         */
        void onConsumeCompleted(IoArgs args) ;
    }
}
