package com.yicj.study.client;

import com.yicj.study.client.bean.ServerInfo;
import com.yicj.study.common.utils.ByteUtils;
import com.yicj.study.constants.UDPConstants;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: ClientSearch
 * Description: TODO(描述)
 * Date: 2020/6/14 11:02
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class ClientSearch {
    private static final int LISTEN_PORT = UDPConstants.PORT_CLIENT_RESPONSE;

    public static ServerInfo searchServer(int timeout) {
        System.out.println("UDPSearcher start.");
        // 成功收到回送的栅栏
        CountDownLatch receiveLatch = new CountDownLatch(1) ;
        Listener listener = null ;
        try {
            listener = listen(receiveLatch) ;
            sendBroadcast() ;
            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            e.printStackTrace();
        }
        // 完成
        System.out.println("UDPSearcher finished.");
        if (listener == null){
            return null ;
        }
        List<ServerInfo> devices = listener.getServerAndClose() ;
        if (devices.size() > 0){
            return devices.get(0) ;
        }
        return null ;
    }

    private static Listener listen(CountDownLatch receiveLatch) throws InterruptedException {
        System.out.println("UDPSearcher start listen.");
        CountDownLatch startLatch = new CountDownLatch(1) ;
        Listener listener = new Listener(LISTEN_PORT, startLatch, receiveLatch) ;
        listener.start() ;
        startLatch.await();
        return listener ;
    }

    private static void sendBroadcast(){
        System.out.println("UDPSearcher sendBroadcast started.");
    }

    private static class Listener extends Thread{

        private final int listenPort ;
        private final CountDownLatch startLatch ;
        private final CountDownLatch receiveLatch ;
        private final List<ServerInfo> serverInfoList = new ArrayList<>() ;
        private final byte[] buffer = new byte[128] ;
        private final int minLen = UDPConstants.HEADER.length + 2 + 4 ;
        private boolean done = false;
        private DatagramSocket ds = null ;


        private Listener(int listenPort, CountDownLatch startLatch, CountDownLatch receiveLatch) {
            this.listenPort = listenPort;
            this.startLatch = startLatch;
            this.receiveLatch = receiveLatch;
        }

        @Override
        public void run() {
            // 通知已启动
            startLatch.countDown();
            try {
                // 监听回送端口
                ds = new DatagramSocket(listenPort) ;
                //构建接收实体
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length) ;
                while (!done){
                    // 接收
                    ds.receive(receivePack);
                    // 打印接收到的信息与发送者的信息
                    // 发送者的ip地址
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength() ;
                    byte[] data = receivePack.getData();
                    boolean isValid = dataLen >= minLen &&
                            ByteUtils.startsWith(data, UDPConstants.HEADER) ;
                    System.out.println("UDPSearcher receive from ip: " + ip +
                            "\tport:" + port +"\tdataValid:" + isValid);
                    if (!isValid){
                        continue;
                    }
                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, UDPConstants.HEADER.length, dataLen);
                    final short cmd = byteBuffer.getShort() ;
                    final int serverPort = byteBuffer.getInt() ;
                    if (cmd !=2 || serverPort <=0){
                        System.out.println("UDPSearcher receive cmd:" + cmd + "\tserverPort:" + serverPort);
                        continue;
                    }
                    String sn = new String(buffer,minLen,dataLen -minLen) ;
                    ServerInfo info = new ServerInfo(serverPort, ip, sn) ;
                    serverInfoList.add(info) ;
                    // 成功接收到一份
                    receiveLatch.countDown();
                }
            }catch (Exception ignore){
            }finally {
                close() ;
            }
            System.out.println("UDPSearcher listener finished.");
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        List<ServerInfo> getServerAndClose() {
            done = true;
            close();
            return serverInfoList;
        }
    }
}
