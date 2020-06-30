package com.yicj.study.common.impl.async;

import com.yicj.study.common.core.IoArgs;
import com.yicj.study.common.core.SendDispatcher;
import com.yicj.study.common.core.SendPacket;
import com.yicj.study.common.core.Sender;
import com.yicj.study.common.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ClassName: AsyncSendDispatcher
 * Description: TODO(描述)
 * Date: 2020/6/17 22:08
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class AsyncSendDispatcher implements SendDispatcher, IoArgs.IoArgsEventProcessor, AsyncPacketReader.PacketProvider {

    private final Sender sender;
    private final Queue<SendPacket> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isSending = new AtomicBoolean();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final AsyncPacketReader reader = new AsyncPacketReader(this) ;



    public AsyncSendDispatcher(Sender sender) {
        this.sender = sender;
        sender.setSendListener(this);
    }

    @Override
    public void send(SendPacket packet) {
        queue.offer(packet);
        requestSend() ;
    }

    @Override
    public void cancel(SendPacket packet) {
        boolean ret = queue.remove(packet);
        if(ret){
            packet.cancel();
            return;
        }
        reader.cancel(packet) ;
    }

    @Override
    public SendPacket takePacket() {
        SendPacket packet= queue.poll();
        if (packet == null) {
            // 队列为空，取消发送状态
           return null;
        }
        if (packet.isCanceled()){
            return takePacket();
        }
        return packet;
    }

    /**
     * 完成packet发送
     * @param packet
     * @param isSuccess
     */
    @Override
    public void completedPacket(SendPacket packet, boolean isSuccess) {
        CloseUtils.close(packet);
    }


    /**
     * 请求网络进行数据发送
     */
    private void requestSend() {
        synchronized (isSending){
            if (isSending.get() || isClosed.get()){
                return;
            }
            // 返回true代表当前有数据需要发送
            if (reader.requestTaskPacket()){
                try {
                    boolean isSuccess = sender.postSendAsync();
                    if (isSuccess){
                        isSending.set(true);
                    }
                } catch (IOException e) {
                    closeAndNotify();
                }
            }
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            reader.close() ;
            // 清空队列
            queue.clear();
            synchronized (isSending){
                isSending.set(false);
            }
        }
    }


    @Override
    public IoArgs provideIoArgs() {
        return isClosed.get() ? null: reader.fillDate() ;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {
        e.printStackTrace();
        synchronized (isSending){
            isSending.set(false);
        }
        // 继续请求发送当前的数据
        requestSend();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        // 继续发送当前包
        synchronized (isSending){
            isSending.set(false);
        }
        // 继续请求发送当前的数据
        requestSend();
    }
}
