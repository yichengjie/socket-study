package com.yicj.study.demo;

import java.nio.ByteBuffer;

/**
 * ClassName: ByteBufferDemo
 * Description: TODO(描述)
 * Date: 2020/6/16 9:31
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class ByteBufferDemo {

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(256) ;
        buffer.putInt(12) ;
        buffer.putInt(13) ;
        buffer.putInt(14) ;
        //
        //buffer.clear() ;
        //System.out.println(buffer.position() +" , " + buffer.limit());
        buffer.flip() ;
        //buffer.rewind() ;

        System.out.println(buffer.position() +" , " + buffer.limit());
        int a = buffer.getInt();
        int b = buffer.getInt() ;
        int c = buffer.getInt() ;
        //int d = buffer.getInt() ;

        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        //System.out.println(d);
        //buffer.flip();
        buffer.rewind() ;
        int e = buffer.getInt();
        System.out.println(e);

    }
}
