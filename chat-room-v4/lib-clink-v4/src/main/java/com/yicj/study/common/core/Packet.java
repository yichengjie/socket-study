package com.yicj.study.common.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * 公用的数据封装
 * 提供了类型以及基本的长度定义
 * ClassName: Packet
 * Description: TODO(描述)
 * Date: 2020/6/17 21:05
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public abstract class Packet <T extends Closeable> implements Closeable {
    protected long length ;
    private T stream ;

    public long length(){
        return length ;
    }

    protected abstract T createStream() ;

    public final T open() {
        if (stream == null){
            stream = createStream() ;
        }
        return stream;
    }

    @Override
    public final void close() throws IOException {
        if (stream != null){
            closeStream(stream);
            stream = null ;
        }
    }

    protected void closeStream(T stream) throws IOException {
        stream.close();
    }

}
