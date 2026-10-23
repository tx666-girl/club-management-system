package com.zeroone.star.dao.impl;

import com.zeroone.star.dao.UserDao;
import com.zeroone.star.entity.User;
import com.zeroone.star.util.DruidUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    public User getUserByUsername(String username){
        //登录的时候用这个查用户
        String sql="SELECT id,username,password,name,student_no,grade,role,status,created_at FROM sys_user WHERE username=?";
        try (Connection conn=DruidUtil.getConnection();
             PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setString(1,username);
            try (ResultSet rs=ps.executeQuery()){
                if (rs.next()){
                    return toUser(rs);
                }
                return null;
            }
        }catch (SQLException e){
            throw new RuntimeException("查用户失败",e);
        }
    }

    @Override
    public User getUserById(long id){
        String sql="SELECT id,username,password,name,student_no,grade,role,status,created_at FROM sys_user WHERE id=?";
        try(Connection conn=DruidUtil.getConnection();
             PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setLong(1, id);
            try(ResultSet rs=ps.executeQuery()){
                if (rs.next()){
                    return toUser(rs);
                }
                return null;
            }
        } catch(SQLException e){
            throw new RuntimeException("按id查用户异常",e);
        }
    }

    @Override
    public long createUser(User user){
        throw new UnsupportedOperationException("TODO B：请实现 UserDao.createUser");
    }

    @Override
    public void updateProfile(long id, String name, String studentNo, String grade){
        throw new UnsupportedOperationException("TODO B：请实现 UserDao.updateProfile");
    }

    @Override
    public void updatePassword(long id, String password){
        throw new UnsupportedOperationException("TODO B：请实现UserDao.updatePassword");
    }

    @Override
    public Integer getUrlCount(String username, String url){
        return 1;
    }

    private User toUser(ResultSet rs) throws SQLException{
        User u=new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setName(rs.getString("name"));
        u.setStudentNo(rs.getString("student_no"));
        u.setGrade(rs.getString("grade"));
        u.setRole(rs.getString("role"));
        u.setStatus(rs.getString("status"));
        u.setCreatedAt(rs.getTimestamp("created_at").toString());
        return u;
    }
}
