package com.yicj.study.common.impl.async;

import com.yicj.study.common.box.StringReceivePacket;
import com.yicj.study.common.core.IoArgs;
import com.yicj.study.common.core.ReceiveDispatcher;
import com.yicj.study.common.core.ReceivePacket;
import com.yicj.study.common.core.Receiver;
import com.yicj.study.common.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
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
public class AsyncReceiveDispatcher implements ReceiveDispatcher,IoArgs.IoArgsEventProcessor {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private final Receiver receiver;
    private final ReceivePacketCallback callback;

    private IoArgs ioArgs = new IoArgs();
    private ReceivePacket<?> packetTemp;
    private WritableByteChannel packetChannel ;
    private long total;
    private long position;

    public AsyncReceiveDispatcher(Receiver receiver, ReceivePacketCallback callback) {
        this.receiver = receiver;
        this.receiver.setReceiveProcessor(this);
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
            completePacket(false);
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    private void registerReceive() {
        try {
            receiver.postReceiveAsync();
        } catch (IOException e) {
            closeAndNotify();
        }
    }


    /**
     * 解析数据到Packet
     */
    private void assemblePacket(IoArgs args) {
        if (packetTemp == null) {
            // 发送过来的数据长度
            int length = args.readLength();
            // 本次发送的数据包
            packetTemp = new StringReceivePacket(length);
            packetChannel = Channels.newChannel(packetTemp.open()) ;
            total = length;
            position = 0;
        }
        try {
            int count = args.writeTo(packetChannel);
            position += count;
            // 检查是否已完成一份Packet接收
            if (position == total) {
                completePacket(true);
            }
        }catch (IOException e){
            e.printStackTrace();
            completePacket(false);
        }
    }

    /**
     * 完成数据接收操作
     */
    private void completePacket(boolean isSuccess) {
        ReceivePacket packet = this.packetTemp;
        CloseUtils.close(packet);
        packetTemp = null ;
        WritableByteChannel channel = this.packetChannel ;
        CloseUtils.close(channel);
        packetChannel = null ;
        if (packet != null){
            callback.onReceivePacketCompleted(packet);
        }
    }


    @Override
    public IoArgs provideIoArgs() {
        IoArgs args = ioArgs ;
        int receiveSize;
        if (packetTemp == null) {
            // ioArgs 首次接收数据的buffer长度设置为4
            receiveSize = 4;
        } else {
            // ioArgus非首次接收数据时
            // total - position为数据包剩余总字节数，与args的总容量比较大小，取较小值
            receiveSize = (int) Math.min(total - position, args.capacity());
        }
        // 设置本次接收数据大小
        args.limit(receiveSize);
        return args ;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {

    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        assemblePacket(args);
        registerReceive();
    }
}
