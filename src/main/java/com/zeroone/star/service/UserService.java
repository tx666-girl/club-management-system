package com.zeroone.star.service;

/**
 * <p>
 * 描述：用户信息业务接口
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
public interface UserService {

    /**
     * 用户登录验证
     * @param username 用户名
     * @param password 密码
     * @return 登录结果 用户不存在返回-1，密码错误返回0，验证成功返回1
     */
    int login(String username, String password);

    /**
     * 用户是否有访问指定页面的权限
     * @param username 用户名
     * @param url 指定页面
     * @return 有权限返回true
     */
    boolean hasPermission(String username, String url);
}
