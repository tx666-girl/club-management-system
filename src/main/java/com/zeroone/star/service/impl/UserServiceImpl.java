package com.zeroone.star.service.impl;

import com.zeroone.star.dao.UserDao;
import com.zeroone.star.dao.impl.UserDaoImpl;
import com.zeroone.star.entity.User;
import com.zeroone.star.service.UserService;

/**
 * <p>
 * 描述：用户操作业务接口实现
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
public class UserServiceImpl implements UserService {

    private final UserDao userDao = new UserDaoImpl();

    @Override
    public int login(String username, String password) {
        User user = userDao.getUserByUsername(username);
        if (user == null) {
            return -1;
        }
        return password.equals(user.getPassword()) ? 1 : 0;
    }

    @Override
    public boolean hasPermission(String username, String url) {
        return userDao.getUrlCount(username, url) > 0;
    }

}
