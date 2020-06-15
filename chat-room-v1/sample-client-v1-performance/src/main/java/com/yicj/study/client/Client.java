package com.yicj.study.client;

import com.yicj.study.client.bean.ServerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * ClassName: Client
 * Description: TODO(描述)
 * Date: 2020/6/14 11:01
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class Client {

    public static void main(String[] args) throws IOException {
        ServerInfo info = UDPSearch.searchServer(10000) ;
        System.out.println("Server: " + info);
        if (info != null){
            TCPClient tcpClient = null ;
            try {
                tcpClient = TCPClient.startWith(info);
                if (tcpClient != null){
                    write(tcpClient);
                }
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                if (tcpClient!=null){
                    tcpClient.exit();
                }
            }

        }
    }


    private static void write(TCPClient tcpClient) throws IOException {
        InputStream in = System.in ;
        BufferedReader input = new BufferedReader(new InputStreamReader(in)) ;
        do {
            // 键盘读取一行
            String str = input.readLine() ;
            // 发送到服务器
            tcpClient.send(str);
            if ("00bye00".equalsIgnoreCase(str)){
                break;
            }
        }while (true) ;
    }

}
