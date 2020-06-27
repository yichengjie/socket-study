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
     * 从bytes数组进行消费
     * @param bytes
     * @param offset
     * @param count
     * @return
     */
    public int readFrom(byte[] bytes, int offset, int count) {
        int size = Math.min(count, buffer.remaining()) ;
        if (size <= 0){
            return 0 ;
        }
        buffer.put(bytes,offset,size) ;
        return size ;
    }

    /**
     * 写入数据到bytes中
     * @param bytes
     * @param offset
     * @return
     */
    public int writeTo(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, buffer.remaining()) ;
        if (size <= 0){
            return 0 ;
        }
        buffer.get(bytes, offset, size) ;
        return size ;
    }

    /**
     * 从bytes中读取数据
     */
    public int readFrom(ReadableByteChannel channel) throws IOException {
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.read(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
        // 数据读取完成将buffer重置到读取模式
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
        this.limit = Math.min(limit, buffer.capacity());
    }

    public int readLength() {
        return buffer.getInt();
    }

    public int capacity() {
        return buffer.capacity();
    }

    public boolean remained() {
        return buffer.remaining() > 0 ;
    }


    /**
     *
     * @param size 想要填充数据的长度
     * @return 真实填充数据的长度
     */
    public int fillEmpty(int size) {
        int fillSize = Math.min(size, buffer.remaining()) ;
        buffer.position(buffer.position() + fillSize) ;
        return fillSize ;
    }

    /**
     * 清空一部分数据(不读取一部分数据的操作)
     * 与fillEmpty的作用完全一样
     * @param size
     * @return
     */
    public int setEmpty(int size) {
        int emptySize = Math.min(size, buffer.remaining()) ;
        buffer.position(buffer.position() + emptySize) ;
        return emptySize ;
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
