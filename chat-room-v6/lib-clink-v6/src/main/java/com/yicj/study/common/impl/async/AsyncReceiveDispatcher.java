package com.yicj.study.common.impl.async;

import com.yicj.study.common.core.*;
import com.yicj.study.common.utils.CloseUtils;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * receiver的注册及数据的调度
 * ClassName: AsyncReceiveDispatcher
 * Description: TODO(描述)
 * Date: 2020/6/18 22:21
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class AsyncReceiveDispatcher
        implements ReceiveDispatcher,IoArgs.IoArgsEventProcessor, AsyncPacketWriter.PacketProvider {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Receiver receiver;
    private final ReceivePacketCallback callback;
    private final AsyncPacketWriter writer =new AsyncPacketWriter(this);

    public AsyncReceiveDispatcher(Receiver receiver, ReceivePacketCallback callback) {
        this.receiver = receiver;
        this.receiver.setReceiveListener(this);
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
            writer.close() ;
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    /**
     * 继续注册接收的操作
     */
    private void registerReceive() {
        try {
            receiver.postReceiveAsync();
        } catch (IOException e) {
            closeAndNotify();
        }
    }

    @Override
    public IoArgs provideIoArgs() {
        return writer.takeIoArgs() ;
    }

    //IoArgs.IoArgsEventProcessor
    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {

    }

    //IoArgs.IoArgsEventProcessor
    @Override
    public void onConsumeCompleted(IoArgs args) {
        if (isClosed.get()){
            return;
        }
        do {
            writer.consumeIoArgs(args) ;
        }while (args.remained() && !isClosed.get()) ;
        registerReceive();
    }

    //AsyncPacketWriter.PacketProvider
    @Override
    public ReceivePacket takePacket(byte type, long length, byte[] headerInfo) {
        return callback.onArrivedNewPacket(type, length) ;
    }

    //AsyncPacketWriter.PacketProvider
    @Override
    public void completedPacket(ReceivePacket packet, boolean isSuccess) {
        CloseUtils.close(packet);
        callback.onReceivePacketCompleted(packet);
    }
}
