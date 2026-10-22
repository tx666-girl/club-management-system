package com.zeroone.star.dao.impl;

import com.zeroone.star.dao.ClubDao;
import com.zeroone.star.entity.Club;
import com.zeroone.star.entity.Member;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * 社团和成员 DAO 实现练习。
 *
 * <p>实现时建议先完成 list 和 findById，因为社团首页和详情页都依赖它们。
 * removeMember 和 addMember 涉及 club.member_count，必须和成员表一起维护。</p>
 *
 * <p>任务 C：社团查询和统计；任务 D：成员管理。</p>
 */
public class ClubDaoImpl implements ClubDao {

    @Override
    public List<Club> list(String category, long userId) {
        throw new UnsupportedOperationException("TODO C：请实现 ClubDao.list");
    }

    @Override
    public Optional<Club> findById(long clubId, long userId) {
        throw new UnsupportedOperationException("TODO C：请实现 ClubDao.findById");
    }

    @Override
    public List<Club> joinedByUser(long userId) {
        throw new UnsupportedOperationException("TODO C：请实现 ClubDao.joinedByUser");
    }

    @Override
    public List<Member> findMembers(long clubId) {
        throw new UnsupportedOperationException("TODO D：请实现 ClubDao.findMembers");
    }

    @Override
    public void removeMember(long clubId, long userId) {
        throw new UnsupportedOperationException("TODO D：请实现 ClubDao.removeMember");
    }

    @Override
    public boolean addMember(Connection connection, long clubId, long userId) throws SQLException {
        throw new UnsupportedOperationException("TODO D：请实现 ClubDao.addMember");
    }

    @Override
    public boolean isMember(long clubId, long userId) {
        throw new UnsupportedOperationException("TODO D：请实现 ClubDao.isMember");
    }

    @Override
    public boolean existsById(long clubId) {
        throw new UnsupportedOperationException("TODO C：请实现 ClubDao.existsById");
    }

    @Override
    public int countAll() {
        throw new UnsupportedOperationException("TODO C：请实现 ClubDao.countAll");
    }

    @Override
    public int sumMemberCount() {
        throw new UnsupportedOperationException("TODO C：请实现 ClubDao.sumMemberCount");
    }
}
