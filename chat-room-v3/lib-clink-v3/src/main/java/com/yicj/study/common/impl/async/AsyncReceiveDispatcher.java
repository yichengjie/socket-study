package com.yicj.study.common.impl.async;

import com.yicj.study.common.box.StringReceivePacket;
import com.yicj.study.common.core.IoArgs;
import com.yicj.study.common.core.ReceiveDispatcher;
import com.yicj.study.common.core.ReceivePacket;
import com.yicj.study.common.core.Receiver;
import com.yicj.study.common.utils.CloseUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ClassName: AsyncReceiveDispatcher
 * Description: TODO(描述)
 * Date: 2020/6/18 22:21
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class AsyncReceiveDispatcher implements ReceiveDispatcher {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private final Receiver receiver;
    private final ReceivePacketCallback callback;

    private IoArgs ioArgs = new IoArgs();
    private ReceivePacket packetTemp;
    private byte[] buffer;
    private int total;
    private int position;

    public AsyncReceiveDispatcher(Receiver receiver, ReceivePacketCallback callback) {
        this.receiver = receiver;
        this.receiver.setReceiveListener(ioArgsEventListener);
        this.callback = callback;
    }

    @Override
    public void start() {
        registerReceive();
    }

    @Override
    public void stop() {

    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            ReceivePacket packet = packetTemp;
            if (packet != null) {
                packetTemp = null;
                CloseUtils.close(packet);
            }
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    private void registerReceive() {
        try {
            receiver.receiveAsync(ioArgs);
        } catch (IOException e) {
            closeAndNotify();
        }
    }


    /**
     * 解析数据到Packet
     */
    private void assemblePacket(IoArgs args) {
        if (packetTemp == null) {
            int length = args.readLength();
            packetTemp = new StringReceivePacket(length);
            buffer = new byte[length];
            total = length;
            position = 0;
        }
        int count = args.writeTo(buffer, 0);
        if (count > 0) {
            packetTemp.save(buffer, count);
            position += count;

            // 检查是否已完成一份Packet接收
            if (position == total) {
                completePacket();
                packetTemp = null;
            }
        }
    }

    /**
     * 完成数据接收操作
     */
    private void completePacket() {
        ReceivePacket packet = this.packetTemp;
        CloseUtils.close(packet);
        callback.onReceivePacketCompleted(packet);
    }


    private final IoArgs.IoArgsEventListener ioArgsEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {
            int receiveSize;
            if (packetTemp == null) {
                receiveSize = 4;
            } else {
                receiveSize = Math.min(total - position, args.capacity());
            }
            // 设置本次接收数据大小
            args.limit(receiveSize);
        }

        @Override
        public void onCompleted(IoArgs args) {
            assemblePacket(args);
            // 继续接收下一条数据
            registerReceive();
        }
    };


}
