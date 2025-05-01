package com.wxy.rpc.core.extension.factory;

import com.wxy.rpc.core.extension.ExtensionFactory;
import com.wxy.rpc.core.extension.ExtensionLoader;
import com.wxy.rpc.core.extension.SPI;

/**
 * @author Wuxy
 * @version 1.0
 * {@code ClassName} SpiExtensionFactory
 * {@code Date} 2025/1/11 22:33
 */
public class SpiExtensionFactory implements ExtensionFactory {
    @Override
    public <T> T getExtension(Class<T> type, String name) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type cannot be null");
        }
//        if (!type.isInterface()) {
//            throw new IllegalArgumentException("Extension type must be an interface");
//        } 为了支持非接口的扩展类
        SPI annotation = type.getAnnotation(SPI.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Extension type must be annotated with @SPI");
        }
        ExtensionLoader<T> loader = ExtensionLoader.getExtensionLoader(type);
        return loader.getExtension(name);
    }
}
