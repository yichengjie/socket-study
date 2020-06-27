package com.yicj.study.common.impl.async;

import com.yicj.study.common.core.Frame;
import com.yicj.study.common.core.IoArgs;
import com.yicj.study.common.core.ReceivePacket;
import com.yicj.study.common.frames.*;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Collection;
import java.util.HashMap;

/**
 * 用户管理具体package的发送与接收
 * ClassName: AsyncPacketWriter
 * Description: TODO(描述)
 * Date: 2020/6/25 20:05
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class AsyncPacketWriter implements Closeable {


    private final PacketProvider provider ;
    private final HashMap<Short, PacketModel> packetMap = new HashMap<>() ;
    private final IoArgs args = new IoArgs() ;
    // packet 虽然可以穿插，可以接收一个packet的一部分，然乎再接收另一个packet的一部分
    // 但是在某一个固定的时刻一定是一帧一帧的接收的，所以当前一定有一个唯一的帧
    private volatile Frame frameTemp ;

    public AsyncPacketWriter(PacketProvider provider) {
        this.provider = provider;
    }


    synchronized IoArgs takeIoArgs() {
        //如果是首帧，则只有6个字节
        args.limit(frameTemp == null ? Frame.FRAME_HEADER_LENGTH
                : frameTemp.getConsumableLength() );
        return args ;
    }

    /**
     * 消费IoArgs中的数据
     * @param args
     */
    synchronized void consumeIoArgs(IoArgs args) {
        if (frameTemp ==null){
            Frame temp ;
            do {
                temp = buildNewFrame(args) ;
            }while (temp == null && args.remained()) ;
            if (temp ==null){
                return;
            }
            frameTemp = temp ;
            if (!args.remained()){
                return;
            }
        }
        Frame currentFrame = frameTemp ;
        do {
            try {
                if (currentFrame.handle(args)){
                    if (currentFrame instanceof ReceiveHeaderFrame){
                        ReceiveHeaderFrame headerFrame = (ReceiveHeaderFrame)currentFrame ;
                        ReceivePacket packet = provider.takePacket(headerFrame.getPacketType(),
                                headerFrame.getPacketLength(),
                                headerFrame.getPacketHeaderInfo());
                        appendNewPacket(headerFrame.getBodyIdentifier(), packet) ;
                    }else if (currentFrame instanceof ReceiveEntityFrame){
                        completeEntityFrame((ReceiveEntityFrame) currentFrame) ;
                    }
                    frameTemp = null ;
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }while (args.remained()) ;
    }

    private void completeEntityFrame(ReceiveEntityFrame frame) {
        synchronized (packetMap){
            short identifier = frame.getBodyIdentifier();
            //当前这一帧的长度
            int length = frame.getBodyLength();
            PacketModel model = packetMap.get(identifier);
            model.unReceivedLength -= length ;
            if (model.unReceivedLength <=0){
                provider.completedPacket(model.packet, true);
                packetMap.remove(identifier) ;
            }
        }
    }

    private void appendNewPacket(short identifier, ReceivePacket packet) {
        synchronized (packetMap){
            PacketModel model = new PacketModel(packet) ;
            packetMap.put(identifier,model) ;
        }
    }

    private Frame buildNewFrame(IoArgs args) {
        AbsReceiveFrame frame = ReceiveFrameFactory.createInstance(args);
        if (frame instanceof CancelReceiveFrame){
            cancelReceivePacket(frame.getBodyIdentifier()) ;
            return null ;
        }else if (frame instanceof ReceiveEntityFrame){
            WritableByteChannel channel = getPacketChannel(frame.getBodyIdentifier()) ;
            ((ReceiveEntityFrame)frame).bindPacketChannel(channel);
        }
        return frame ;
    }

    private WritableByteChannel getPacketChannel(short identifier) {
        synchronized (packetMap){
            PacketModel model = packetMap.get(identifier);
            return model ==null ? null: model.channel;
        }
    }

    private void cancelReceivePacket(short identifier) {
        synchronized (packetMap){
            PacketModel model = packetMap.get(identifier);
            if (model !=null){
                ReceivePacket packet = model.packet;
                provider.completedPacket(packet,false);
            }
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (packetMap){
            Collection<PacketModel> values = packetMap.values();
            for (PacketModel value: values){
                provider.completedPacket(value.packet, false);
            }
            packetMap.clear();
        }
    }


    interface PacketProvider{
        ReceivePacket takePacket(byte type, long length, byte[] headerInfo) ;

        /**
         *
         * @param packet 接收packet
         * @param isSuccess 是否完成
         */
        void completedPacket(ReceivePacket packet, boolean isSuccess) ;
    }

    static class PacketModel{
        final ReceivePacket packet ;
        final WritableByteChannel channel ;
        volatile long unReceivedLength ;
        PacketModel(ReceivePacket<?,?> packet){
            this.packet = packet ;
            this.channel = Channels.newChannel(packet.open()) ;
            this.unReceivedLength = packet.length() ;
        }
    }
}
