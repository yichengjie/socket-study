package com.yicj.study.common.impl.async;

import com.yicj.study.common.core.IoArgs;
import com.yicj.study.common.core.SendDispatcher;
import com.yicj.study.common.core.SendPacket;
import com.yicj.study.common.core.Sender;
import com.yicj.study.common.utils.CloseUtils;

import java.io.IOException;
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
public class AsyncSendDispatcher implements SendDispatcher {

    private final Sender sender;
    private final Queue<SendPacket> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isSending = new AtomicBoolean();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private IoArgs ioArgs = new IoArgs();
    private SendPacket packetTemp;

    // 当前发送的packet的大小，以及进度
    private int total;
    private int position;

    public AsyncSendDispatcher(Sender sender) {
        this.sender = sender;
    }

    @Override
    public void send(SendPacket packet) {
        queue.offer(packet);
        if (isSending.compareAndSet(false, true)) {
            sendNextPacket();
        }
    }

    @Override
    public void cancel(SendPacket packet) {

    }

    private SendPacket takePacket() {
        SendPacket packet = queue.poll();
        if (packet != null && packet.isCanceled()) {
            // 已取消，不用发送
            return takePacket();
        }
        return packet;
    }


    /**
     * 从队列中取出新的待发送的packet数据
     */
    private void sendNextPacket() {
        //1. 先将之前的packet关闭
        SendPacket temp = packetTemp;
        if (temp != null) {
            CloseUtils.close(temp);
        }
        //2. 从对了中获取新的待发送的packet
        SendPacket packet = packetTemp = takePacket();
        if (packet == null) {
            // 队列为空，取消状态发送
            isSending.set(false);
            return;
        }

        //准备发送
        total = packet.length();
        position = 0;

        sendCurrentPacket();
    }

    private void sendCurrentPacket() {
        IoArgs args = ioArgs;

        // 开始，清理
        args.startWriting();

        if (position >= total) {
            // 从队列中取出待发送的packet数据
            sendNextPacket();
            return;
        } else if (position == 0) {
            // 首包，需要携带长度信息
            args.writeLength(total);
        }

        byte[] bytes = packetTemp.bytes();
        // 把bytes的数据写入到IoArgs
        int count = args.readFrom(bytes, position);
        position += count;

        // 完成封装
        args.finishWriting();

        try {
            sender.sendAsync(args, ioArgsEventListener);
        } catch (IOException e) {
            closeAndNotify();
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            isSending.set(false);
            SendPacket packet = this.packetTemp;
            if (packet != null) {
                packetTemp = null;
                CloseUtils.close(packet);
            }
        }
    }

    private final IoArgs.IoArgsEventListener ioArgsEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {

        }

        @Override
        public void onCompleted(IoArgs args) {
            // 继续发送当前包
            sendCurrentPacket();
        }
    };

}
