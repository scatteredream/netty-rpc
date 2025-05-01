package com.wxy.rpc.core.extension;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 拓展加载器类 <p>
 * 参考：<a href="https://cn.dubbo.apache.org/zh/docsv2.7/dev/source/dubbo-spi/">dubbo spi</a>
 * <p>
 * Extension - 即实现类，每一个接口 com.xxx.XxxInterface 对应一个文件，一个 ExtensionLoader 对应一个 interface，Loader 存储了
 * 该接口的所有扩展类实现类，并且缓存了这个接口文件内所有的声明的实现类
 * </p>
 *
 * @author Wuxy
 * @version 1.0
 * &#064;ClassName  ExtensionLoader
 * &#064;Date  2023/1/11 18:44
 */
@Slf4j
public class ExtensionLoader<T> {

    /**
     * 服务的存储目录
     */
    private static final String SERVICES_DIRECTORY = "META-INF/extensions/";

    /**
     * 扩展类加载器缓存，key - class，val - 对应的扩展器类
     */
    private static final Map<Class<?>, ExtensionLoader<?>> classToLoaderMap = new ConcurrentHashMap<>();

    /**
     * 存储接口实现类的实例，key - impClass，val - object 实例对象
     */
    private static final Map<Class<?>, Object> classToInstanceMap = new ConcurrentHashMap<>();

    /**
     * 拓展类加载器对应的接口类型
     */
    private final Class<?> type;


    /**
     * 缓存的实例
     */
    private final Map<String, Holder<Object>> keyToInstanceHolderMap = new ConcurrentHashMap<>();

//    private final Holder<Object> cachedAdaptiveInstance = new Holder<>();

    /**
     * 缓存的类型（当前接口的所有 Extension 类型，对应文件内的：String - key，implClass - value）
     */
    private final Map<String, Class<?>> keyToClassMap = new ConcurrentHashMap<>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    /**
     * 获取指定服务类型的拓展类加载器
     *
     * @param type 指定类型
     * @param <T>  服务类
     * @return 拓展类加载器
     */
    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
//        if (!type.isInterface()) {
//            throw new IllegalArgumentException(String.format("Extension type (%s) is not an interface!", type));
//        }为了支持非接口的扩展类
        if (type.getAnnotation(SPI.class) == null) {
            throw new IllegalArgumentException(String.format("Extension type (%s) is not an extension, " + "because it is NOT annotated with @%s!", type, SPI.class.getSimpleName()));
        }
        return (ExtensionLoader<T>) classToLoaderMap.computeIfAbsent(type, k -> new ExtensionLoader<>(type));
    }

    /**
     * 根据指定的 key 值获取扩展实现类实例
     *
     * @param name 指定名称
     * @return 扩展实现类实例
     */
    @SuppressWarnings("unchecked")
    public T getExtension(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Extension name == null.");
        }
        // 先从缓存中取出对应实例 Holder
        Holder<Object> holder = keyToInstanceHolderMap.computeIfAbsent(name, k -> new Holder<>(()-> createExtension(k)));
        return (T) holder.get();
    }

    /**
     * 根据 name （文件中定义的 key）创建 Extension 实例
     *
     * @param name 指定名称
     * @return 实例对象
     */
    @SuppressWarnings("unchecked")
    private T createExtension(String name) {
        // 获取指定 name 的拓展实现类类型
        Class<?> clazz = getExtensionClasses().get(name); // 从指定的目录加载当前接口的所有拓展类,返回一个map, key是拓展名, value是拓展类
        if (clazz == null) {
            throw new IllegalArgumentException("No such extension name " + name);
        }
        T t = (T) classToInstanceMap.computeIfAbsent(clazz, k -> {
            try {
                return k.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException |
                     InvocationTargetException | NoSuchMethodException e) {
                log.error("Failed to create extension instance.", e);
                throw new RuntimeException(e);
            }
        });
        return t;
    }

    /**
     * 获取当前接口的所有扩展实现类类型
     */
    private Map<String, Class<?>> getExtensionClasses() {
        loadDirectory(keyToClassMap);
        return keyToClassMap;
    }

    /**
     * 从指定的目录加载当前接口的所有拓展类
     *
     * @param extensionClasses 所有拓展类类型缓存 map
     */
    private void loadDirectory(Map<String, Class<?>> extensionClasses) {
        // 拼接文件名
        String filename = SERVICES_DIRECTORY + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(filename);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (IOException e) {
            log.debug("Failed to load directory.", e);
        }
    }

    /**
     * 加载指定资源路径下的所有扩展类类型
     *
     * @param extensionClasses 扩展类类型缓存
     * @param classLoader      类加载器
     * @param resourceUrl      路径资源
     */
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), StandardCharsets.UTF_8))) {
            String line;
            // 按行读取
            while ((line = reader.readLine()) != null) {
                // 得到注释的第一个索引值
                final int ci = line.indexOf("#");
                if (ci >= 0) {
                    // 过滤注释，截取 # 号前面的内容
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (!line.isEmpty()) {
                    try {
                        // 找到 = 的第一个位置
                        final int i = line.indexOf("=");
                        String name = line.substring(0, i).trim();
                        String className = line.substring(i + 1).trim();
                        if (!name.isEmpty() && !className.isEmpty()) {
                            Class<?> clazz = classLoader.loadClass(className);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error("Failed to load extension class (interface: {}, class line: {}) in {}, cause: {}", type, line, resourceUrl, e.getMessage(), e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Exception occurred when loading extension class (interface: {}, class file: {}) in {}", type, resourceUrl, resourceUrl, e);
            throw new RuntimeException(e);
        }
    }
}
