package com.zeroone.star.dao.impl;

import com.zeroone.star.util.DruidUtil;
import com.zeroone.star.dao.AnnouncementDao;
import com.zeroone.star.entity.Announcement;

import java.util.List;

import java.util.ArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 * 公告 DAO 实现练习。
 *
 * <p>建议先完成 listByClub，再完成 create/update/delete，
 * 最后实现 latestForUser，因为它需要额外关联 club_member。</p>
 *
 * <p>本类属于任务 G。</p>
 */
public class AnnouncementDaoImpl implements AnnouncementDao {

    @Override
    public List<Announcement> listByClub(long clubId) {
        //throw new UnsupportedOperationException("TODO G：请实现 AnnouncementDao.listByClub");
        List<Announcement> list = new ArrayList<>();
        String sql = "SELECT a.id, a.club_id, c.name AS club_name, a.title, a.content, " +
                "a.created_by, u.name AS publisher_name, a.created_at " +
                "FROM announcement a " +
                "JOIN club c ON c.id = a.club_id " +
                "JOIN sys_user u ON u.id = a.created_by " +
                "WHERE a.club_id = ? ORDER BY a.created_at DESC, a.id DESC";
        try(Connection connection = DruidUtil.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1,clubId);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    Announcement ann = new Announcement();
                    ann.id = resultSet.getLong("id");
                    ann.clubId = resultSet.getLong("club_id");
                    ann.title = resultSet.getString("title");
                    ann.content = resultSet.getString("content");
                    ann.createdAt = resultSet.getString("created_at");
                    ann.clubName = resultSet.getString("club_name");
                    ann.publisherName = resultSet.getString("publisher_name");
                    ann.createdBy = resultSet.getLong("created_by");
                    list.add(ann);
                }
            }
        }catch(SQLException e){
            throw new RuntimeException("查询失败",e);
        }
        return list;


    }

    @Override
    public List<Announcement> latestForUser(long userId, int limit) {
        //throw new UnsupportedOperationException("TODO G：请实现 AnnouncementDao.latestForUser");
        List<Announcement> list = new ArrayList<>();
        String sql = "SELECT a.id, a.club_id, c.name AS club_name, a.title, a.content, " +
                "a.created_by, u.name AS publisher_name, a.created_at " +
                "FROM announcement a " +
                "JOIN club c ON c.id = a.club_id " +
                "JOIN sys_user u ON u.id = a.created_by " +
                "JOIN club_member cm ON cm.club_id = a.club_id " +
                "WHERE cm.user_id = ? ORDER BY a.created_at DESC, a.id DESC LIMIT ?";
        try(Connection connection = DruidUtil.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1,userId);
            statement.setInt(2,limit);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    Announcement ann = new Announcement();
                    ann.id = resultSet.getLong("id");
                    ann.clubId = resultSet.getLong("club_id");
                    ann.title = resultSet.getString("title");
                    ann.content = resultSet.getString("content");
                    ann.createdAt = resultSet.getString("created_at");
                    ann.clubName = resultSet.getString("club_name");
                    ann.publisherName = resultSet.getString("publisher_name");
                    ann.createdBy = resultSet.getLong("created_by");
                    list.add(ann);
                }
            }
        }catch(SQLException e){
            throw new RuntimeException("查询失败",e);
        }
        return list;

    }

    @Override
    public void create(long clubId, long userId, String title, String content) {
        //throw new UnsupportedOperationException("TODO G：请实现 AnnouncementDao.create");
        String sql = "INSERT INTO announcement (club_id,created_by,title,content) VALUES (?,?,?,?)";
        try(Connection connection = DruidUtil.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, clubId);
            statement.setLong(2, userId);
            statement.setString(3, title);
            statement.setString(4, content);
            statement.executeUpdate();

        }catch(SQLException e){
            throw new RuntimeException("发布公告失败",e);
        }

    }

    @Override
    public void update(long id, String title, String content) {
        //throw new UnsupportedOperationException("TODO G：请实现 AnnouncementDao.update");
        String sql = "UPDATE announcement Set title = ?,content = ? WHERE id = ?";
        try(Connection connection = DruidUtil.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1,title);
            statement.setString(2,content);
            statement.setLong(3,id);
            statement.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException("修改失败",e);
        }
    }

    @Override
    public void delete(long id) {
        //throw new UnsupportedOperationException("TODO G：请实现 AnnouncementDao.delete");
        String sql = "DELETE FROM announcement WHERE id = ?";
        try(Connection connection = DruidUtil.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1,id);
            statement.executeUpdate();
        }catch(SQLException e){
            throw new RuntimeException("删除失败",e);
        }

    }
}
