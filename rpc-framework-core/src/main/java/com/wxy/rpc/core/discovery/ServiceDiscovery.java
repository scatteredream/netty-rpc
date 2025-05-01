package com.wxy.rpc.core.discovery;

import com.wxy.rpc.core.common.RpcRequest;
import com.wxy.rpc.core.common.ServiceInfo;
import com.wxy.rpc.core.extension.SPI;
import com.wxy.rpc.core.loadbalance.LoadBalance;
import lombok.Setter;

import java.util.List;

/**
 * 服务发现接口类
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName ServiceDiscovery
 * @Date 2023/1/5 21:05
 */
@SPI
public abstract class ServiceDiscovery {

    @Setter
    protected String registryAddr;

    @Setter
    protected LoadBalance loadBalance;

    abstract public void start();
    /**
     * 进行服务发现
     *
     * @param request Rpc请求，封装了请求的服务名
     * @return 返回服务提供方信息
     */
    abstract public ServiceInfo discover(RpcRequest request);

    /**
     * 返回服务的所有提供方，若未实现，默认返回空的 ArrayList
     *
     * @param serviceName 服务名称
     * @return 所有的服务提供方信息
     */
    abstract public List<ServiceInfo> getServices(String serviceName) throws Exception;

    /**
     * 关闭与服务注册中心的连接
     */
    abstract public void destroy() throws Exception;

}
