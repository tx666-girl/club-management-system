package com.zeroone.star.dao.impl;

import com.zeroone.star.dao.ClubDao;
import com.zeroone.star.entity.Club;
import com.zeroone.star.entity.Member;
import com.zeroone.star.util.DruidUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    private static final String BASE_SELECT =
            "SELECT c.id, c.name, c.category, c.description, c.image_url, c.member_count, " +
            "EXISTS(SELECT 1 FROM club_member cm " +
            "WHERE cm.club_id = c.id AND cm.user_id = ?) AS joined, " +
            "EXISTS(SELECT 1 FROM application a " +
            "WHERE a.club_id = c.id AND a.user_id = ? " +
            "AND a.type = 'JOIN' AND a.status = 'PENDING') AS pending " +
            "FROM club c ";

    @Override
    public List<Club> list(String category, long userId) {
        boolean hasCategory = category != null && !category.trim().isEmpty();
        String sql = BASE_SELECT + (hasCategory ? "WHERE c.category = ? " : "") + "ORDER BY c.id";

        try (Connection connection = DruidUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setLong(index++, userId);
            statement.setLong(index++, userId);
            if (hasCategory) {
                statement.setString(index, category);
            }

            List<Club> clubs = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    clubs.add(mapClub(resultSet));
                }
            }
            return clubs;
        } catch (SQLException e) {
            throw new RuntimeException("查询社团列表失败", e);
        }
    }

    @Override
    public Optional<Club> findById(long clubId, long userId) {
        String sql = BASE_SELECT + "WHERE c.id = ?";

        try (Connection connection = DruidUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, userId);
            statement.setLong(3, clubId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapClub(resultSet))
                        : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询社团详情失败", e);
        }
    }

    @Override
    public List<Club> joinedByUser(long userId) {
        String sql =
                "SELECT c.id, c.name, c.category, c.description, c.image_url, c.member_count, " +
                "TRUE AS joined, FALSE AS pending " +
                "FROM club c JOIN club_member cm ON cm.club_id = c.id " +
                "WHERE cm.user_id = ? ORDER BY c.id";

        try (Connection connection = DruidUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);

            List<Club> clubs = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    clubs.add(mapClub(resultSet));
                }
            }
            return clubs;
        } catch (SQLException e) {
            throw new RuntimeException("查询已加入社团失败", e);
        }
    }

    @Override
    public List<Member> findMembers(long clubId) {
        String sql = "SELECT u.id, u.username, u.name, u.student_no, cm.joined_at "
                + "FROM club_member cm "
                + "JOIN sys_user u ON u.id = cm.user_id "
                + "WHERE cm.club_id = ? "
                + "ORDER BY cm.joined_at, u.id";

        List<Member> members = new ArrayList<>();

        try (Connection connection = DruidUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, clubId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Member member = new Member();
                    member.userId = resultSet.getLong("id");
                    member.username = resultSet.getString("username");
                    member.name = resultSet.getString("name");
                    member.studentNo = resultSet.getString("student_no");
                    member.joinedAt = resultSet.getString("joined_at");
                    members.add(member);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询社团成员失败", e);
        }

        return members;
    }

    @Override
    public void removeMember(long clubId, long userId) {
        String deleteSql = "DELETE FROM club_member WHERE club_id = ? AND user_id = ?";
        String updateSql = "UPDATE club SET member_count = GREATEST(member_count - 1, 0) WHERE id = ?";

        try (Connection connection = DruidUtil.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                int deleted;
                try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
                    statement.setLong(1, clubId);
                    statement.setLong(2, userId);
                    deleted = statement.executeUpdate();
                }

                if (deleted > 0) {
                    try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                        statement.setLong(1, clubId);
                        statement.executeUpdate();
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new RuntimeException("移除社团成员失败", e);
        }
    }

    @Override
    public boolean addMember(Connection connection, long clubId, long userId) throws SQLException {
        String insertSql = "INSERT IGNORE INTO club_member (club_id, user_id) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            statement.setLong(1, clubId);
            statement.setLong(2, userId);

            int inserted = statement.executeUpdate();
            if (inserted == 0) {
                // 已经是成员了，直接返回 false，不增加计数
                return false;
            }
        }

        String updateSql = "UPDATE club SET member_count = member_count + 1 WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
            statement.setLong(1, clubId);
            statement.executeUpdate();
        }

        return true;
    }

    @Override
    public boolean isMember(long clubId, long userId) {
        String sql = "SELECT 1 FROM club_member WHERE club_id = ? AND user_id = ? LIMIT 1";

        try (Connection connection = DruidUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, clubId);
            statement.setLong(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("判断社团成员失败", e);
        }
    }

    @Override
    public boolean existsById(long clubId) {
        String sql = "SELECT 1 FROM club WHERE id = ?";
        try (Connection connection = DruidUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, clubId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("判断社团是否存在失败", e);
        }
    }

    @Override
    public int countAll() {
        return count("SELECT COUNT(*) FROM club");
    }

    @Override
    public int sumMemberCount() {
        return count("SELECT COALESCE(SUM(member_count), 0) FROM club");
    }

    private int count(String sql) {
        try (Connection connection = DruidUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("统计社团数据失败", e);
        }
    }

    private Club mapClub(ResultSet resultSet) throws SQLException {
        Club club = new Club();
        club.id = resultSet.getLong("id");
        club.name = resultSet.getString("name");
        club.category = resultSet.getString("category");
        club.description = resultSet.getString("description");
        club.imageUrl = resultSet.getString("image_url");
        club.memberCount = resultSet.getInt("member_count");
        club.joined = resultSet.getBoolean("joined");
        club.pending = resultSet.getBoolean("pending");
        return club;
    }
}
