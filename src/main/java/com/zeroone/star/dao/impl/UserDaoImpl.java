package com.zeroone.star.dao.impl;

import com.zeroone.star.dao.UserDao;
import com.zeroone.star.entity.User;

/**
 * <p>
 * 描述：用户操作接口实现
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
public class UserDaoImpl implements UserDao {

    @Override
    public User getUserByUsername(String username) {
        // TODO:硬编码测试获取用户信息
        if ("admin".equals(username)) {
            User user = new User();
            user.setUsername(username);
            user.setPassword("123456");
            user.setName("王三石");
            return user;
        }
        return null;
    }

    @Override
    public Integer getUrlCount(String username, String url) {
        // TODO:硬编码获取实现
        return 1;
    }
}
