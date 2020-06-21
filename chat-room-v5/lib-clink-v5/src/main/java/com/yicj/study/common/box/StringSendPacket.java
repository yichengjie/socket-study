package com.yicj.study.common.box;

import com.yicj.study.common.core.SendPacket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ClassName: StringSendPacket
 * Description: TODO(描述)
 * Date: 2020/6/17 21:13
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class StringSendPacket extends BytesSendPacket {
    /**
     * 字符串发送时就是Byte数组，所以直接得到Byte数组，并按照Byte的发送方式发送即可
     * @param msg 字符串
     */
    public StringSendPacket(String msg) {
        super(msg.getBytes());
    }


    @Override
    public byte type() {
        return TYPE_MEMORY_STRING;
    }
}
