package com.zeroone.star.dao.impl;

import com.zeroone.star.dao.ApplicationDao;
import com.zeroone.star.dao.ClubDao;
import com.zeroone.star.entity.Application;

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
        throw new UnsupportedOperationException("TODO F：请实现 ApplicationDao.approve");
    }

    @Override
    public void reject(long applicationId, long adminId) {
        throw new UnsupportedOperationException("TODO F：请实现 ApplicationDao.reject");
    }

    @Override
    public int countPending() {
        throw new UnsupportedOperationException("TODO E：请实现 ApplicationDao.countPending");
    }
}
