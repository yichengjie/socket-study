package com.yicj.study.client;

import com.yicj.study.client.bean.ServerInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: ClientTest
 * Description: TODO(描述)
 * Date: 2020/6/14 20:06
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class ClientTest {

    private static boolean done ;

    public static void main(String[] args) throws IOException {
        ServerInfo info = UDPSearch.searchServer(10000);
        System.out.println(info);
        if (info == null){
            return;
        }
        // 当前连接数量
        int size = 0;
        List<TCPClient> tcpClients = new ArrayList<>() ;
        for (int i = 0 ; i < 1000; i ++){
            try {
                TCPClient tcpClient = TCPClient.startWith(info) ;
                if (tcpClient == null){
                    System.out.println("连接异常");
                    continue;
                }
                tcpClients.add(tcpClient) ;
                System.out.println("连接成功 : " + (++size));
            } catch (IOException e) {
                System.out.println("连接异常");
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.in.read() ;

        Runnable runnable = () -> {
            while (!done){
                for (TCPClient tcpClient:  tcpClients){
                    tcpClient.send("Hello~~");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runnable) ;
        thread.start();

        System.in.read() ;
        done = true ;
        // 等待线程完成
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (TCPClient tcpClient:  tcpClients){
            tcpClient.exit();
        }
    }
}
