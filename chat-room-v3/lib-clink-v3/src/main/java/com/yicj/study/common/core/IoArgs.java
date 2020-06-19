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
    private int limit = 256 ;
    private byte[] byteBuffer = new byte[256];
    private ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);

    /**
     * 从bytes中读取数据
     * @param bytes
     * @param offset
     * @return
     */
    public int readFrom(byte [] bytes, int offset){
        int size = Math.min(bytes.length- offset, buffer.remaining()) ;
        buffer.put(bytes,offset,size) ;
        return size ;
    }

    /**
     * 写入数据到bytes中
     * @param bytes
     * @param offset
     * @return
     */
    public int writeTo(byte[] bytes, int offset){
        int size = Math.min(bytes.length- offset, buffer.remaining()) ;
        buffer.get(bytes,offset,size) ;
        return size ;
    }

    /**
     * 从SocketChannel中读取数据
     * @param channel
     * @return
     * @throws IOException
     */
    //读并不是说把数据读到socketChannel，而是从socketChannel中读取数据到ByteBuffer
    public int readFrom(SocketChannel channel) throws IOException {
        startWriting();
        int bytesProduced = 0 ;
        while (buffer.hasRemaining()){
            int len = channel.read(buffer) ;
            if (len < 0){
                throw new EOFException() ;
            }
            bytesProduced += len ;
        }
        finishWriting();
        return channel.read(buffer);
    }

    /**
     * 写数据到SocketChannel
     * @param channel
     * @return
     * @throws IOException
     */
    // 将ByteBuffer写入到channel
    public int writeTo(SocketChannel channel) throws IOException {
        int bytesProduced = 0 ;
        while (buffer.hasRemaining()){
            int len = channel.write(buffer) ;
            if (len < 0){
                throw new EOFException() ;
            }
            bytesProduced += len ;
        }
        return bytesProduced;
    }

    /**
     * 开始写数据到IoArgs
     */
    public void startWriting(){
        buffer.clear();
        // 定义容纳区间
        buffer.limit() ;
    }

    /**
     * 写完数据后调用
     */
    public void finishWriting(){
        buffer.flip();
    }

    /**
     * 设置单次写操作的容纳区间
     * @param limit 区间大小
     */
    public void limit(int limit){
        this.limit = limit ;
    }

    public void writeLength(int total) {
        buffer.putInt(total) ;
    }

    public int readLength(){
        return buffer.getInt() ;
    }

    /**
     * 容量
     * @return
     */
    public int capacity() {
        return buffer.capacity() ;
    }


    public interface IoArgsEventListener {
        void onStarted(IoArgs args);

        void onCompleted(IoArgs args);
    }
}
