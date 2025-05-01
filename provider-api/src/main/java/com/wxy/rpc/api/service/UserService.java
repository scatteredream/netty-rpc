package com.wxy.rpc.api.service;

import com.wxy.rpc.api.pojo.User;

import java.util.List;

/**
 * @author Wuxy
 * @version 1.0
 * {@code ClassName} UserService
 * {@code Date} 2025/1/8 23:43
 */
public interface UserService {

    User queryUser();

    List<User> getAllUsers();

}
