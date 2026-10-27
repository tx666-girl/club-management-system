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
        if (clubDao == null) {
            throw new IllegalArgumentException("clubDao 不能为空");
        }

        Connection connection = null;
        try {
            // 1. 取连接并开启事务。从这里开始，后面的 SQL 都属于同一个事务
            connection = DruidUtil.getConnection();
            connection.setAutoCommit(false);

            long clubId;
            long userId;

            // 2. 锁定申请记录。FOR UPDATE 必须在 setAutoCommit(false) 之后执行，
            //    否则语句一结束锁就释放，挡不住并发重复审批
            String lockSql = "SELECT club_id, user_id, status "
                    + "FROM application WHERE id = ? FOR UPDATE";
            try (PreparedStatement statement = connection.prepareStatement(lockSql)) {
                statement.setLong(1, applicationId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new IllegalArgumentException("申请不存在：" + applicationId);
                    }
                    clubId = resultSet.getLong("club_id");
                    userId = resultSet.getLong("user_id");
                    String status = resultSet.getString("status");
                    if (!"PENDING".equals(status)) {
                        throw new IllegalStateException("该申请已处理，当前状态：" + status);
                    }
                }
            }

            // 3. 更新申请状态。再带一次 status = 'PENDING' 作为兜底条件
            String updateSql = "UPDATE application SET status = 'APPROVED', "
                    + "reviewed_by = ?, reviewed_at = CURRENT_TIMESTAMP "
                    + "WHERE id = ? AND status = 'PENDING'";
            try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                statement.setLong(1, adminId);
                statement.setLong(2, applicationId);
                if (statement.executeUpdate() != 1) {
                    throw new IllegalStateException("申请状态已被修改，请刷新后重试");
                }
            }

            // 4. 在同一个事务里新增社团成员。
            //    必须把 connection 传进去，不能让 addMember 自己再取一条连接
            boolean inserted = clubDao.addMember(connection, clubId, userId);
            if (!inserted) {
                // 用户已经是社团成员：不重复加人、member_count 也不加，
                // 但这条申请仍然算审批通过
                System.out.println("用户 " + userId + " 已在社团 " + clubId + " 中，跳过新增成员");
            }

            // 5. 全部成功才提交
            connection.commit();
        } catch (Exception e) {
            // 任何一步失败都回滚，保证申请状态和成员记录一起撤销
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    // 回滚本身失败时只挂附属信息，不能盖掉原始异常
                    e.addSuppressed(rollbackException);
                }
            }
            // 保留原始异常类型：IllegalArgumentException 对应 400，
            // IllegalStateException 对应 409，由 ApiServlet 统一翻译
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("审批入团申请失败", e);
        } finally {
            if (connection != null) {
                try {
                    // 归还连接池前恢复自动提交
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // 恢复失败不影响已经提交或回滚的结果
                }
                try {
                    connection.close();
                } catch (SQLException ignored) {
                    // 关闭失败交给连接池处理
                }
            }
        }
    }

    @Override
    public void reject(long applicationId, long adminId) {
        Connection connection = null;
        try {
            // 1. 取连接并开启事务
            connection = DruidUtil.getConnection();
            connection.setAutoCommit(false);

            // 2. 锁定申请记录。FOR UPDATE 必须在 setAutoCommit(false) 之后执行，
            //    否则语句一结束锁就释放，挡不住并发重复审批
            String lockSql = "SELECT status FROM application WHERE id = ? FOR UPDATE";
            try (PreparedStatement statement = connection.prepareStatement(lockSql)) {
                statement.setLong(1, applicationId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new IllegalArgumentException("申请不存在：" + applicationId);
                    }
                    String status = resultSet.getString("status");
                    if (!"PENDING".equals(status)) {
                        throw new IllegalStateException("该申请已处理，当前状态：" + status);
                    }
                }
            }

            // 3. 更新申请状态为已驳回，并记录审批人和时间
            String updateSql = "UPDATE application SET status = 'REJECTED', "
                    + "reviewed_by = ?, reviewed_at = CURRENT_TIMESTAMP "
                    + "WHERE id = ? AND status = 'PENDING'";
            try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                statement.setLong(1, adminId);
                statement.setLong(2, applicationId);
                if (statement.executeUpdate() != 1) {
                    throw new IllegalStateException("申请状态已被修改，请刷新后重试");
                }
            }

            // 4. 驳回只改申请状态，不新增 club_member，也不动 member_count
            connection.commit();
        } catch (Exception e) {
            // 任何一步失败都回滚
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    // 回滚本身失败时只挂附属信息，不能盖掉原始异常
                    e.addSuppressed(rollbackException);
                }
            }
            // 保留原始异常类型：IllegalArgumentException 对应 400，
            // IllegalStateException 对应 409，由 ApiServlet 统一翻译
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("驳回入团申请失败", e);
        } finally {
            if (connection != null) {
                try {
                    // 归还连接池前恢复自动提交
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // 恢复失败不影响已经提交或回滚的结果
                }
                try {
                    connection.close();
                } catch (SQLException ignored) {
                    // 关闭失败交给连接池处理
                }
            }
        }
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
