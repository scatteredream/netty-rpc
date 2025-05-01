package com.wxy.rpc.core.codec;

import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author Wuxy
 * @version 1.0
 * {@code ClassName} TestSharableRpcMessageCodec
 * {@code Date} 2025/1/5 17:19
 */
public class TestSharableRpcMessageCodec {

    static class Server {
        public static void main(String[] args) {
            NioEventLoopGroup boss = new NioEventLoopGroup();
            NioEventLoopGroup worker = new NioEventLoopGroup();
        }
    }

    static class Client {
        public static void main(String[] args) {

        }
    }

}
