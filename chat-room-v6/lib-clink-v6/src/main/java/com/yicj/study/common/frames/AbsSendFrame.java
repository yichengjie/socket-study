package com.yicj.study.common.frames;

import com.yicj.study.common.core.Frame;
import com.yicj.study.common.core.IoArgs;

import java.io.IOException;


public abstract class AbsSendFrame extends Frame {
    protected volatile byte headerRemaining = Frame.FRAME_HEADER_LENGTH ;
    protected volatile int bodyRemaining ;
    /**
     * @param length     body部分的长度
     * @param type       body部分传输数据的类型
     * @param flag       body部分传输的flag
     * @param identifier 包的唯一表示
     */
    public AbsSendFrame(int length, byte type, byte flag, short identifier) {
        super(length, type, flag, identifier);
        bodyRemaining = length;
    }

    /**
     * 同一时刻只有一个线程消费数据
     * @param args
     * @return
     * @throws IOException
     */
    @Override
    public synchronized boolean handle(IoArgs args) throws IOException {
        try {
            //可读部分数据长度
            args.limit(headerRemaining + bodyRemaining);
            args.startWriting();
            //如果头部还有数据消费
            if (headerRemaining > 0 && args.remained()){
                headerRemaining -= consumeHeader(args) ;
            }
            //如果body有数据消费
            if (headerRemaining ==0 && bodyRemaining >0 && args.remained()){
                bodyRemaining -= consumeBody(args) ;
            }
            return headerRemaining == 0 && bodyRemaining ==0;
        }finally {
            args.finishWriting();
        }
    }

    @Override
    public int getConsumableLength() {
        return headerRemaining + bodyRemaining;
    }

    /**
     * 消费头部数据
     * @param args
     * @return
     * @throws IOException
     */
    private byte consumeHeader(IoArgs args){
        int count = headerRemaining ;
        // 总长度 - 剩余长度得到待消费开始位置
        int offset = header.length - count ;
        return (byte) args.readFrom(header, offset, count) ;
    }

    /**
     * 消费body数据
     * @param args
     * @return
     */
    protected abstract int consumeBody(IoArgs args) throws IOException;


    protected synchronized boolean isSending(){
        return headerRemaining < Frame.FRAME_HEADER_LENGTH ;
    }
}
