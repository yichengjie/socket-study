package com.yicj.nio.v1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * ClassName: BlockServer
 * Description: TODO(描述)
 * Date: 2020/6/16 11:18
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class BlockServer {
    public static void main(String[] args) throws IOException {
        //1. 获取通道
        ServerSocketChannel server = ServerSocketChannel.open() ;
        //2. 得到文件通道，将客户端传递过来的图片写道本地项目下（写模式，没有则创建）
        FileChannel outChannel = FileChannel.open(
                Paths.get("2.png"), StandardOpenOption.WRITE,StandardOpenOption.CREATE) ;
        //3. 绑定连接
        server.bind(new InetSocketAddress(6666)) ;

        //4. 获取客户端的连接（阻塞的）
        SocketChannel client = server.accept();
        //5. 要使用NIO有了Channel，就必然要有buffer
        ByteBuffer buffer = ByteBuffer.allocate(1024) ;
        //6. 将客户端传递过来的图片保存在本地中
        while (client.read(buffer) != -1){
            // 在读取之前切换为读模式
            buffer.flip() ;
            outChannel.write(buffer) ;
            // 读完后切换为写模式,能让惯到继续读取文件的数据
            buffer.clear() ;
        }

        // 此时服务端保存了图片之后，想要告诉客户端，图片已经上传完成
        buffer.put("img is success".getBytes()) ;
        buffer.flip() ;
        client.write(buffer) ;
        buffer.clear() ;


        //7. 关闭通道
        outChannel.close();
        client.close();
        server.close();
    }
}
