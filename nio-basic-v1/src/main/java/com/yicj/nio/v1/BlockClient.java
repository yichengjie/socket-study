package com.yicj.nio.v1;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;

/**
 * ClassName: BlockClient
 * Description: TODO(描述)
 * Date: 2020/6/16 11:09
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class BlockClient {

    public static void main(String[] args) throws Exception {
        //1. 获取通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",6666)) ;
        //2. 发送一张图片给服务器端
        FileChannel fileChannel = FileChannel.open(Paths.get("D:\\opt\\applog\\test.png")) ;
        //3. 要使用NIO，有了Channel，就必然有Buffer，Buffer是与数据打交道的
        ByteBuffer buffer = ByteBuffer.allocate(1024) ;
        //4. 读取本地文件（图片）, 发送到服务器
        while (fileChannel.read(buffer) != -1){
            // 在都之前切换成读模式
            buffer.flip() ;
            socketChannel.write(buffer) ;
            // 读完后切换成写模式，能让惯到继续读取我呢见中的数据
            buffer.clear() ;
        }

        // 告诉服务器已经写完了
        socketChannel.shutdownOutput() ;

        //知道服务器要返回数据给客户端，客户端在这里接收
        int len = 0 ;
        while ((len = socketChannel.read(buffer)) != -1){
            //切换读取模式
            buffer.flip() ;
            byte[] array = buffer.array();
            System.out.println(new String(array, 0 ,len));
            // 切换写模式
            buffer.clear() ;
        }

        //5. 关闭流
        fileChannel.close();
        socketChannel.close();
    }
}
