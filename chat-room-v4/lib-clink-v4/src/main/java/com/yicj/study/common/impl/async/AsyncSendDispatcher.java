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
public class AsyncSendDispatcher implements SendDispatcher, IoArgs.IoArgsEventProcessor {

    private final Sender sender;
    private final Queue<SendPacket> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isSending = new AtomicBoolean();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private IoArgs ioArgs = new IoArgs();
    private SendPacket<?> packetTemp;

    // 当前发送的packet的大小，以及进度
    private ReadableByteChannel packetChannel ;
    private long total;
    private long position;

    public AsyncSendDispatcher(Sender sender) {
        this.sender = sender;
        sender.setSendProcessor(this);
    }

    @Override
    public void send(SendPacket packet) {
        queue.offer(packet);
        if (isSending.compareAndSet(false, true)) {
            sendNextPacket();
        }
    }

    private SendPacket takePacket() {
        SendPacket packet = queue.poll();
        return packet;
    }

    private void sendNextPacket() {
        SendPacket temp = packetTemp;
        if (temp != null) {
            CloseUtils.close(temp);
        }
        SendPacket packet = packetTemp = takePacket();
        if (packet == null) {
            // 队列为空，取消状态发送
            isSending.set(false);
            return;
        }
        total = packet.length();
        position = 0;
        sendCurrentPacket();
    }

    private void sendCurrentPacket() {
        if (position >= total) {
            completePacket(position == total);
            sendNextPacket();
            return;
        }
        try {
            sender.postSendAsync();
        } catch (IOException e) {
            closeAndNotify();
        }
    }


    /**
     * 关闭资源并将packet转换到具体的数据类型eg：string
     * @param isSuccess
     */
    private void completePacket(boolean isSuccess){
        SendPacket packet = this.packetTemp ;
        if (packet == null){
            return;
        }
        // close package 的时候，会调用具体packet的closeStream方法
        CloseUtils.close(packet);
        CloseUtils.close(packetChannel);

        packetTemp = null ;
        packetChannel = null ;
        total = 0 ;
        position = 0 ;
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            isSending.set(false);
            // 异常关闭导致的完成
            this.completePacket(false);
        }
    }


    @Override
    public IoArgs provideIoArgs() {
        IoArgs args = ioArgs;
        if (packetChannel == null){
            packetChannel = Channels.newChannel(packetTemp.open()) ;
            args.limit(4);
            args.writeLength((int) packetTemp.length());
        }else {
            args.limit((int) Math.min(args.capacity(), total - position));
            try {
                int count = args.readFrom(packetChannel) ;
                position += count ;
            } catch (IOException e) {
                e.printStackTrace();
                return null ;
            }
        }
        return args ;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        // 继续发送当前包
        sendCurrentPacket();
    }
}
