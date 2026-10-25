package com.zeroone.star.dao.impl;

import com.zeroone.star.dao.ApplicationDao;
import com.zeroone.star.dao.ClubDao;
import com.zeroone.star.entity.Application;
import com.zeroone.star.util.DruidUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        throw new UnsupportedOperationException("TODO E：请实现 ApplicationDao.createJoin");
    }

    @Override
    public List<Application> findByUser(long userId) {
        throw new UnsupportedOperationException("TODO E：请实现 ApplicationDao.findByUser");
    }

    @Override
    public List<Application> findPending() {
        throw new UnsupportedOperationException("TODO E：请实现 ApplicationDao.findPending");
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
        throw new UnsupportedOperationException("TODO E：请实现 ApplicationDao.countPending");
    }
}
