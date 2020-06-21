package com.yicj.study.common.box;

import com.yicj.study.common.core.SendPacket;

import java.io.*;

/**
 * ClassName: StringSendPacket
 * Description: TODO(描述)
 * Date: 2020/6/17 21:13
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class FileSendPacket extends SendPacket<FileInputStream> {


    public FileSendPacket(File file) {
        this.length = file.length() ;
    }


    @Override
    protected FileInputStream createStream() {
        return null;
    }
}
