package com.yicj.study.common.impl;

import com.yicj.study.common.core.IoArgs;
import com.yicj.study.common.core.IoProvider;
import com.yicj.study.common.core.Receiver;
import com.yicj.study.common.core.Sender;
import com.yicj.study.common.utils.CloseUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ClassName: SocketChannelAdapter
 * Description: TODO(描述)
 * Date: 2020/6/16 20:24
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class SocketChannelAdapter implements Sender , Receiver, Closeable {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final SocketChannel channel;
    private final IoProvider ioProvider;
    private final OnChannelStatusChangedListener listener;
    private IoArgs.IoArgsEventProcessor receiveIoEventProcessor;
    private IoArgs.IoArgsEventProcessor sendIoEventProcessor;


    public SocketChannelAdapter(SocketChannel channel, IoProvider ioProvider,
                                OnChannelStatusChangedListener listener) throws IOException {
        this.channel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;

        channel.configureBlocking(false);
    }

    @Override
    public void setReceiveProcessor(IoArgs.IoArgsEventProcessor processor) {
        this.receiveIoEventProcessor = processor ;
    }

    @Override
    public void setSendProcessor(IoArgs.IoArgsEventProcessor processor) {
        this.sendIoEventProcessor = processor ;
    }

    @Override
    public boolean postReceiveAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }
        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public boolean postSendAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }
        return ioProvider.registerOutput(channel, outputCallback);
    }


    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            // 解除注册回调
            ioProvider.unRegisterInput(channel);
            ioProvider.unRegisterOutput(channel);
            // 关闭
            CloseUtils.close(channel);
            // 回调当前Channel已关闭
            listener.onChannelClosed(channel);
        }
    }


    /**
     * 接收到数据时的回调函数
     */
    private final IoProvider.HandleInputCallback inputCallback = new IoProvider.HandleInputCallback() {
        @Override
        protected void canProviderInput() {
            if (isClosed.get()) {
                return;
            }
            IoArgs.IoArgsEventProcessor processor = SocketChannelAdapter.this.receiveIoEventProcessor;
            IoArgs args = processor.provideIoArgs() ;
            try {
                // 具体的读取操作
                if (args.readFrom(channel) > 0) {
                    // 读取完成回调
                    processor.onConsumeCompleted(args);
                } else {
                    processor.onConsumeFailed(args,new IOException("Cannot readFrom any data!"));
                }
            } catch (IOException ignored) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };


    private final IoProvider.HandleOutputCallback outputCallback = new IoProvider.HandleOutputCallback() {
        @Override
        protected void canProviderOutput() {
            if (isClosed.get()) {
                return;
            }
            IoArgs.IoArgsEventProcessor processor = sendIoEventProcessor ;
            IoArgs args = processor.provideIoArgs();
            try {
                // 具体的读取操作
                if (args.writeTo(channel) > 0) {
                    // 读取完成回调
                    processor.onConsumeCompleted(args);
                } else {
                    processor.onConsumeFailed(args, new IOException("Cannot write any data!"));
                }
            } catch (IOException ignored) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };




    public interface OnChannelStatusChangedListener {
        void onChannelClosed(SocketChannel channel);
    }
}
