package com.yicj.study.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

/**
 * UDP 提供者，用于提供服务
 * ClassName: UDPProvider
 * Description: TODO(描述)
 * Date: 2020/6/13 11:26
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class UDPProvider {

    public static void main(String[] args) throws IOException {

        // 生成一份唯一标识
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn) ;
        provider.start();
        // 读取任意键盘信息后可以退出
        System.in.read() ;
        provider.exit();
    }


    private static class Provider extends Thread{

        private final String sn ;
        private boolean done = false;
        private DatagramSocket ds = null ;

        public Provider(String sn){
            this.sn = sn ;
        }

        @Override
        public void run() {
            try {
                //监听20000端口
                ds = new DatagramSocket(20000) ;
                System.out.println("UDPProvider  Started.");
                while (!done){
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
                    String tempStr = "UDPProvider  receive from ip: %s, \tport: %s, \tdata: %s" ;
                    System.out.println(String.format(tempStr,ip,port,data));

                    //解析端口号
                    int responsePort = MessageCreator.parsePort(data) ;
                    if (responsePort != -1){
                        // 构建一份回送数据
                        String responseData = MessageCreator.buildWithSn(sn) ;
                        byte [] responseDataBytes = responseData.getBytes() ;
                        // 直接根据发送者构建一份回送信息
                        DatagramPacket responsePacket = new DatagramPacket(
                                responseDataBytes,
                                responseDataBytes.length,
                                receivePack.getAddress(),
                                responsePort
                        );
                        ds.send(responsePacket);
                    }
                }
            }catch (Exception ignore){
                //异常可以忽略
                //ignore.printStackTrace();
            }finally {
                close();
            }
            //完成
            System.out.println("UDPProvider Finished.");
        }

        private void close(){
            if (ds != null){
                ds.close();
                ds = null ;
            }
        }


        void exit(){
            done =true ;
            close();
        }
    }


}
