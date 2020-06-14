package com.yicj.study.udp;

import java.io.IOException;
import java.net.*;

/**
 * UDP 搜索者，用于搜索服务支持方
 * ClassName: UDPSearcher
 * Description: TODO(描述)
 * Date: 2020/6/13 11:27
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class UDPSearcher {

    public static void main(String[] args) throws IOException {

        System.out.println("UDPSearcher  Started.");
        // 作为搜索放，让系统自动分配端口
        DatagramSocket ds = new DatagramSocket() ;

        // 构建一份请求数据
        String requestData = "HelloWorld!";
        byte [] requestDataBytes = requestData.getBytes() ;
        // 直接根据发送者构建一份回送信息
        DatagramPacket requestPacket = new DatagramPacket(
                requestDataBytes,
                requestDataBytes.length
        );
        // 本机20000端口
        requestPacket.setAddress(InetAddress.getLocalHost());
        requestPacket.setPort(20000);
        // 发送
        ds.send(requestPacket);

        // 构建接收实体
        final byte [] buf = new byte[512] ;
        DatagramPacket receivePack = new DatagramPacket(buf,buf.length) ;
        // 接收
        ds.receive(receivePack) ;
        // 打印接收到的信息，与发送者的信息
        String ip = receivePack.getAddress().getHostAddress() ;
        int port = receivePack.getPort();
        int dataLen = receivePack.getLength();
        String data = new String(receivePack.getData(),0, dataLen) ;
        String tempStr = "UDPSearcher  receive from ip: %s, \tport: %s, \tdata: %s" ;
        System.out.println(String.format(tempStr,ip,port,data));

        //完成
        System.out.println("UDPSearcher Finished.");
        ds.close();
    }
}
