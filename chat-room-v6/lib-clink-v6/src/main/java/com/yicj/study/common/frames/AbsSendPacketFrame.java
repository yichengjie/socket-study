package com.yicj.study.common.frames;

import com.yicj.study.common.core.Frame;
import com.yicj.study.common.core.IoArgs;
import com.yicj.study.common.core.SendPacket;

import java.io.IOException;

public abstract class AbsSendPacketFrame extends AbsSendFrame{
    protected volatile SendPacket<?> packet ;


    public AbsSendPacketFrame(int length, byte type, byte flag, short identifier, SendPacket<?> packet){
        super(length, type, flag, identifier);
        this.packet = packet ;
    }


    /**
     * 获取当前对应的发送packet
     * @return
     */
    public synchronized SendPacket getPacket(){
        return packet ;
    }


    @Override
    public final synchronized Frame nextFrame() {
        return packet == null ? null: buildNextFrame() ;
    }

    @Override
    public synchronized boolean handle(IoArgs args) throws IOException {
        if (packet == null && !isSending()){
            // 已经取消，并且未发送任何数据，直接返回结束，发送下一帧
            return true ;
        }
        return super.handle(args);
    }

    /**
     *
     * @return
     * true: 当前帧没有发送任何数据
     * false: 发送了部分数据
     */
    public final synchronized boolean abort(){
        boolean isSending = isSending() ;
        if (isSending){
            fillDirtyDataOnAbort() ;
        }
        packet = null ;
        return !isSending ;
    }

    protected void fillDirtyDataOnAbort(){

    }

    protected abstract Frame buildNextFrame() ;

}
