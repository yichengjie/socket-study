package com.yicj.study.common.box;


import java.io.ByteArrayOutputStream;

/**
 * ClassName: 字符串接收包
 * Description: TODO(描述)
 * Date: 2020/6/17 21:18
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class StringReceivePacket extends AbsByteArrayReceivePacket<String> {
    public StringReceivePacket(long len) {
        super(len);
    }

    @Override
    protected String buildEntity(ByteArrayOutputStream stream) {
        return new String(stream.toByteArray());
    }

    @Override
    public byte type() {
        return TYPE_MEMORY_STRING;
    }
}
