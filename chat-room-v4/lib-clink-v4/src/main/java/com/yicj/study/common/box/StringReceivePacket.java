package com.yicj.study.common.box;

import com.yicj.study.common.core.ReceivePacket;
import com.yicj.study.common.core.Receiver;

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
public class StringReceivePacket extends ReceivePacket {

    private byte [] buffer ;
    private int position ;

    public StringReceivePacket(int len){
        buffer = new byte[len] ;
        length = len ;
    }

    @Override
    public void save(byte[] bytes, int count) {
        /**
         * 参数1：原数组
         * 参数2：原数组起始位置
         * ---------------------
         * 参数3：目标数组
         * 参数4：目标数组起始位置
         * 参数5：复制数据长度
         */
        System.arraycopy(bytes,0, buffer, position, count);
        position += count ;
    }

    public String string(){
        return new String(buffer) ;
    }

    @Override
    public void close() throws IOException {

    }
}
