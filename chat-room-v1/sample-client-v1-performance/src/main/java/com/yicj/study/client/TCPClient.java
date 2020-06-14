package com.yicj.study.client;

import com.yicj.study.client.bean.ServerInfo;
import com.yicj.study.common.utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

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

    private final Socket socket ;
    private final ReadHandler readHandler ;
    private final PrintStream printStream ;


    public TCPClient(Socket socket, ReadHandler readHandler) throws IOException {
        this.socket = socket;
        this.readHandler = readHandler;
        this.printStream = new PrintStream(socket.getOutputStream());
    }


    public void exit(){
        readHandler.exit();
        CloseUtils.close(printStream);
        CloseUtils.close(socket);
    }

    public void send(String msg){
        printStream.println(msg);
    }


    public static TCPClient startWith(ServerInfo info) throws IOException {
        Socket socket = new Socket() ;
        //超时时间
        socket.setSoTimeout(3000);
        // 连接本地，端口，超时时间3000
        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getIp()), info.getPort()), 3000);
        System.out.println("已发起服务器连接，并进入后续流程～");
        System.out.println("客户端信息：" + socket.getLocalAddress() + " P:" + socket.getLocalPort());
        System.out.println("服务器信息：" + socket.getInetAddress() + " P:" + socket.getPort());
        try {
            ReadHandler readHandler = new ReadHandler(socket.getInputStream()) ;
            readHandler.start();
            return new TCPClient(socket,readHandler) ;
        }catch (Exception e){
            System.out.println("连接异常");
            CloseUtils.close(socket);
        }
        return null ;
    }

    static class ReadHandler extends Thread{
        private boolean done =false ;
        private final InputStream inputStream ;

        public ReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        @Override
        public void run() {
            try {
                // 得到输入流，用于接收数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream)) ;
                do {
                    String str = null ;
                    try {
                        //客户端拿到一条数据
                        str = socketInput.readLine() ;
                    }catch (SocketTimeoutException e){
                        continue;
                    }
                    if (str == null){
                        System.out.println("连接已关闭，无法读取数据！");
                        break;
                    }
                    //打印到屏幕，并回送数据长度
                    System.out.println(str);
                }while (!done) ;
            }catch (Exception e){
                if (!done){
                    System.out.println("连接异常断开:" + e.getMessage());
                }
            }finally {
                // 连接关闭
                CloseUtils.close(inputStream);
            }

        }
        void exit(){
            done = true ;
            CloseUtils.close(inputStream);
        }
    }
}
