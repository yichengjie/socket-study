package com.yicj.study.client;

import com.yicj.study.Foo;
import com.yicj.study.client.bean.ServerInfo;
import com.yicj.study.common.core.Connector;
import com.yicj.study.common.core.IoContext;
import com.yicj.study.common.core.Packet;
import com.yicj.study.common.core.ReceivePacket;
import com.yicj.study.common.utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;

/**
 * ClassName: TCPClient
 * Description: TODO(描述)
 * Date: 2020/6/14 14:23
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class TCPClient extends Connector {

    private final File cachePath ;

    public TCPClient(SocketChannel socket, File cachePath) throws IOException {
        this.cachePath =cachePath ;
        setup(socket);
    }

    public void exit(){
        CloseUtils.close(this);
    }

    @Override
    protected File createNewReceiveFile() {
        return Foo.createRandomTemp(cachePath);
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        System.out.println("连接已关闭无法读取数据！");
    }

    @Override
    protected void onReceivedPacket(ReceivePacket packet) {
        super.onReceivedPacket(packet);
        if (packet.type() == Packet.TYPE_MEMORY_STRING){
            String str = (String) packet.entity() ;
            System.out.println(key.toString() +": " + str);
        }
    }

    public static TCPClient startWith(ServerInfo info, File cachePath) throws IOException {
        SocketChannel socket = SocketChannel.open() ;
        // 连接本地，端口，超时时间3000
        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getIp()), info.getPort()));
        System.out.println("已发起服务器连接，并进入后续流程～");
        System.out.println("客户端信息：" + socket.getLocalAddress().toString());
        System.out.println("服务器信息：" + socket.getRemoteAddress().toString());
        try {
            return new TCPClient(socket,cachePath) ;
        }catch (Exception e){
            System.out.println("连接异常");
            CloseUtils.close(socket);
        }
        return null ;
    }
}
