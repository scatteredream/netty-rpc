package com.wxy.rpc.server.config;

import com.wxy.rpc.core.extension.ExtensionFactory;
import com.wxy.rpc.core.extension.Holder;
import com.wxy.rpc.core.extension.factory.SpiExtensionFactory;
import com.wxy.rpc.core.registry.ServiceRegistry;
import com.wxy.rpc.server.spring.RpcServerBeanPostProcessor;
import com.wxy.rpc.server.transport.RpcServer;
import com.wxy.rpc.server.transport.http.HttpRpcServer;
import com.wxy.rpc.server.transport.netty.NettyRpcServer;
import com.wxy.rpc.server.transport.socket.SocketRpcServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RpcServer 端的自动配置类
 *
 * @author Wuxy
 * @version 1.0
 * {@code ClassName} RpcServerAutoConfiguration
 * {@code Date} 2025/1/8 12:34
 */
@Configuration
@EnableConfigurationProperties(RpcServerProperties.class)
public class RpcServerAutoConfiguration {

    private final Holder<ExtensionFactory> factoryHolder = new Holder<>(SpiExtensionFactory::new);
    private final RpcServerProperties rpcServerProperties;
    public RpcServerAutoConfiguration(RpcServerProperties properties) {
        this.rpcServerProperties = properties;
    }

    /**
     * 创建 ServiceRegistry 实例 bean，当没有配置时默认使用 zookeeper 作为配置中心
     */
    @Bean(name = "serviceRegistry")
    @ConditionalOnMissingBean
    public ServiceRegistry serviceRegistry() {
        String registryKey = rpcServerProperties.getRegistry();
        String registryAddr = rpcServerProperties.getRegistryAddr();
        ServiceRegistry registry = factoryHolder.get().getExtension(ServiceRegistry.class, registryKey);
        registry.setRegistryAddr(registryAddr);
        registry.start();
        return registry;
    }

    // 当没有配置通信协议属性时，默认使用 netty 作为通讯协议
    @Bean(name = "rpcServer")
    @ConditionalOnMissingBean
    public RpcServer getRpcServer() {
        String transport = rpcServerProperties.getTransport();
        if(transport.equalsIgnoreCase("http")) {
            return new HttpRpcServer();
        } else if(transport.equalsIgnoreCase("socket")) {
            return new SocketRpcServer();
        } else return new NettyRpcServer();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ServiceRegistry.class, RpcServer.class})
    public RpcServerBeanPostProcessor rpcServerBeanPostProcessor(ServiceRegistry serviceRegistry, RpcServer rpcServer, RpcServerProperties properties) {
        return new RpcServerBeanPostProcessor(serviceRegistry, rpcServer, properties);
    }

}
