package com.yicj.study.client;

import com.yicj.study.client.bean.ServerInfo;
import com.yicj.study.common.core.Connector;
import com.yicj.study.common.core.IoContext;
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


    public TCPClient(SocketChannel socket) throws IOException {
       setup(socket);
    }

    public void exit(){
        CloseUtils.close(this);
    }

    @Override
    protected void onReceiveNewMessage(String str) {
        super.onReceiveNewMessage(str);
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        System.out.println("连接已关闭无法读取数据！");
    }

    public static TCPClient startWith(ServerInfo info) throws IOException {
        SocketChannel socket = SocketChannel.open() ;
        // 连接本地，端口，超时时间3000
        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getIp()), info.getPort()));
        System.out.println("已发起服务器连接，并进入后续流程～");
        System.out.println("客户端信息：" + socket.getLocalAddress().toString());
        System.out.println("服务器信息：" + socket.getRemoteAddress().toString());
        try {
            return new TCPClient(socket) ;
        }catch (Exception e){
            System.out.println("连接异常");
            CloseUtils.close(socket);
        }
        return null ;
    }
}
