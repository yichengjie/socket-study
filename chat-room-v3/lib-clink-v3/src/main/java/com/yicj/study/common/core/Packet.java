package com.yicj.study.common.core;

import java.io.Closeable;

/**
 * 公用的数据封装
 * 提供了类型以及基本的长度定义
 * ClassName: Packet
 * Description: TODO(描述)
 * Date: 2020/6/17 21:05
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public abstract class Packet implements Closeable {
    protected byte type ;
    protected int length ;

    public byte type(){
        return type ;
    }

    public int length(){
        return length ;
    }
}
