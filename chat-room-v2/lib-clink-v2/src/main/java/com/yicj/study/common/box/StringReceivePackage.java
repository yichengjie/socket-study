package com.yicj.study.common.box;

import com.yicj.study.common.core.ReceivePacket;
import com.yicj.study.common.core.Receiver;

/**
 * ClassName: StringReceivePackage
 * Description: TODO(描述)
 * Date: 2020/6/17 21:18
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class StringReceivePackage extends ReceivePacket {

    private byte [] buffer ;
    private int position ;

    public StringReceivePackage(int len){
        buffer = new byte[len] ;
        length = len ;
    }

    @Override
    public void save(byte[] bytes, int count) {
        System.arraycopy(bytes,0, buffer, position, count);
        position += count ;
    }

    public String string(){
        return new String(buffer) ;
    }

}
