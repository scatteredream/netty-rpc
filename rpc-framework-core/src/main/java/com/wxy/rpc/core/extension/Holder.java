package com.wxy.rpc.core.extension;

import java.util.function.Supplier;

/**
 * Holder 类，作用是为不可变的对象引用提供一个可变的包装
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName Holder
 * @Date 2023/1/11 19:01
 */
public class Holder<T> {
    private volatile T value;
    private final Supplier<T> supplier;
    public Holder(Supplier<T> supplier) {
        this.supplier = supplier;
    }
    public T get() {
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    value = supplier.get();
                }
            }
        }
        return value;
    }

}