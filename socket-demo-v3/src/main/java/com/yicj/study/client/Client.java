package com.yicj.study.client;

import com.yicj.study.client.bean.ServerInfo;

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

    public static void main(String[] args) {
        ServerInfo info = ClientSearch.searchServer(10000) ;
        System.out.println("Server: " + info);
    }
}
