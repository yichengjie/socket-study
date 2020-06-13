package com.yicj.study.udp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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

    private static final int LISTEN_PORT = 30000;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("UDPSearcher started .");
        Listener listener = listen();
        sendBroadcast();
        // 读取键盘任意信息后可以退出
        System.in.read() ;
        List<Device> devices = listener.getDevicesAndClose();
        for (Device device: devices){
            System.out.println("Device: " + device);
        }
    }


    public static Listener listen() throws InterruptedException {
        System.out.println("UDPSearcher start listen .");
        CountDownLatch countDownLatch = new CountDownLatch(1) ;
        Listener listener = new Listener(LISTEN_PORT, countDownLatch) ;
        listener.start();
        countDownLatch.await();
        return listener ;
    }

    public static void sendBroadcast() throws IOException {
        System.out.println("UDPSearcher sendBroadcast Started.");
        // 作为搜索放，让系统自动分配端口
        DatagramSocket ds = new DatagramSocket() ;

        // 构建一份请求数据
        String requestData = MessageCreator.buildWithPort(LISTEN_PORT);
        byte [] requestDataBytes = requestData.getBytes() ;
        // 直接根据发送者构建一份回送信息
        DatagramPacket requestPacket = new DatagramPacket(
                requestDataBytes,
                requestDataBytes.length
        );
        // 20000端口，广播地址
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPacket.setPort(20000);
        // 发送
        ds.send(requestPacket);
        ds.close();
        //完成
        System.out.println("UDPSearcher sendBroadcast Finished.");
    }


    @Data
    @Builder
    @AllArgsConstructor
    private static class Device{
        private final int port ;
        private final String ip ;
        private final String sn ;
    }


    private static class Listener extends Thread{

        private final int listenPort ;
        private final CountDownLatch countDownLatch ;
        private List<Device> devices = new ArrayList<>() ;
        private boolean done = false ;
        private DatagramSocket ds =null;

        private Listener(int listenPort, CountDownLatch countDownLatch) {
            this.listenPort = listenPort;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            // 通知已启动
            countDownLatch.countDown(); ;
            try {
                // 监听回送端口
                ds = new DatagramSocket(listenPort) ;
                while (!done){
                    // 构建接收实体
                    final byte [] buf = new byte[512] ;
                    DatagramPacket receivePack = new DatagramPacket(buf,buf.length) ;
                    // 接收
                    ds.receive(receivePack);
                    // 打印接收到的信息与发送者的信息
                    //发送者id地址
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength() ;
                    String data = new String(receivePack.getData(),0 , dataLen) ;
                    String tempStr = "UDPProvider  receive from ip: %s, \tport: %s, \tdata: %s" ;
                    System.out.println(String.format(tempStr,ip,port,data));

                    String sn = MessageCreator.parseSn(data) ;
                    if (sn !=null){
                      Device  device = new Device(port, ip, sn) ;
                      devices.addAll(devices) ;
                    }
                }
            }catch (Exception ignore){
            }finally {
                close();
            }
            System.out.println("UDPSearcher listener finished !");
        }


        private void close(){
            if (ds != null){
                ds.close();
                ds = null ;
            }
        }

        public List<Device> getDevicesAndClose(){
            done = true ;
            close();
            return devices ;
        }
    }
}
