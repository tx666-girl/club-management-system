package com.zeroone.star.dao;

import com.zeroone.star.entity.User;

/**
 * <p>
 * 描述：用户数据库操作接口
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
public interface UserDao {

    /**
     * 通过用户名获取用户信息
     * @param username 用户名
     * @return 获取到的用户信息，没有查询到返回null
     */
    User getUserByUsername(String username);

    /**
     * 获取指定用户拥有指定url资源的个数
     * @param username 用户名
     * @param url url资源名称
     * @return 返回资源个数
     */
    Integer getUrlCount(String username, String url);

    /**
     *
     */
}
