package com.yicj.study;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * ClassName: Foo
 * Description: TODO(描述)
 * Date: 2020/6/14 18:27
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class Foo {

    public static final String COMMAND_EXIT = "00bye00";
    private static final String CACHE_DIR = "cache";

    public static void main(String[] args) {
//        String property = System.getProperty("user.dir");
//        System.out.println(property);
        File server = getCacheDir("server");
        String absolutePath = server.getAbsolutePath();
        System.out.println(absolutePath);
    }

    public static File getCacheDir(String dir){
        String path = System.getProperty("user.dir") + File.separator
                + CACHE_DIR + File.separator +dir;
        File file = new File(path) ;
        if (!file.exists()){
            if (!file.mkdirs()){
                throw new RuntimeException("Create path error: " + path) ;
            }
        }
        return file ;
    }

    public static File createRandomTemp(File parent){
        String str = UUID.randomUUID().toString() +".tmp" ;
        File file = new File(parent, str) ;
        try {
            file.createNewFile() ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file ;
    }
}
