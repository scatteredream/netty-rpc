package com.wxy.rpc.core.exception;

/**
 * @author Wuxy
 * @version 1.0
 * {@code ClassName} SerializeException
 * {@code Date} 2025/1/5 16:03
 */
public class SerializeException extends RuntimeException {

    private static final long serialVersionUID = 3365624081242234232L;

    public SerializeException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
