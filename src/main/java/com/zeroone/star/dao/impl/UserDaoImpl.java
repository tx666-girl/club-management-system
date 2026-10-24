package com.zeroone.star.dao.impl;

import com.zeroone.star.dao.UserDao;
import com.zeroone.star.entity.User;
import com.zeroone.star.util.DruidUtil;

import java.sql.*;

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
       String sql = "insert into sys_user (username, password, name, student_no, grade, role, status) VALUES (?,?,?,?,?,?,?)";
        try(Connection conn = DruidUtil.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1,user.getUsername());
            ps.setString(2,user.getPassword());
            ps.setString(3,user.getName());
            ps.setString(4,user.getStudentNo());
            ps.setString(5,user.getGrade());
            ps.setString(6,user.getRole());
            ps.setString(7,user.getStatus());
            ps.executeUpdate();
            try(ResultSet rs = ps.getGeneratedKeys()){
                if(rs.next()){
                    return rs.getLong(1);
                }
                throw new RuntimeException("新增用户成功，但未获取到自动生成的主键");
            }
        }catch(SQLException e){
            throw new RuntimeException("新增用户失败",e);
        }
    }

    @Override
    public void updateProfile(long id, String name, String studentNo, String grade){
       String sql = "update sys_user set name=?, student_no=?, grade=? where id=?";
         try(Connection conn = DruidUtil.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)){
             ps.setString(1,name);
             ps.setString(2,studentNo);
             ps.setString(3,grade);
             ps.setLong(4,id);
             ps.executeUpdate();
         }catch(SQLException e){
            throw new RuntimeException("修改用户资料失败",e);
        }
    }

    @Override
    public void updatePassword(long id, String password){
        String sql = "update sys_user set password=? where id=?";
        try(Connection conn = DruidUtil.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)){
             ps.setString(1,password);
             ps.setLong(2,id);
             ps.executeUpdate();
        }catch(SQLException e){
            throw new RuntimeException("修改密码失败",e);
        }
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
