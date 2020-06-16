package com.yicj.nio.v2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 * ClassName: NoBlockServer
 * Description: TODO(描述)
 * Date: 2020/6/16 12:18
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class NoBlockServer {

    public static void main(String[] args) throws IOException {
        //1. 获取通道
        ServerSocketChannel server = ServerSocketChannel.open() ;
        //2. 切换成非阻塞模式
        server.configureBlocking(false) ;
        //3， 绑定连接
        server.bind(new InetSocketAddress(6666)) ;
        //4. 获取选择器
        Selector selector = Selector.open();
        //4.1 将通道注册到选择器上，指定接收‘监听通道’事件
        server.register(selector, SelectionKey.OP_ACCEPT) ;
        //5. 轮询地获取选择器上已‘就绪’的事件--> 只要select()>0, 说明已就绪
        while (selector.select() > 0){
            //6. 获取当前选择器所有的注册的'选择键'（已就绪的监听事件）
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            //7. 获取已‘就绪’的事件（不同的事件做不同的事）
            while (iterator.hasNext()){
                SelectionKey key = iterator.next() ;
                // 接收事件就绪
                if (key.isAcceptable()){
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
                    //8. 获取客户端的连接
                    SocketChannel client = serverSocketChannel.accept();
                    //8.1 切换成非阻塞状态
                    client.configureBlocking(false) ;
                    //8.2 注册到选择器上--> 拿到客户端的连接为了读取通道的数据（监听读就绪）
                    client.register(selector,SelectionKey.OP_READ) ;
                }else if (key.isReadable()){
                    //9.  获取当前选择器都就绪状态的通道
                    SocketChannel client = (SocketChannel)key.channel();
                    //9.1 读取数据
                    ByteBuffer buffer = ByteBuffer.allocate(1024) ;
                    //9.2 得到文件通道，将客户端传递过来的图片写到本地项目下（写模式，没有就新建）
                    FileChannel outChannel = FileChannel.open(Paths.get("2.png"),
                            StandardOpenOption.WRITE,StandardOpenOption.CREATE) ;
                    while (client.read(buffer) > 0){
                        // 切换成读取模式
                        buffer.flip() ;
                        outChannel.write(buffer) ;
                        // 读完后切换到写模式，能让管道继续读取文件的数据
                        buffer.clear() ;
                    }

                    //10 此时服务端保存完图片之后，想要告诉客户端，图片已经上传成功了!
                    ByteBuffer writeBuffer = ByteBuffer.allocate(1024) ;
                    writeBuffer.put("the img is received , thanks you client ".getBytes()) ;
                    writeBuffer.flip() ;
                    client.write(writeBuffer) ;
                }
                //11 取消选择键（已经处理过的事件，就应该取消掉了）
                iterator.remove();
            }
        }
    }
}
