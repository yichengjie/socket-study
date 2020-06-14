package com.yicj.study.client;

import com.yicj.study.client.bean.ServerInfo;

import java.io.IOException;

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
            TCPClient.linkWith(info) ;
        }
    }
}
