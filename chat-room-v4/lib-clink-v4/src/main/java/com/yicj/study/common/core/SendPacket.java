package com.yicj.study.common.core;

/**
 * 发送包
 * ClassName: SendPacket
 * Description: TODO(描述)
 * Date: 2020/6/17 21:07
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public abstract class SendPacket extends Packet {
    // 是否已取消
    private boolean isCanceled ;
    //发送的内容
    public abstract byte[] bytes() ;

    public boolean isCanceled(){
        return isCanceled ;
    }

}
