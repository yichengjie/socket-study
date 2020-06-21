package com.yicj.study.common.box;

import com.yicj.study.common.core.ReceivePacket;
import com.yicj.study.common.core.Receiver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * ClassName: StringReceivePacket
 * Description: TODO(描述)
 * Date: 2020/6/17 21:18
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class StringReceivePacket extends ReceivePacket<ByteArrayOutputStream> {

    private String string ;

    public StringReceivePacket(long len){
        length = len ;
    }

    public String string(){
        return string ;
    }

    @Override
    protected void closeStream(ByteArrayOutputStream stream) throws IOException {
        super.closeStream(stream);
        string = new String(stream.toByteArray()) ;
    }

    @Override
    protected ByteArrayOutputStream createStream() {
        return new ByteArrayOutputStream((int) length);
    }
}
