package com.yicj.nio.v2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * ClassName: NoBlockClient
 * Description: TODO(描述)
 * Date: 2020/6/16 13:06
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class NoBlockClient {

    public static void main(String[] args) throws IOException {

        //1. 获取通道
        SocketChannel socketChannel = SocketChannel.open(
                new InetSocketAddress("127.0.0.1",6666)) ;
        //1.1 切换成非阻塞模式
        socketChannel.configureBlocking(false) ;
        //1.2 获取选择器
        Selector selector = Selector.open();
        //1.3 将通道注册到选择器中，获取服务端返回的数据
        socketChannel.register(selector, SelectionKey.OP_READ) ;
        //2. 发送一张图片给服务器
        FileChannel fileChannel = FileChannel.open(
                Paths.get("D:\\opt\\applog\\test.png")) ;
        //3. 要使用NIO，有了Channel，就必然需要有Buffer，Buffer是与数据打交道的
        ByteBuffer buffer = ByteBuffer.allocate(1024) ;
        //4. 读取本地文件（图片）,发送到服务器
        while (fileChannel.read(buffer) != -1){
            // 在读取之前都要切换到读模式
            buffer.flip() ;
            socketChannel.write(buffer) ;
            // 都完后切换到写模式，能让管道继续读取文件的数据
            buffer.clear() ;
        }
        //5. 轮询地获取选择器上已‘就绪’的事件--> 只要select()>0,说明已就绪
        while (selector.select() > 0){
            //6. 获取当前选择器所有注册的‘选择键’（已就绪的监听事件）
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            //7. 获取已‘就绪’的事件（不同事件做不同处理）
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                //8. 读事件就绪
                if (key.isReadable()){
                    //8.1 得到对应的通道
                    SocketChannel channel = (SocketChannel)key.channel();
                    ByteBuffer responseBuffer = ByteBuffer.allocate(1024) ;
                    //9. 知道服务端要返回响应数据给客户端，客户端在这里接收
                    int readBytes = channel.read(responseBuffer);
                    if (readBytes > 0){
                        // 切换到读模式
                        responseBuffer.flip() ;
                        System.out.println(new String(responseBuffer.array(),0, readBytes));
                    }
                }
            }
            // 10 取消选择键（已经处理的事件，就应该取消掉）
            iterator.remove();
        }
    }
}
