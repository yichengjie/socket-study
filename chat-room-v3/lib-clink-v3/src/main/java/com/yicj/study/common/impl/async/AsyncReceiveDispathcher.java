package com.yicj.study.common.impl.async;

import com.yicj.study.common.core.IoArgs;
import com.yicj.study.common.core.ReceiveDispatcher;
import com.yicj.study.common.core.ReceivePacket;
import com.yicj.study.common.core.Receiver;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ClassName: AsyncReceiveDispathcher
 * Description: TODO(描述)
 * Date: 2020/6/18 22:21
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class AsyncReceiveDispathcher implements ReceiveDispatcher {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Receiver receiver ;
    private final ReceivePacketCallback callback ;
    private IoArgs ioArgs = new IoArgs() ;
    private ReceivePacket packetTemp ;
    private byte[] buffer ;
    private int total ;
    private int position ;


    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void close() throws IOException {

    }
}
