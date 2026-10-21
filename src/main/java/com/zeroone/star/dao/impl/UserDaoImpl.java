package com.zeroone.star.dao.impl;

import com.zeroone.star.dao.UserDao;
import com.zeroone.star.entity.User;

/**
 * 用户 DAO 实现练习。
 *
 * <p>实现前先阅读 UserDao 中每个方法的注释。
 * 本类只允许写 JDBC 数据访问代码，不负责判断登录密码是否正确。</p>
 *
 * <p>任务 A：用户查询；任务 B：用户新增和修改。</p>
 */
public class UserDaoImpl implements UserDao {

    @Override
    public User getUserByUsername(String username) {
        throw new UnsupportedOperationException("TODO A：请实现 UserDao.getUserByUsername");
    }

    @Override
    public User getUserById(long id) {
        throw new UnsupportedOperationException("TODO A：请实现 UserDao.getUserById");
    }

    @Override
    public long createUser(User user) {
        throw new UnsupportedOperationException("TODO B：请实现 UserDao.createUser");
    }

    @Override
    public void updateProfile(long id, String name, String studentNo, String grade) {
        throw new UnsupportedOperationException("TODO B：请实现 UserDao.updateProfile");
    }

    @Override
    public void updatePassword(long id, String password) {
        throw new UnsupportedOperationException("TODO B：请实现 UserDao.updatePassword");
    }

    @Override
    public Integer getUrlCount(String username, String url) {
        throw new UnsupportedOperationException("TODO A：请实现或按接口注释暂时返回 1");
    }
}
