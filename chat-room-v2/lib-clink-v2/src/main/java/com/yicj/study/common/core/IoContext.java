package com.yicj.study.common.core;

import java.io.IOException;

/**
 * IOProvider不是针对某一个连接的，是针对所有连接的，
 * 所有的连接都可以通过IoProvider进行注册和解除注册,是一个全局性的变量，
 * @title: IO上下文，这里面仅仅是对IOProvider进行提供
 * @description: TODO(描述)
 * @params 
 * @author yicj
 * @date 2020/6/16 20:15
 * @return 
 **/  
public class IoContext {
    private static IoContext INSTANCE;
    private final IoProvider ioProvider;

    private IoContext(IoProvider ioProvider) {
        this.ioProvider = ioProvider;
    }

    public IoProvider getIoProvider() {
        return ioProvider;
    }

    public static IoContext get() {
        return INSTANCE;
    }

    public static StartedBoot setup() {
        return new StartedBoot();
    }

    public static void close() throws IOException {
        if (INSTANCE != null) {
            INSTANCE.callClose();
        }
    }

    private void callClose() throws IOException {
        ioProvider.close();
    }



    public static class StartedBoot {
        private IoProvider ioProvider;

        private StartedBoot() {

        }

        public StartedBoot ioProvider(IoProvider ioProvider) {
            this.ioProvider = ioProvider;
            return this;
        }

        public IoContext start() {
            INSTANCE = new IoContext(ioProvider);
            return INSTANCE;
        }
    }
}
