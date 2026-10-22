package com.zeroone.star.dao;

import com.zeroone.star.entity.Announcement;

import java.util.List;

/**
 * 社团公告数据访问接口。
 *
 * <p>对应数据库表：{@code announcement}。</p>
 *
 * <p>公告列表需要 JOIN club 和 sys_user，分别取得社团名称和发布人姓名。</p>
 */
public interface AnnouncementDao {

    /**
     * 任务 G：查询一个社团的全部公告。
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>announcement JOIN club JOIN sys_user。</li>
     *   <li>过滤 announcement.club_id = ?。</li>
     *   <li>按 created_at 倒序，再按 id 倒序。</li>
     *   <li>把 created_by 对应用户的 name 放入 publisherName。</li>
     * </ul>
     *
     * @param clubId 社团 id
     * @return 公告列表
     */
    List<Announcement> listByClub(long clubId);

    /**
     * 任务 G：查询用户已加入社团的最新公告。
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>announcement JOIN club_member，条件为 club_member.user_id = ?。</li>
     *   <li>按发布时间倒序。</li>
     *   <li>使用 {@code LIMIT ?} 限制返回数量。</li>
     *   <li>同一个用户加入多个社团时，公告结果会混在一起，这是个人中心需要的行为。</li>
     * </ul>
     *
     * @param userId 用户 id
     * @param limit  最多返回条数
     * @return 最新公告列表
     */
    List<Announcement> latestForUser(long userId, int limit);

    /**
     * 任务 G：发布一条公告。
     *
     * <p>写入字段：club_id、title、content、created_by。</p>
     *
     * <p>实现提示：使用 INSERT，created_at 由数据库默认值生成。发布人 id 来自当前管理员。</p>
     *
     * @param clubId  社团 id
     * @param userId  发布公告的管理员 id
     * @param title   公告标题
     * @param content 公告内容
     */
    void create(long clubId, long userId, String title, String content);

    /**
     * 任务 G：修改公告标题和内容。
     *
     * <p>实现提示：UPDATE announcement SET title = ?, content = ? WHERE id = ?。</p>
     *
     * @param id      公告 id
     * @param title   新标题
     * @param content 新内容
     */
    void update(long id, String title, String content);

    /**
     * 任务 G：删除公告。
     *
     * <p>实现提示：DELETE FROM announcement WHERE id = ?。</p>
     *
     * @param id 公告 id
     */
    void delete(long id);
}
