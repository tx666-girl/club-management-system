package com.zeroone.star.dao.impl;

import com.zeroone.star.dao.ApplicationDao;
import com.zeroone.star.dao.ClubDao;
import com.zeroone.star.entity.Application;
import com.zeroone.star.util.DruidUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 入团申请 DAO 实现练习。
 *
 * <p>推荐顺序：先实现 createJoin、findByUser、findPending，
 * 最后完成 approve 和 reject。approve 必须使用事务。</p>
 *
 * <p>任务 E：申请提交和查询；任务 F：申请审批事务。</p>
 */
public class ApplicationDaoImpl implements ApplicationDao {

    @Override
    public void createJoin(long userId, long clubId) {
        //throw new UnsupportedOperationException("TODO E：请实现 ApplicationDao.createJoin");
        String sql = "INSERT INTO application (user_id, club_id,type,status,created_at) " +
                "VALUES (?, ?, 'JOIN', 'PENDING', CURRENT_TIMESTAMP) " +
                "ON DUPLICATE KEY UPDATE status = 'PENDING', reviewed_by = NULL, " +
                "reviewed_at = NULL, created_at = CURRENT_TIMESTAMP";
        try (Connection conn = DruidUtil.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 填写参数
            pstmt.setLong(1, userId);
            pstmt.setLong(2, clubId);

            // 执行更新
            pstmt.executeUpdate();

        }catch(SQLException e){
            e.printStackTrace();
            throw new RuntimeException("提交申请失败",e);
        }
    }

    @Override
    public List<Application> findByUser(long userId) {
        //throw new UnsupportedOperationException("TODO E：请实现 ApplicationDao.findByUser");
        List<Application> list = new ArrayList<>();

        String sql = "SELECT a.*, s.username AS applicant_name, c.`name` AS club_name " +
                "FROM application a " +
                "JOIN sys_user s ON a.user_id = s.id " +
                "JOIN club c ON c.id = a.club_id " +
                "WHERE a.user_id = ? " +
                "ORDER BY a.created_at DESC";
        try (Connection conn = DruidUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 填充参数
            pstmt.setLong(1, userId);

            // 执行查讯
            try (ResultSet rs = pstmt.executeQuery()) {
                // 遍历结果集，封装成Application对象
                while (rs.next()) {
                    Application application = new Application();
                    application.id = rs.getLong("id");
                    application.userId = rs.getLong("user_id");
                    application.clubId = rs.getLong("club_id");
                    application.type = rs.getString("type");
                    application.status = rs.getString("status");
                    application.createdAt = rs.getString("created_at");

                    // 设置额外查出来的别名字段
                    application.applicantName = rs.getString("applicant_name");
                    application.clubName = rs.getString("club_name");

                    list.add(application);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("查询用户申请列表失败", e);
        }
        return list;
    }

    @Override
    public List<Application> findPending() {
        List<Application> list = new ArrayList<>();
        //throw new UnsupportedOperationException("TODO E：请实现 ApplicationDao.findPending");
        String sql = "SELECT  a.*, s.username AS applicant_name, c.`name` AS club_name " +
                "FROM application a " +
                "JOIN sys_user s ON s.id = a.user_id " +
                "JOIN club c ON c.id = a.club_id " +
                "WHERE a.`status` = 'PENDING'" +
                "ORDER BY a.created_at";
        try (Connection conn = DruidUtil.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                // 执行查询
                while (rs.next()) {
                    // 遍历结果，封装对象
                    Application application = new Application();
                    application.id = rs.getLong("id");
                    application.userId = rs.getLong("user_id");
                    application.clubId = rs.getLong("club_id");
                    application.type = rs.getString("type");
                    application.status = rs.getString("status");
                    application.createdAt = rs.getString("created_at");

                    application.reviewedAt = rs.getString("reviewed_at");

                    // 设置JOIN查出来的展示名称
                    application.applicantName = rs.getString("applicant_name");
                    application.clubName = rs.getString("club_name");

                    list.add(application);

                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("查询待审批列表失败",e);
        }
        return list;
    }

    @Override
    public void approve(long applicationId, long adminId, ClubDao clubDao) {
        throw new UnsupportedOperationException("TODO F：请实现 ApplicationDao.approve");
    }

    @Override
    public void reject(long applicationId, long adminId) {
        throw new UnsupportedOperationException("TODO F：请实现 ApplicationDao.reject");
    }

    @Override
    public int countPending() {
        //throw new UnsupportedOperationException("TODO E：请实现 ApplicationDao.countPending");
        String sql = "SELECT COUNT(*) " +
                "FROM application " +
                "WHERE status = 'PENDING'";
        int count = 0;
        try(Connection conn = DruidUtil.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

            // 因为count返回结果只有一列
            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("统计待审批数量失败",e);
        }
        return count;
    }
}
