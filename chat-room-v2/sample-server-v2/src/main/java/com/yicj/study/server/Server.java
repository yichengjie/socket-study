package com.yicj.study.server;

import com.yicj.study.common.core.IoContext;
import com.yicj.study.common.impl.IoSelectorProvider;
import com.yicj.study.constants.TCPConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
    public static void main(String[] args) throws IOException {

        IoContext
                .setup()
                .ioProvider(new IoSelectorProvider())
                .start() ;

        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER) ;
        boolean isSucceed = tcpServer.start() ;
        if (!isSucceed){
            System.out.println("Start TCP server failed!");
            return;
        }
        UDPProvider.start(TCPConstants.PORT_SERVER) ;

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in)) ;
        String str ;
        do {
            str = bufferedReader.readLine() ;
            tcpServer.broadcast(str) ;
        }while (!"00bye00".equalsIgnoreCase(str)) ;
        UDPProvider.stop() ;
        tcpServer.stop() ;
        IoContext.close();
    }
}
