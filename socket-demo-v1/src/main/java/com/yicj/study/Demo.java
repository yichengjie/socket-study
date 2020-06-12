package com.yicj.study;

import java.lang.reflect.Array;

/**
 * ClassName: Demo
 * Description: TODO(描述)
 * Date: 2020/6/12 20:49
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class Demo {

    public static void main(String[] args) {
        byte [] buffer = new byte[128]  ;
        buffer[0] = 1;
        String str = new String(buffer,0 , buffer.length) ;
        //System.out.println(Array.getByte(buffer,0));
        byte [] bb = "1".getBytes() ;
        System.out.println(bb[0]);
    }
}
