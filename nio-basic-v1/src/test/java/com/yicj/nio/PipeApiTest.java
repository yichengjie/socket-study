package com.yicj.nio;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

/**
 * ClassName: PipeApiTest
 * Description: TODO(描述)
 * Date: 2020/6/16 13:36
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class PipeApiTest {


    @Test
    public void test1() throws IOException {
        //1. 获取管道
        Pipe pipe = Pipe.open() ;
        //---------------------------------------------//
        //2. 将缓存区中的数据写入管道
        ByteBuffer sinkBuffer = ByteBuffer.allocate(1024);
        Pipe.SinkChannel sinkChannel = pipe.sink();
        sinkBuffer.put("通过单向通道发送数据".getBytes()) ;
        sinkBuffer.flip() ;
        sinkChannel.write(sinkBuffer) ;
        //---------------------------------------------//
        ByteBuffer sourceBuf = ByteBuffer.allocate(1024) ;
        //3. 读取缓存区中的数据
        Pipe.SourceChannel sourceChannel = pipe.source();
        int len = sourceChannel.read(sourceBuf) ;
        System.out.println(new String(sourceBuf.array(),0 , len));
        sourceChannel.close();
        sinkChannel.close();
    }

    @Test
    public void test2(){
        ByteBuffer buffer = ByteBuffer.allocate(256) ;
        buffer.putInt(123) ;
        buffer.putInt(456) ;
        buffer.flip() ;
        int a = buffer.getInt();
        System.out.println(a);
        buffer.flip() ;
        System.out.println(buffer.position() +", " + buffer.limit());
        int b = buffer.getInt();
        System.out.println(b);
        // 这里再次读取的话会报错
        //int c = buffer.getInt();
        //System.out.println(c);
    }

    @Test
    public void test3(){
        ByteBuffer buffer = ByteBuffer.allocate(256) ;
        buffer.putInt(123) ;
        buffer.putInt(456) ;
        buffer.flip() ;
        int a = buffer.getInt();
        System.out.println(a);
        buffer.rewind() ;
        System.out.println(buffer.position() +", " + buffer.limit());
        int b = buffer.getInt();
        System.out.println(b);
        // 这里可以再次读取
        int c = buffer.getInt();
        System.out.println(c);
    }

}
