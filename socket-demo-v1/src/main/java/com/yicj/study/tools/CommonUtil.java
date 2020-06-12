package com.yicj.study.tools;

/**
 * ClassName: CommonUtil
 * Description: TODO(描述)
 * Date: 2020/6/12 21:10
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class CommonUtil {

    public static void main(String[] args) {
        int a = 1196 ;
        byte[] bytes = int2ByteArray(a);
        int b = bytes2Int(bytes);
        System.out.println(b);
    }


    public static int bytes2Int(byte[] bytes){
        int num= ((bytes[0] <<24) & 0xFF0000) |
                 ((bytes[1] <<16) & 0xFF0000) |
                 ((bytes[2] <<8) & 0xFF00) |
                 (bytes[3] & 0xFF) ;
        return num;
    }


    public static byte [] int2ByteArray(int value){
        return new byte[]{
            (byte)((value >> 24)),
            (byte)((value >> 16)),
            (byte)((value >> 8)) ,
            (byte)(value)
        } ;
    }

}
