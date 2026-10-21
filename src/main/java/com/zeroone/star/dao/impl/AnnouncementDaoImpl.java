package com.zeroone.star.dao.impl;

import com.zeroone.star.dao.AnnouncementDao;
import com.zeroone.star.entity.Announcement;

import java.util.List;

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
        throw new UnsupportedOperationException("TODO G：请实现 AnnouncementDao.listByClub");
    }

    @Override
    public List<Announcement> latestForUser(long userId, int limit) {
        throw new UnsupportedOperationException("TODO G：请实现 AnnouncementDao.latestForUser");
    }

    @Override
    public void create(long clubId, long userId, String title, String content) {
        throw new UnsupportedOperationException("TODO G：请实现 AnnouncementDao.create");
    }

    @Override
    public void update(long id, String title, String content) {
        throw new UnsupportedOperationException("TODO G：请实现 AnnouncementDao.update");
    }

    @Override
    public void delete(long id) {
        throw new UnsupportedOperationException("TODO G：请实现 AnnouncementDao.delete");
    }
}
