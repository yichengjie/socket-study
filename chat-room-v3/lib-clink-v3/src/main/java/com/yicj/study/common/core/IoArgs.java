package com.yicj.study.common.core;

import java.io.EOFException;
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
    private int limit = 5;
    private byte[] byteBuffer = new byte[5];
    private ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);

    /**
     * 从bytes中读取数据
     */
    public int readFrom(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.put(bytes, offset, size);
        return size;
    }

    /**
     * 写入数据到bytes中
     * @param bytes  装数据的buffer
     * @param offset 从
     * @return
     */
    public int writeTo(byte[] bytes, int offset) {
        // byte.length - offset为数组中剩余空间数
        // buffer.remaining() 为缓存区中还剩余的字节数
        // 取【字节数组剩余空间长度】与【缓存区剩余字节数】中较小的作为待读取字节数
        // buffer中可能包含下一个数据包的数组
        int size = Math.min(bytes.length - offset, buffer.remaining());
        // 从缓冲区中读取字节到bytes数组中
        /**
         * 参数：
         *   dst - 向其中写入字节的数组
         *   offset - 要写入的第一个字节在数组中的偏移量；必须为非负且不大于 dst.length
         *   length - 要写入到给定数组中的字节的最大数量；必须为非负且不大于 dst.length - offset
         */
        buffer.get(bytes, offset, size);
        return size;
    }

    /**
     * 从SocketChannel读取数据
     */
    public int readFrom(SocketChannel channel) throws IOException {
        startWriting();
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.read(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
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
        buffer.putInt(total);
    }

    public int readLength() {
        return buffer.getInt();
    }

    public int capacity() {
        return buffer.capacity();
    }


    public interface IoArgsEventListener {
        void onStarted(IoArgs args);

        void onCompleted(IoArgs args);
    }
}
