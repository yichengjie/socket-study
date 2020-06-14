package com.yicj.study.server;

import com.yicj.study.constants.TCPConstants;

import java.io.IOException;

/**
 * ClassName: Server
 * Description: TODO(描述)
 * Date: 2020/6/14 10:14
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class Server {
    public static void main(String[] args) {

        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER) ;
        boolean isSucceed = tcpServer.start() ;
        if (!isSucceed){
            System.out.println("Start TCP server failed!");
            return;
        }
        UDPProvider.start(TCPConstants.PORT_SERVER) ;
        try {
            System.in.read() ;
        }catch (IOException e){
            e.printStackTrace();
        }
        UDPProvider.stop() ;
        tcpServer.stop() ;
    }
}
