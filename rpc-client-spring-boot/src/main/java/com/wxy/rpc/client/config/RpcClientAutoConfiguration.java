package com.wxy.rpc.client.config;

import com.wxy.rpc.client.spring.RpcClientBeanPostProcessor;
import com.wxy.rpc.client.proxy.ClientStubProxyFactory;
import com.wxy.rpc.client.spring.RpcClientExitDisposableBean;
import com.wxy.rpc.client.transport.RpcClient;
import com.wxy.rpc.client.transport.http.HttpRpcClient;
import com.wxy.rpc.client.transport.netty.NettyRpcClient;
import com.wxy.rpc.client.transport.socket.SocketRpcClient;
import com.wxy.rpc.core.discovery.ServiceDiscovery;
import com.wxy.rpc.core.discovery.nacos.NacosServiceDiscovery;
import com.wxy.rpc.core.discovery.zk.ZookeeperServiceDiscovery;
import com.wxy.rpc.core.extension.ExtensionFactory;
import com.wxy.rpc.core.extension.Holder;
import com.wxy.rpc.core.extension.factory.SpiExtensionFactory;
import com.wxy.rpc.core.loadbalance.impl.ConsistentHashLoadBalance;
import com.wxy.rpc.core.loadbalance.LoadBalance;
import com.wxy.rpc.core.loadbalance.impl.RandomLoadBalance;
import com.wxy.rpc.core.loadbalance.impl.RoundRobinLoadBalance;
import com.wxy.rpc.core.serialization.Serialization;
import com.wxy.rpc.core.serialization.hessian.HessianSerialization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

/**
 * RpcClient 自动配置类
 * <pre>
 *     1. ConditionalOnBean：是否存在某个某类或某个名字的Bean
 *     2. ConditionalOnMissingBean：是否缺失某个某类或某个名字的Bean
 *     3. ConditionalOnSingleCandidate：是否符合指定类型的Bean只有⼀个
 *     4. ConditionalOnClass：是否存在某个类
 *     5. ConditionalOnMissingClass：是否缺失某个类
 *     6. ConditionalOnExpression：指定的表达式返回的是true还是false
 *     7. ConditionalOnJava：判断Java版本
 *     8. ConditionalOnJndi：JNDI指定的资源是否存在
 *     9. ConditionalOnWebApplication：当前应⽤是⼀个Web应⽤
 *     10. ConditionalOnNotWebApplication：当前应⽤不是⼀个Web应⽤
 *     11. ConditionalOnProperty：Environment中是否存在某个属性
 *     12. ConditionalOnResource：指定的资源是否存在
 *     13. ConditionalOnWarDeployment：当前项⽬是不是以War包部署的⽅式运⾏
 *     14. ConditionalOnCloudPlatform：是不是在某个云平台上
 * </pre>
 *
 * @author Wuxy
 * @version 1.0
 * &#064;ClassName  RpcClientAutoConfiguration
 * &#064;Date  2023/1/8 12:06
 */
@Configuration
@EnableConfigurationProperties(RpcClientProperties.class)
public class RpcClientAutoConfiguration {

    /**
     * 属性绑定的实现方式二：
     * - 创建 RpcClientProperties 对象，绑定到配置文件
     * - 如果使用此方法，可以直接给属性赋初始值
     *
     * @param environment 当前应用的环境（支持 yaml、properties 等文件格式）
     * @return 返回对应的绑定属性类
     * @deprecated 弃用，使用被 {@link org.springframework.boot.context.properties.ConfigurationProperties} 标注的属性类代替，
     * 生成 metadata。
     */
//    @Bean
    @Deprecated
    public RpcClientProperties rpcClientProperties(Environment environment) {
        // 获取绑定器，将对应的属性绑定到指定类上
        BindResult<RpcClientProperties> bind = Binder.get(environment).bind("rpc.client", RpcClientProperties.class);
        // 获取实例
        return bind.get();
    }

    private final RpcClientProperties rpcClientProperties;

    public RpcClientAutoConfiguration(RpcClientProperties rpcClientProperties) {
        this.rpcClientProperties = rpcClientProperties;
    }

    private final Holder<ExtensionFactory> factoryHolder = new Holder<>(SpiExtensionFactory::new);

    @Bean(name = "loadBalance")
    @ConditionalOnMissingBean
    public LoadBalance loadBalance() {
        String loadBalanceKey = rpcClientProperties.getLoadBalance();
        // 通过 SPI 扩展机制获取 LoadBalance 实现类
        return factoryHolder.get().getExtension(LoadBalance.class, loadBalanceKey);
    }

    @Bean(name = "serviceDiscovery")
    @ConditionalOnMissingBean
    public ServiceDiscovery serviceDiscovery(LoadBalance loadBalance) {
        String discoveryKey = rpcClientProperties.getRegistry();
        String registryAddr = rpcClientProperties.getRegistryAddr();
        ServiceDiscovery discovery = factoryHolder.get().getExtension(ServiceDiscovery.class, discoveryKey);
        discovery.setRegistryAddr(registryAddr);
        discovery.setLoadBalance(loadBalance);
        discovery.start();
        return discovery;
    }

    @Bean(name = "rpcClient")
    @ConditionalOnMissingBean
    public RpcClient getRpcClient(){
        String transport = rpcClientProperties.getTransport();
        if(transport.equalsIgnoreCase("http"))
            return new HttpRpcClient();
        if(transport.equalsIgnoreCase("socket"))
            return new SocketRpcClient();
        return new NettyRpcClient();
    }
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ServiceDiscovery.class, RpcClient.class})
    public ClientStubProxyFactory clientStubProxyFactory(ServiceDiscovery serviceDiscovery, RpcClient rpcClient, RpcClientProperties rpcClientProperties) {
        return new ClientStubProxyFactory(serviceDiscovery, rpcClient, rpcClientProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public RpcClientBeanPostProcessor rpcClientBeanPostProcessor(ClientStubProxyFactory clientStubProxyFactory) {
        return new RpcClientBeanPostProcessor(clientStubProxyFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public RpcClientExitDisposableBean rpcClientExitDisposableBean(
            ServiceDiscovery serviceDiscovery, RpcClient rpcClient) {
        return new RpcClientExitDisposableBean(serviceDiscovery,rpcClient);
    }

}
