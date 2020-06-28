package com.yicj.study.common.frames;

import com.yicj.study.common.core.Frame;
import com.yicj.study.common.core.IoArgs;

/**
 * ClassName: ReceiveFrameFactory
 * Description: TODO(描述)
 * Date: 2020/6/27 22:21
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class ReceiveFrameFactory {

    public static AbsReceiveFrame createInstance(IoArgs args){
        byte [] buffer = new byte[Frame.FRAME_HEADER_LENGTH] ;
        args.writeTo(buffer, 0) ;
        byte type = buffer[2] ;
        switch (type){
            case Frame.TYPE_COMMAND_SEND_CANCEL:
                return new CancelReceiveFrame(buffer) ;
            case Frame.TYPE_PACKET_HEADER:
                return new ReceiveHeaderFrame(buffer) ;
            case Frame.TYPE_PACKET_ENTITY:
                return new ReceiveEntityFrame(buffer) ;
            default:
                throw new UnsupportedOperationException("Unsupported frame type: " + type) ;
        }
    }
}
