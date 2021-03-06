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
    public void setReceiveListener(IoArgs.IoArgsEventProcessor processor) {
        receiveIoEventProcessor = processor;
    }

    @Override
    public boolean postReceiveAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }
        // 进行callback状态监测，判断是否处于自循环状态
        inputCallback.checkAttachNull() ;
        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public void setSendListener(IoArgs.IoArgsEventProcessor processor) {
        sendIoEventProcessor = processor;
    }

    @Override
    public boolean postSendAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }
        // 进行callback状态监测，判断是否处于自循环状态
        inputCallback.checkAttachNull() ;
        // 当前发送的数据附加到回调中
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

    private final IoProvider.HandleProviderCallback inputCallback = new IoProvider.HandleProviderCallback() {
        @Override
        protected void onProviderIo(IoArgs args) {
            if (isClosed.get()) {
                return;
            }
            IoArgs.IoArgsEventProcessor processor = receiveIoEventProcessor;
            if (args == null){
                // 拿一份新的IoArgs
                args = processor.provideIoArgs();
            }

            try {
                if (args == null){
                    processor.onConsumeFailed(null, new IOException("ProvideIoArgs is null."));
                }else {
                    int count = args.readFrom(channel);
                    if (count ==0){
                        System.out.println("Current read zero data !");
                    }
                    if (args.remained()){
                        // 附加当前未消费完成的args
                        attach = args ;
                        // 再次注册数据发送
                        ioProvider.registerInput(channel, this) ;
                    }else {
                        attach = null ;
                        // 读取数据完成回调
                        processor.onConsumeCompleted(args);
                    }
                }
            } catch (IOException ignored) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };


    private final IoProvider.HandleProviderCallback outputCallback = new IoProvider.HandleProviderCallback() {
        @Override
        protected void onProviderIo(IoArgs args) {
            if (isClosed.get()) {
                return;
            }

            IoArgs.IoArgsEventProcessor processor = sendIoEventProcessor;
            if (args == null){
                // 拿一份新的IoArgs
                args = processor.provideIoArgs();
            }
            try {
                if (args == null) {
                    processor.onConsumeFailed(null, new IOException("ProvideIoArgs is null."));
                }else {
                    int count = args.writeTo(channel);
                    if (count ==0){
                        System.out.println("Current write zero data !");
                    }
                    if (args.remained()){
                        // 附加当前未消费完成的args
                        attach = args ;
                        // 再次注册数据发送
                        ioProvider.registerOutput(channel, this) ;
                    }else {
                        attach = null ;
                        // 输出完成回调
                        processor.onConsumeCompleted(args);
                    }
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
