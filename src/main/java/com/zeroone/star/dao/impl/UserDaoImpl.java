package com.zeroone.star.dao.impl;

import com.zeroone.star.dao.UserDao;
import com.zeroone.star.entity.User;
import com.zeroone.star.util.DruidUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    //查找用户名
    public User getUserByUsername(String username) {
        // 改为数据库查询
        return null;
    }

    @Override
    public Integer getUrlCount(String username, String url) {
        // 改为权限表查询
        return 1;
    }

}
