package com.wxy.rpc.client.transport.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保存并获取 Channel 对象
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName ChannelProvider
 * @Date 2023/1/6 18:16
 */
@Slf4j
public class ChannelProvider {

    /**
     * 存储 Channel，key 为 ip:port，val 为 channel 对象
     */
    private final Map<String, Channel> channels = new ConcurrentHashMap<>();

    public Channel get(String hostname, Integer port) {
        String key = hostname + ":" + port;
        // 如果之前对应的 ip port 已经建立了 channel
        if (channels.containsKey(key)) {
            // 取出 channel
            Channel channel = channels.get(key);
            // 如果 channel 不为 null，并且处于活跃状态（连接状态）
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                // 为 null 或者已经关闭连接，从 map 中移除
                channels.remove(key);
            }
        }
        return null;
    }

    public Channel get(InetSocketAddress inetSocketAddress) {
        return get(inetSocketAddress.getHostName(), inetSocketAddress.getPort());
    }

    public void set(String hostname, Integer port, Channel channel) {
        String key = hostname + ":" + port;
        channels.put(key, channel);
    }

    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        this.set(inetSocketAddress.getHostName(), inetSocketAddress.getPort(), channel);
    }
    public void shutdownGracefully() {
        for (Map.Entry<String, Channel> entry : channels.entrySet()) {
            Channel channel = entry.getValue();
            if (channel != null && channel.isActive()) {
                channel.close().addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("Channel {} (connected to server [{}]) closed successfully",
                                future.channel().id().asLongText(),
                                entry.getKey());
                    }
                    else {
                        log.error("Failed to close channel {} (connected to server [{}])",
                                future.channel().id().asLongText(),
                                entry.getKey(), future.cause());
                    }
                });
            }
        }
        channels.clear();
        log.info("ChannelProvider shutdown gracefully");
    }
}
