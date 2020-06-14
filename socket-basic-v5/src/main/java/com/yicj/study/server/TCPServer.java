package com.yicj.study.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ClassName: TCPServer
 * Description: TODO(描述)
 * Date: 2020/6/14 13:54
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class TCPServer {
    private final int port ;
    private ClientListener mListener ;

    public TCPServer(int port) {
        this.port = port ;
    }

    public boolean start(){
        try {
            ClientListener listener = new ClientListener(port) ;
            mListener = listener ;
            listener.start() ;
        }catch (IOException e){
            e.printStackTrace();
            return false ;
        }
        return true ;
    }

    public void stop(){
        if (mListener != null){
            mListener.exit() ;
        }
    }

    private static class ClientListener extends Thread{
        private ServerSocket server ;
        private boolean done = false ;

        private ClientListener(int port) throws IOException {
            server = new ServerSocket(port);
            System.out.println("服务器信息： " + server.getInetAddress() + " P: " + server.getLocalPort());
        }

        @Override
        public void run() {
            System.out.println("服务器准备就绪");
            // 等待客户端连接
            do {
                Socket client ;
                // 得到客户端
                try {
                    client = server.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                //客户端构建异步线程
                ClientHandler clientHandler = new ClientHandler(client) ;
                clientHandler.start() ;
            }while (!done) ;
            System.out.println("服务器已关闭!");
        }

        void exit(){
            done = true ;
            try {
                server.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    // 客户端消息处理
    private static class ClientHandler extends Thread{
        private Socket socket ;
        private boolean flag = true ;

        ClientHandler(Socket client) {
            this.socket = client ;
        }

        @Override
        public void run() {
            System.out.println("新客户端连接: " + socket.getInetAddress() +", P: " + socket.getPort());
            try {
                // 得到打印流，用户数据输出：服务器会送数据使用
                PrintStream socketOutput = new PrintStream(socket.getOutputStream()) ;
                // 得到输入流，用于接收数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream())) ;
                do {
                    //客户端拿到一条数据
                    String str = socketInput.readLine() ;
                    if ("bye".equalsIgnoreCase(str)){
                        flag =false ;
                        //回送
                        socketOutput.println("bye");
                    }else {
                        //打印到屏幕，并回送数据长度
                        System.out.println(str);
                        socketOutput.println("回送：" + str.length());
                    }
                }while (flag) ;

                socketInput.close();
                socketOutput.close();

            }catch (Exception e){
                System.out.println("连接异常断开!");
            }finally {
                // 连接关闭
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("客户端已退出: " + socket.getInetAddress() +", P: " + socket.getPort());
        }
    }
}
