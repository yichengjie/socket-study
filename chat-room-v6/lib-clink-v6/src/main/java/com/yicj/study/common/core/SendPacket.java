package com.yicj.study.common.core;

import java.io.InputStream;

/**
 * 发送包定义
 * ClassName: SendPacket
 * Description: TODO(描述)
 * Date: 2020/6/17 21:07
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public abstract class SendPacket<T extends InputStream> extends Packet<T> {
    // 是否已取消
    private boolean isCanceled ;

    public boolean isCanceled(){
        return isCanceled ;
    }


    /**
     * 设置取消发送标记
     */
    public void cancel(){

        isCanceled = true ;
    }
}
