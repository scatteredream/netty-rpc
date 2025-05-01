package com.wxy.rpc.api.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Wuxy
 * @version 1.0
 * {@code ClassName} User
 * {@code Date} 2025/1/8 23:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private String username;

    private String password;

    private Integer age;
}
