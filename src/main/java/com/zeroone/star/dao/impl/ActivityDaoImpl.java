package com.zeroone.star.dao.impl;

import com.zeroone.star.dao.ActivityDao;
import com.zeroone.star.entity.Activity;

import java.util.List;

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
        throw new UnsupportedOperationException("TODO G：请实现 ActivityDao.listByClub");
    }

    @Override
    public void create(long clubId, long userId, String title, String description,
                       String startTime, String location) {
        throw new UnsupportedOperationException("TODO G：请实现 ActivityDao.create");
    }

    @Override
    public void update(long id, String title, String description, String startTime, String location) {
        throw new UnsupportedOperationException("TODO G：请实现 ActivityDao.update");
    }

    @Override
    public void delete(long id) {
        throw new UnsupportedOperationException("TODO G：请实现 ActivityDao.delete");
    }

    @Override
    public int countAll() {
        throw new UnsupportedOperationException("TODO G：请实现 ActivityDao.countAll");
    }
}
