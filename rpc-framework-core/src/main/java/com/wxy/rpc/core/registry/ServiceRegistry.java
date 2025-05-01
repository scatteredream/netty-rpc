package com.wxy.rpc.core.registry;

import com.wxy.rpc.core.common.ServiceInfo;
import com.wxy.rpc.core.extension.SPI;
import lombok.Setter;

/**
 * 服务注册中心接口
 *
 * @author Wuxy
 * @version 1.0
 * {@code ClassName} ServiceRegistry
 * {@code Date} 2025/1/5 21:06
 */
@SPI
public abstract class ServiceRegistry {

    @Setter
    protected String registryAddr;

    public abstract void start();

    /**
     * 注册/重新注册一个服务信息到 注册中心
     *
     * @param serviceInfo 服务信息
     */
    public abstract void register(ServiceInfo serviceInfo) throws Exception;

    /**
     * 接触注册/移除一个服务信息从 注册中心
     *
     * @param serviceInfo 服务信息
     */
    public abstract void unregister(ServiceInfo serviceInfo) throws Exception;

    /**
     * 关闭与服务注册中心的连接
     */
    public abstract void destroy() throws Exception;

}
