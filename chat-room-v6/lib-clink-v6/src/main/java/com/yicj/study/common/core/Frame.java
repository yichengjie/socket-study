package com.yicj.study.common.core;

import java.io.IOException;

/**
 * ClassName: Frame
 * Description: TODO(描述)
 * Date: 2020/6/25 20:05
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public abstract  class Frame {
    public static final int FRAME_HEADER_LENGTH = 6 ;
    public static final int MAX_CAPACITY = 64 *1024 -1 ;

    // 代表packet的header
    public static final byte TYPE_PACKET_HEADER = 11 ;
    // 代表packet的entity
    public static final byte TYPE_PACKET_ENTITY = 12 ;
    // 发送取消的操作
    public static final byte TYPE_COMMAND_SEND_CANCEL = 41 ;
    // 接收拒绝
    public static final byte TYPE_COMMAND_RECEIVE_REJECT = 42 ;
    // 后续扩展
    public static final byte FLAG_NONE = 0 ;



    protected final byte [] header = new byte[FRAME_HEADER_LENGTH] ;
    /**
     *
     * @param length  body部分的长度
     * @param type    body部分传输数据的类型
     * @param flag    body部分传输的flag
     * @param identifier  包的唯一表示
     */
    public Frame(int length, byte type, byte flag, short identifier){
        if (length < 0 || length > MAX_CAPACITY){
            throw new RuntimeException("length 长度["+length+"]不符合[1-"+MAX_CAPACITY+"]" + length) ;
        }
        if (identifier < 1 || identifier >255){
            throw new RuntimeException("identifier 长度["+identifier+"]不符合[1-255]") ;
        }
        //1. 长度部分
        header[0] = (byte) (length >> 8) ;
        header[1] = (byte) length ;
        //2. type部分
        header[2] = type ;
        //3. flag
        header[3] = flag ;
        //4. identifier
        header[4] = (byte) identifier ;
        //5. other
        header[5] = 0 ;
    }


    public Frame(byte [] header){
        System.arraycopy(header, 0 , this.header, 0, FRAME_HEADER_LENGTH);
    }

    public int getBodyLength(){
        return  ((((int) header[0]) & 0xFF) << 8) | (((int) header[1]) & 0xFF);
    }

    public byte getBodyType(){
        return header[2] ;
    }

    public byte getBodyFlag(){
        return header[3] ;
    }

    public short getBodyIdentifier(){
        return (short) ((short) header[4] & 0xFF) ;
    }

    /**
     *
     * @return 当前是否消费完成
     */
    public abstract boolean handle(IoArgs args) throws IOException;

    // 64Mb   64KB 1024+1  6
    //下一帧
    public abstract Frame nextFrame() ;
}
