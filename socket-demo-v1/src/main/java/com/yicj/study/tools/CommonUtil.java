package com.yicj.study.tools;

import java.nio.ByteBuffer;

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
        byte[] bytes = int2ByteArray2(a);
        int b = bytes2Int(bytes);
        System.out.println(b);


        ByteBuffer buffer = ByteBuffer.wrap(bytes) ;
        int anInt = buffer.getInt();
        System.out.println("position : " + buffer.position());
        System.out.println(anInt);

        new String() ;
    }

    public static int bytes2Int(byte [] values){
        return  (values[3] & 0xFF) |
                ((values[2] & 0xFF) << 8)  |
                ((values[1] & 0xFF) << 16) |
                ((values[0] & 0xFF) << 24)  ;
    }

    public static int bytes2Int2(byte[] bytes){
        int num=bytes[3] & 0xFF;
        num |=((bytes[2] <<8) & 0xFF00);
        num |=((bytes[1] <<16) & 0xFF0000);
        num |=((bytes[0] <<24) & 0xFF0000);
        return num;
    }


    public static int bytes2Int3(byte[] bytes){
       ByteBuffer buffer = ByteBuffer.wrap(bytes) ;
       return buffer.getInt() ;
    }

    public static byte [] int2ByteArray(int value){
        return new byte[]{
            (byte)((value >> 24) & 0xFF),
            (byte)((value >> 16) & 0xFF),
            (byte)((value >> 8) & 0xFF) ,
            (byte)(value & 0xFF)
        } ;
    }

    public static byte [] int2ByteArray2(int value){
       ByteBuffer buffer = ByteBuffer.allocate(4) ;
       buffer.putInt(value) ;
       return buffer.array() ;
    }

}
