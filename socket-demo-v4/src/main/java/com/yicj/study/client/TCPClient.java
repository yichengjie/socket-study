package com.yicj.study.client;

import com.yicj.study.client.bean.ServerInfo;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * ClassName: TCPClient
 * Description: TODO(描述)
 * Date: 2020/6/14 14:23
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class TCPClient {


    public static void linkWith(ServerInfo info) throws IOException {
        Socket socket = new Socket() ;
        //超时时间
        socket.setSoTimeout(3000);
        // 连接本地，端口，超时时间3000
        socket.connect(new InetSocketAddress(InetAddress.getByName(info.getIp()), info.getPort()), 3000);
        System.out.println("已发起服务器连接，并进入后续流程～");
        System.out.println("客户端信息：" + socket.getLocalAddress() + " P:" + socket.getLocalPort());
        System.out.println("服务器信息：" + socket.getInetAddress() + " P:" + socket.getPort());
        try {
            // 发送接收数据
            todo(socket) ;
        }catch (Exception e){
            System.out.println("异常关闭");
        }
        // 释放资源
        socket.close();
        System.out.println("客户端已退出~");
    }

    private static void todo(Socket client) throws IOException {
        //构建键盘输入流
        InputStream in = System.in ;
        BufferedReader input = new BufferedReader(new InputStreamReader(in)) ;
        // 得到socket输出流，并转换为打印流
        OutputStream outputStream = client.getOutputStream() ;
        PrintStream socketPrintStream = new PrintStream(outputStream) ;
        // 得到Socket输入流，并转换为BufferReader
        InputStream inputStream = client.getInputStream() ;
        BufferedReader socketBufferReader = new BufferedReader(new InputStreamReader(inputStream)) ;
        boolean flag = true ;
        do {
            //键盘读取一行
            String str = input.readLine();
            // 发送到服务器
            socketPrintStream.print(str);
            // 从服务器读取一行
            String echo = socketBufferReader.readLine();
            if("bye".equalsIgnoreCase(echo)){
                flag = false ;
            }else {
                System.out.println(echo);
            }
        }while (false) ;
        // 释放资源
        socketPrintStream.close();
        socketBufferReader.close();
    }
}
