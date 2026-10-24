package com.zeroone.star.dao.impl;

import com.zeroone.star.dao.ActivityDao;
import com.zeroone.star.entity.Activity;

import java.util.List;
import java.util.ArrayList;

import com.zeroone.star.util.DruidUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * 活动 DAO 实现练习。
 *
 * <p>当前只练习活动浏览和管理员维护，不需要实现成员活动报名。</p>
 *
 * <p>本类属于任务 G。</p>
 */
public class ActivityDaoImpl implements ActivityDao {

    @Override
    public List<Activity> listByClub(long clubId) {
        //throw new UnsupportedOperationException("TODO G：请实现 ActivityDao.listByClub");

        List<Activity> list = new ArrayList<>();
        String sql = "SELECT a.id,a.club_id,a.club_name,a.title,a.description,a.start_time,a.location,u.username AS publisher_name," +
                "a.created_at,a.created_by" +
                "FROM activity a JOIN club c ON a.club_id = c.id" +
                "JOIN user u ON a.created_by = u.id" +
                "WHERE a.club_id = ? ORDER BY start_time ";
        try(Connection connection = DruidUtil.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1,clubId);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    Activity act = new Activity();
                    act.id = resultSet.getLong("id");
                    act.clubId = resultSet.getLong("club_id");
                    act.clubName = resultSet.getString("club_name");
                    act.title = resultSet.getString("title");
                    act.description = resultSet.getString("description");
                    act.startTime = resultSet.getString("start_time");
                    act.location = resultSet.getString("location");
                    act.publisherName = resultSet.getString("publisher_name");
                    act.createdAt = resultSet.getString("created_at");
                    act.createdBy = resultSet.getLong("created_by");
                    list.add(act);

                }
            }
        }catch(SQLException e){
            throw new RuntimeException("查询失败",e);
        }
        return list;
    }

    @Override
    public void create(long clubId, long userId, String title, String description,
                       String startTime, String location) {
        //throw new UnsupportedOperationException("TODO G：请实现 ActivityDao.create");
        String sql = "INSERT INTO activity(club_id,created_by,title,description,start_time        ,location)" +
                "VALUES (?,?,?,?,?,?)";
        try(Connection connection = DruidUtil.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1,clubId);
            statement.setLong(2,userId);
            statement.setString(3,title);
            statement.setString(4,description);
            statement.setTimestamp(5,Timestamp.valueOf(startTime));
            statement.setString(6,location);
            statement.executeUpdate();
        }catch(SQLException e){
            throw new RuntimeException("发布失败",e);
        }
    }

    @Override
    public void update(long id, String title, String description, String startTime, String location) {
        //throw new UnsupportedOperationException("TODO G：请实现 ActivityDao.update");

        String sql = "UPDATE activity SET title = ?,description = ?,start_time = ?,location = ? WHERE id = ?";
        try(Connection connection = DruidUtil.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1,title);
            statement.setString(2,description);
            statement.setTimestamp(3,Timestamp.valueOf(startTime));
            statement.setString(4,location);
            statement.setLong(5,id);
            statement.executeUpdate();

        }catch(SQLException e){
            throw new RuntimeException("修改失败",e);
        }
    }

    @Override
    public void delete(long id) {
        //throw new UnsupportedOperationException("TODO G：请实现 ActivityDao.delete");


        String sql = "DELETE FROM activity WHERE id = ?";
        try(Connection connection = DruidUtil.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1,id);
            statement.executeUpdate();
        }catch(SQLException e){
            throw new RuntimeException("删除失败",e);
        }
    }

    @Override
    public int countAll() {
        //throw new UnsupportedOperationException("TODO G：请实现 ActivityDao.countAll");

        int count = 0;
        String sql = "SELECT COUNT(*) FROM activity";
        try(Connection connection = DruidUtil.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)){
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                     count =  resultSet.getInt(1);
                }
            }
        }catch (SQLException e){
            throw new RuntimeException("查询失败",e);
        }
        return count;
    }
}
