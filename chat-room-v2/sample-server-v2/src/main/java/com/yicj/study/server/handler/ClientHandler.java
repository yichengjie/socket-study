package com.yicj.study.server.handler;

import com.yicj.study.common.utils.CloseUtils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 客户端消息处理
 * ClassName: ClientHandler
 * Description: TODO(描述)
 * Date: 2020/6/14 16:31
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class ClientHandler {
    private final Socket socket ;
    private final ClientReadHandler readHandler ;
    private final ClientWriteHandler writeHandler ;
    private final ClientHandlerCallback clientHandlerCallback ;
    private final String clientInfo ;

    public ClientHandler(Socket client, ClientHandlerCallback clientHandlerCallback) throws IOException {
        this.socket = client ;
        this.readHandler = new ClientReadHandler(socket.getInputStream()) ;
        this.writeHandler = new ClientWriteHandler(socket.getOutputStream()) ;
        this.clientHandlerCallback = clientHandlerCallback ;
        this.clientInfo =   "A ["+socket.getInetAddress().getHostAddress()+"], P["+socket.getPort()+"]" ;
        System.out.println("新客户端连接: " + clientInfo);
    }

    public String getClientInfo(){
        return clientInfo ;
    }

    public void exit() {
        readHandler.exit();
        writeHandler.exit() ;
        CloseUtils.close(socket);
        System.out.println("客户端已退出: " + socket.getInetAddress() +", P: " + socket.getPort());
    }

    public void exitBySelf(){
        exit();
        this.clientHandlerCallback.onSelfClosed(this);
    }

    public void send(String str) {
        this.writeHandler.send(str) ;
    }

    // 启动读取线程
    public void readToPrint() {
        readHandler.start();
    }

    public interface ClientHandlerCallback {
        // 自身关闭通知
        void onSelfClosed(ClientHandler handler) ;
        // 收到消息通知
        void onNewMessageArrived(ClientHandler handler, String msg) ;
    }

    class ClientReadHandler extends Thread{
        private boolean done =false ;
        private final InputStream inputStream ;

        public ClientReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        @Override
        public void run() {
            try {
                // 得到输入流，用于接收数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream)) ;
                do {
                    //客户端拿到一条数据
                    String str = socketInput.readLine() ;
                    if (str == null){
                        System.out.println("客户端已无法读取数据！");
                        // 退出当前客户端
                        ClientHandler.this.exitBySelf();
                        break;
                    }
                    clientHandlerCallback.onNewMessageArrived(ClientHandler.this, str);
                }while (!done) ;
            }catch (Exception e){
                if (!done){
                    System.out.println("连接异常断开!");
                    ClientHandler.this.exitBySelf();
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

    class ClientWriteHandler{
        private boolean done = false ;
        private final PrintStream printStream ;
        private final ExecutorService executorService ;

        ClientWriteHandler(OutputStream outputStream) {
            this.printStream = new PrintStream(outputStream);
            this.executorService = Executors.newSingleThreadExecutor() ;
        }

        void exit(){
            done = true ;
            CloseUtils.close(printStream);
            executorService.shutdownNow() ;
        }

        void send(String str) {
            // 如果客户端已经下线
            if (done){
                return;
            }
            executorService.execute(new WriteRunnable(str));
        }

        class WriteRunnable implements Runnable{
            private final String msg ;

            WriteRunnable(String msg) {
                this.msg = msg;
            }
            @Override
            public void run() {
                if (ClientWriteHandler.this.done){
                    return;
                }
                try {
                    ClientWriteHandler.this.printStream.println(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }
}
