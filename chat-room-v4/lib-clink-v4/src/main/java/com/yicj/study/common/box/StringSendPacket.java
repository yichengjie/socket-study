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
public class StringSendPacket extends SendPacket<ByteArrayInputStream> {
    private final byte [] bytes ;

    public StringSendPacket(String msg) {
        this.bytes = msg.getBytes();
        this.length = bytes.length ;
    }

    @Override
    protected ByteArrayInputStream createStream() {
        return new ByteArrayInputStream(bytes);
    }
}
