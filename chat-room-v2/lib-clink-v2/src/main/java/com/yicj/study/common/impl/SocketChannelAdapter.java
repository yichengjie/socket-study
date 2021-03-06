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

    private final AtomicBoolean isClosed = new AtomicBoolean(false) ;
    private final SocketChannel channel ;
    private final IoProvider ioProvider ;
    private final OnChannelStatusChangedListener listener ;
    private IoArgs.IoArgsEventListener receiveIoEventListener ;
    private IoArgs.IoArgsEventListener sendIoEventListener ;

    public SocketChannelAdapter(SocketChannel channel, IoProvider ioProvider,
            OnChannelStatusChangedListener listener) throws IOException {
        this.channel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;
        channel.configureBlocking(false) ;
    }


    @Override
    public boolean receiveAsync(IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()){
            throw new IOException("Current channel is closed!") ;
        }
        receiveIoEventListener = listener ;
        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public boolean sendAsync(IoArgs args, IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()){
            throw new IOException("Current channel is closed!") ;
        }
        sendIoEventListener = listener ;
        // 当前发送的数据附加到回调中
        outputCallback.setAttach(args);
        return ioProvider.registerOutput(channel,outputCallback) ;
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false,true)){
            // 解除注册回调
            ioProvider.unRegisterInput(channel);
            ioProvider.unRegisterOutput(channel);
            // 关闭
            CloseUtils.close(channel);
            // 回调当前Channel已关闭
            listener.onChannelClosed(channel);
        }
    }

    public interface OnChannelStatusChangedListener{
        void onChannelClosed(SocketChannel channel) ;
    }


    private final IoProvider.HandleInputCallback inputCallback = new IoProvider.HandleInputCallback() {
        @Override
        protected void canProviderInput() {
            if (isClosed.get()){
                return;
            }
            IoArgs args = new IoArgs() ;
            IoArgs.IoArgsEventListener listener = SocketChannelAdapter.this.receiveIoEventListener ;
            if (listener !=null){
                listener.onStarted(args);
            }
            // 具体的读取操作
            try {
                if (args.read(channel) > 0 && listener != null){
                    //读取完成回调
                    listener.onCompleted(args);
                }else {
                    throw new IOException("Cannot read any data!") ;
                }
            }catch (IOException ignore){
                // 如果发生异常则关闭自己
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    } ;
    private final IoProvider.HandleOutputCallback outputCallback = new IoProvider.HandleOutputCallback() {
        @Override
        protected void canProviderOutput(Object attach) {
            if (isClosed.get()){
                return;
            }
            sendIoEventListener.onCompleted(null);
        }
    } ;

}
