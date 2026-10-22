package com.zeroone.star.dao;

import com.zeroone.star.entity.Application;

import java.util.List;

/**
 * 入团申请数据访问接口。
 *
 * <p>对应数据库表：{@code application}。</p>
 *
 * <p>状态约定：</p>
 * <pre>
 * PENDING   等待审批
 * APPROVED  已通过
 * REJECTED  已驳回
 * </pre>
 *
 * <p>审批操作会同时修改申请状态和社团成员数据，因此必须理解事务的使用。</p>
 */
public interface ApplicationDao {

    /**
     * 任务 E：提交一条入团申请。
     *
     * <p>写入内容：user_id、club_id、type='JOIN'、status='PENDING'。</p>
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>表上有 user_id + club_id + type 的唯一约束。</li>
     *   <li>被驳回后允许再次申请，可以使用 INSERT ... ON DUPLICATE KEY UPDATE。</li>
     *   <li>重复提交时把 status 重置为 PENDING，并清空 reviewed_by、reviewed_at。</li>
     *   <li>created_at 可重置为 CURRENT_TIMESTAMP。</li>
     * </ul>
     *
     * @param userId 申请人 id
     * @param clubId 目标社团 id
     */
    void createJoin(long userId, long clubId);

    /**
     * 任务 E：查询某个用户提交过的全部申请。
     *
     * <p>结果需要包含申请人和社团名称，供“我的申请”页面直接显示。</p>
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>application 分别 JOIN sys_user 和 club。</li>
     *   <li>用户姓名别名为 applicant_name，社团名别名为 club_name。</li>
     *   <li>过滤条件为 application.user_id = ?。</li>
     *   <li>按 created_at 倒序排列。</li>
     * </ul>
     *
     * @param userId 用户 id
     * @return 申请列表，没有记录时返回空集合
     */
    List<Application> findByUser(long userId);

    /**
     * 任务 E：查询所有待审批申请。
     *
     * <p>实现提示：过滤 status='PENDING'，按申请时间正序排列，
     * 并 JOIN 用户表和社团表取得展示名称。</p>
     *
     * @return 待审批申请列表
     */
    List<Application> findPending();

    /**
     * 任务 F：审批通过一条入团申请。
     *
     * <p>这是本 DAO 最重要的方法，必须使用事务：</p>
     * <ol>
     *   <li>关闭 connection 自动提交。</li>
     *   <li>使用 {@code SELECT ... FOR UPDATE} 锁定申请记录。</li>
     *   <li>确认申请存在且状态必须是 PENDING。</li>
     *   <li>把 status 改为 APPROVED，并写入 reviewed_by、reviewed_at。</li>
     *   <li>调用 {@code clubDao.addMember(connection, clubId, userId)}。</li>
     *   <li>全部成功后 commit。</li>
     *   <li>任何异常都 rollback，再把异常交给上层。</li>
     * </ol>
     *
     * <p>注意：不能在这里重新获取一条连接来新增成员，否则不是同一个事务。</p>
     *
     * @param applicationId 申请 id
     * @param adminId       执行审批的管理员 id
     * @param clubDao       用于在同一个事务中新增社团成员
     */
    void approve(long applicationId, long adminId, ClubDao clubDao);

    /**
     * 任务 F：驳回一条入团申请。
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>使用事务并锁定申请记录。</li>
     *   <li>只允许处理 PENDING 状态的申请。</li>
     *   <li>把 status 改为 REJECTED，并记录 reviewed_by、reviewed_at。</li>
     *   <li>驳回不能新增 club_member 记录。</li>
     * </ul>
     *
     * @param applicationId 申请 id
     * @param adminId       执行审批的管理员 id
     */
    void reject(long applicationId, long adminId);

    /**
     * 任务 E：统计待审批申请数量。
     *
     * <p>实现提示：{@code SELECT COUNT(*) FROM application WHERE status = 'PENDING'}。</p>
     *
     * @return 待审批数量
     */
    int countPending();
}
