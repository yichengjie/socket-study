package com.yicj.study.common.frames;

import com.yicj.study.common.core.Frame;
import com.yicj.study.common.core.IoArgs;
import com.yicj.study.common.core.SendPacket;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public class SendEntityFrame extends AbsSendPacketFrame {

    private final long unConsumeEntityLength ;
    private ReadableByteChannel channel ;

    protected SendEntityFrame(short identifier, long entityLength, ReadableByteChannel channel, SendPacket<?> packet) {
        super((int)Math.min(entityLength, Frame.MAX_CAPACITY), Frame.TYPE_PACKET_ENTITY, Frame.FLAG_NONE, identifier, packet);
        this.channel = channel ;
        unConsumeEntityLength = entityLength - bodyRemaining ;
    }

    @Override
    protected int consumeBody(IoArgs args) throws IOException {
        if (packet == null){
            // 已经终止当前帧，则填充假数据
            return args.fillEmpty(bodyRemaining) ;
        }
        return args.readFrom(channel);
    }

    @Override
    public Frame buildNextFrame() {
        if (unConsumeEntityLength == 0){
            return null ;
        }
        return new SendEntityFrame(getBodyIdentifier(), unConsumeEntityLength, channel, packet);
    }
}
