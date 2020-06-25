package com.yicj.study.common.core;

import java.io.Closeable;

/**
 * 发送数据的调度者
 * 缓存所有需要发送的数据，通过队列对数据进行发送
 * 并且在发送数据时，实现对数据的基本包装
 * ClassName: SendDispatcher
 * Description: TODO(描述)
 * Date: 2020/6/17 21:55
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public interface SendDispatcher extends Closeable {

    /**
     * 发送一份数据
     * @param packet 数据
     */
    void send(SendPacket packet) ;
}
