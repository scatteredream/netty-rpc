package com.wxy.rpc.core.serialization;

import com.wxy.rpc.core.enums.SerializationType;
import com.wxy.rpc.core.extension.ExtensionFactory;
import com.wxy.rpc.core.extension.Holder;
import com.wxy.rpc.core.extension.factory.SpiExtensionFactory;
import com.wxy.rpc.core.serialization.hessian.HessianSerialization;
import com.wxy.rpc.core.serialization.jdk.JdkSerialization;
import com.wxy.rpc.core.serialization.json.JsonSerialization;
import com.wxy.rpc.core.serialization.kryo.KryoSerialization;
import com.wxy.rpc.core.serialization.protostuff.ProtostuffSerialization;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 序列化算法工厂，通过序列化枚举类型获取相应的序列化算法实例
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName SerializationFactory
 * @Date 2023/1/5 12:21
 */
@Slf4j
public class SerializationFactory {
    private static final Holder<ExtensionFactory> factoryHolder = new Holder<>(SpiExtensionFactory::new);
    private static final String packageName = "com.wxy.rpc.core.serialization";
    private static final Map<Byte, String> typeToNameMap = new HashMap<>();
    private static final Map<String, Byte> nameToTypeMap = new HashMap<>();
    static{
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends Serialization>> classes = reflections.getSubTypesOf(Serialization.class);
        for (Class<? extends Serialization> clazz : classes) {
            try {
                Serialization instance = clazz.getDeclaredConstructor().newInstance();
                byte type = instance.getType();
                String className = clazz.getSimpleName();
                String key = className.replace("Serialization", "").toLowerCase();
                // 处理重复的type
                if (typeToNameMap.containsKey(type)) {
                    log.error("警告: 类型 {} 重复，类 {} 将被忽略", type, clazz.getName());
                    continue;
                }
                typeToNameMap.put(type, key);
                nameToTypeMap.put(key, type);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                log.error("Error in instantiating {}: {}", clazz.getName(), e.getMessage());
            }
        }
    }
    public static Serialization getSerialization(byte type){
        return factoryHolder.get().getExtension(Serialization.class, getName(type));
    }
    public static byte getType(String name){
        return nameToTypeMap.get(name);
    }
    public static String getName(byte type){
        return typeToNameMap.get(type);
    }
    // 使用枚举不支持 SPI
    @Deprecated
    public static Serialization getSerialization(SerializationType enumType) {
        switch (enumType) {
            case JDK:
                return new JdkSerialization();
            case JSON:
                return new JsonSerialization();
            case HESSIAN:
                return new HessianSerialization();
            case KRYO:
                return new KryoSerialization();
            case PROTOSTUFF:
                return new ProtostuffSerialization();
            default:
                throw new IllegalArgumentException(String.format("The serialization type %s is illegal.",
                        enumType.name()));
        }
    }

}
