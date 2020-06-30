package com.yicj.study.client;

import com.yicj.study.Foo;
import com.yicj.study.client.bean.ServerInfo;
import com.yicj.study.common.box.FileSendPacket;
import com.yicj.study.common.core.IoContext;
import com.yicj.study.common.impl.IoSelectorProvider;

import java.io.*;

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
        File cachePath = Foo.getCacheDir("client") ;
        // 初始化
        IoContext.setup().ioProvider(new IoSelectorProvider()).start();

        ServerInfo info = UDPSearch.searchServer(10000) ;
        System.out.println("Server: " + info);
        if (info != null){
            TCPClient tcpClient = null ;
            try {
                tcpClient = TCPClient.startWith(info, cachePath);
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
        IoContext.close();
    }


    private static void write(TCPClient tcpClient) throws IOException {
        InputStream in = System.in ;
        BufferedReader input = new BufferedReader(new InputStreamReader(in)) ;
        do {
            // 键盘读取一行
            String str = input.readLine() ;
            if (str == null || str.length() ==0 || "00bye00".equalsIgnoreCase(str)){
                break;
            }
            // --f url
            if (str.startsWith("--f")){
                String [] arr = str.split(" ") ;
                if (arr.length >=2){
                    String filePath = arr[1] ;
                    File file = new File(filePath) ;
                    if (file.exists() && file.isFile()){
                        FileSendPacket packet = new FileSendPacket(file) ;
                        tcpClient.send(packet);
                        continue;
                    }
                }
            }
            // 发送字符串
            tcpClient.send(str);
        }while (true) ;
    }
}
