package com.zeroone.star.dao;

import com.zeroone.star.entity.Activity;

import java.util.List;

/**
 * 社团活动数据访问接口。
 *
 * <p>对应数据库表：{@code activity}。当前只实现活动浏览和管理员维护，
 * 不包含成员活动报名。</p>
 */
public interface ActivityDao {

    /**
     * 任务 G：查询一个社团的全部活动。
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>activity JOIN club JOIN sys_user。</li>
     *   <li>过滤 activity.club_id = ?。</li>
     *   <li>按 start_time 正序排列，让近期活动更容易查看。</li>
     *   <li>把发布人姓名放入 publisherName。</li>
     * </ul>
     *
     * @param clubId 社团 id
     * @return 活动列表
     */
    List<Activity> listByClub(long clubId);

    /**
     * 任务 G：发布一条活动。
     *
     * <p>写入字段：club_id、title、description、start_time、location、created_by。</p>
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>startTime 格式由页面提供为 {@code yyyy-MM-dd HH:mm:ss}。</li>
     *   <li>可以直接 setString，但更规范的做法是使用 Timestamp.valueOf(startTime)。</li>
     *   <li>created_at 使用数据库默认值。</li>
     * </ul>
     *
     * @param clubId      社团 id
     * @param userId      发布活动的管理员 id
     * @param title       活动标题
     * @param description 活动介绍
     * @param startTime   开始时间
     * @param location    活动地点
     */
    void create(long clubId, long userId, String title, String description,
                String startTime, String location);

    /**
     * 任务 G：修改活动内容。
     *
     * <p>允许修改 title、description、start_time、location。</p>
     *
     * <p>实现提示：使用 UPDATE activity SET ... WHERE id = ?。</p>
     *
     * @param id          活动 id
     * @param title       新标题
     * @param description 新介绍
     * @param startTime   新开始时间
     * @param location    新地点
     */
    void update(long id, String title, String description, String startTime, String location);

    /**
     * 任务 G：删除活动。
     *
     * <p>实现提示：DELETE FROM activity WHERE id = ?。</p>
     *
     * @param id 活动 id
     */
    void delete(long id);

    /**
     * 任务 G：统计全部活动数量。
     *
     * <p>实现提示：{@code SELECT COUNT(*) FROM activity}。</p>
     *
     * @return 活动总数
     */
    int countAll();
}
