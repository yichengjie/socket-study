package com.yicj.study.common.core;

/**
 * 接收包的定义
 * ClassName: ReceivePacket
 * Description: TODO(描述)
 * Date: 2020/6/17 21:10
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public abstract class ReceivePacket extends Packet {
    /**
     * 保存数据
     * @param bytes  数据
     * @param count  前面的某一部分数据
     */
    public abstract void save(byte [] bytes, int count) ;
}
