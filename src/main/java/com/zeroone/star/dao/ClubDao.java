package com.zeroone.star.dao;

import com.zeroone.star.entity.Club;
import com.zeroone.star.entity.Member;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * 社团和社团成员数据访问接口。
 *
 * <p>涉及数据库表：</p>
 * <pre>
 * club
 * club_member
 * application
 * </pre>
 *
 * <p>关系说明：</p>
 * <ul>
 *   <li>club_member 是用户和社团之间的中间表。</li>
 *   <li>首页需要同时查出社团信息和当前用户是否已加入、是否正在申请。</li>
 *   <li>club.member_count 是成员数量缓存，增加或删除成员时要同步修改。</li>
 * </ul>
 *
 * <p>通用实现提示：使用 DruidUtil 获取连接，使用 PreparedStatement 传参，
 * 使用 try-with-resources 关闭 JDBC 资源。</p>
 */
public interface ClubDao {

    /**
     * 任务 C：查询社团列表，可按分类筛选。
     *
     * <p>返回的每个 Club 还需要包含：</p>
     * <ul>
     *   <li>joined：当前用户是否已加入该社团。</li>
     *   <li>pending：当前用户是否有该社团的待审批申请。</li>
     * </ul>
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>基础数据来自 club 表。</li>
     *   <li>joined 可以用 EXISTS 查询 club_member。</li>
     *   <li>pending 可以用 EXISTS 查询 application，条件包含 user_id、club_id、
     *       type='JOIN' 和 status='PENDING'。</li>
     *   <li>category 为空或 null 时查询全部；否则增加 {@code WHERE c.category = ?}。</li>
     *   <li>结果按 club.id 排序，保证页面顺序稳定。</li>
     * </ul>
     *
     * @param category 分类值，可为空
     * @param userId   当前登录用户 id
     * @return 社团列表，没有数据时返回空集合而不是 null
     */
    List<Club> list(String category, long userId);

    /**
     * 任务 C：根据社团 id 查询社团详情。
     *
     * <p>详情同样要包含 joined 和 pending，供社团详情页显示当前用户状态。</p>
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>使用 {@code WHERE c.id = ?}。</li>
     *   <li>找到记录时用 {@code Optional.of(club)} 返回。</li>
     *   <li>没有记录时返回 {@code Optional.empty()}。</li>
     * </ul>
     *
     * @param clubId 社团 id
     * @param userId 当前用户 id
     * @return 社团详情；不存在时为空 Optional
     */
    Optional<Club> findById(long clubId, long userId);

    /**
     * 任务 C：查询某个用户已经加入的全部社团。
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>使用 club JOIN club_member。</li>
     *   <li>过滤条件为 club_member.user_id = ?。</li>
     *   <li>这些结果的 joined 固定为 true，pending 固定为 false。</li>
     * </ul>
     *
     * @param userId 用户 id
     * @return 用户已加入的社团列表
     */
    List<Club> joinedByUser(long userId);

    /**
     * 任务 D：查询某个社团的全部成员。
     *
     * <p>需要返回的 Member 字段：</p>
     * <pre>
     * userId, username, name, studentNo, joinedAt
     * </pre>
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>使用 club_member JOIN sys_user。</li>
     *   <li>过滤条件为 club_member.club_id = ?。</li>
     *   <li>按 joined_at 和 user id 排序。</li>
     * </ul>
     *
     * @param clubId 社团 id
     * @return 社团成员列表
     */
    List<Member> findMembers(long clubId);

    /**
     * 任务 D：把用户移出社团。
     *
     * <p>实现必须使用事务：</p>
     * <ol>
     *   <li>关闭自动提交。</li>
     *   <li>删除 club_member 中对应的 club_id 和 user_id。</li>
     *   <li>如果确实删除了一条记录，把 club.member_count 减 1，且不能小于 0。</li>
     *   <li>全部成功才 commit，出现异常必须 rollback。</li>
     * </ol>
     *
     * <p>实现提示：可以使用 {@code GREATEST(member_count - 1, 0)} 防止出现负数。</p>
     *
     * @param clubId 社团 id
     * @param userId 用户 id
     */
    void removeMember(long clubId, long userId);

    /**
     * 任务 D：使用外部事务连接新增社团成员。
     *
     * <p>该方法专门供 ApplicationDao 审批入团申请时调用。
     * 审批和新增成员必须位于同一个事务中，所以本方法不能自己获取或关闭连接。</p>
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>执行 INSERT IGNORE INTO club_member (club_id, user_id) VALUES (?, ?)。</li>
     *   <li>如果插入成功，把 club.member_count 加 1。</li>
     *   <li>没有插入时返回 false，说明用户已经在社团中。</li>
     *   <li>不要调用 connection.close()，连接由 ApplicationDao 管理。</li>
     * </ul>
     *
     * @param connection 由调用方提供并管理的事务连接
     * @param clubId     社团 id
     * @param userId     用户 id
     * @return 是否真正新增了成员
     * @throws SQLException SQL 执行失败时交给调用方回滚事务
     */
    boolean addMember(Connection connection, long clubId, long userId) throws SQLException;

    /**
     * 任务 D：判断用户是否已经是社团成员。
     *
     * <p>实现提示：查询 club_member 是否存在对应记录，使用 ResultSet.next() 返回 boolean。</p>
     *
     * @param clubId 社团 id
     * @param userId 用户 id
     * @return 已加入返回 true
     */
    boolean isMember(long clubId, long userId);

    /**
     * 任务 C：判断社团是否存在。
     *
     * <p>实现提示：查询 club 表主键，使用 ResultSet.next() 判断。</p>
     *
     * @param clubId 社团 id
     * @return 存在返回 true
     */
    boolean existsById(long clubId);

    /**
     * 任务 C：统计社团总数。
     *
     * <p>实现提示：{@code SELECT COUNT(*) FROM club}，读取第一列整数。</p>
     *
     * @return 社团总数
     */
    int countAll();

    /**
     * 任务 C：统计所有社团的成员数量之和。
     *
     * <p>实现提示：使用 {@code COALESCE(SUM(member_count), 0)}，防止没有社团时返回 null。</p>
     *
     * @return 成员数量合计
     */
    int sumMemberCount();
}
