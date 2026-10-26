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

    @Override
    public List<Club> list(String category, long userId) {
        List<Club> list = new ArrayList<>();

        // 严格按照 schema.sql：主键是 id，中间表是 club_member，申请表是 application (status = 'PENDING', type = 'JOIN')
        StringBuilder sql = new StringBuilder(
                "SELECT c.*, " +
                        "  (SELECT COUNT(1) FROM club_member cm WHERE cm.club_id = c.id AND cm.user_id = ?) > 0 AS joined, " +
                        "  (SELECT COUNT(1) FROM application a WHERE a.club_id = c.id AND a.user_id = ? AND a.status = 'PENDING' AND a.type = 'JOIN') > 0 AS pending " +
                        "FROM club c WHERE 1=1 "
        );

        if (category != null && !category.trim().isEmpty()) {
            sql.append("AND c.category = ? ");
        }
        sql.append("ORDER BY c.created_at DESC");

        try (Connection connection = DruidUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            statement.setLong(paramIndex++, userId);
            statement.setLong(paramIndex++, userId);

            if (category != null && !category.trim().isEmpty()) {
                statement.setString(paramIndex, category);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapRowToClub(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询社团列表失败", e);
        }
        return list;
    }


    @Override
    public Optional<Club> findById(long clubId, long userId) {
        String sql = "SELECT c.*, " +
                "  (SELECT COUNT(1) FROM club_member cm WHERE cm.club_id = c.id AND cm.user_id = ?) > 0 AS joined, " +
                "  (SELECT COUNT(1) FROM application a WHERE a.club_id = c.id AND a.user_id = ? AND a.status = 'PENDING' AND a.type = 'JOIN') > 0 AS pending " +
                "FROM club c WHERE c.id = ?";

        try (Connection connection = DruidUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setLong(2, userId);
            statement.setLong(3, clubId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRowToClub(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询社团详情失败", e);
        }
        return Optional.empty();
    }


    @Override
    public List<Club> joinedByUser(long userId) {
        List<Club> list = new ArrayList<>();
        // 表名严格使用 club_member，主键使用 id
        String sql = "SELECT c.* FROM club c " +
                "INNER JOIN club_member cm ON c.id = cm.club_id " +
                "WHERE cm.user_id = ? " +
                "ORDER BY cm.joined_at DESC";

        try (Connection connection = DruidUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Club club = mapRowToClub(resultSet);
                    // 已经查出来的肯定是已加入的
                    club.setJoined(true);
                    list.add(club);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询已加入社团失败", e);
        }
        return list;
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
        String sql = "SELECT 1 FROM club WHERE id = ? LIMIT 1";
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
        String sql = "SELECT COUNT(*) FROM club";
        try (Connection connection = DruidUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("统计社团总数失败", e);
        }
        return 0;
    }

    @Override
    public int sumMemberCount() {
        // schema.sql 中 club 表有一个 member_count 字段用于缓存成员数量
        // 这里求和计算全平台的总成员数
        String sql = "SELECT SUM(member_count) FROM club";
        try (Connection connection = DruidUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                // 如果表是空的，SUM 会返回 NULL，所以要处理一下
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("统计总成员数失败", e);
        }
        return 0;
    }

    private Club mapRowToClub(ResultSet rs) throws SQLException {
        Club club = new Club();

        // 数据库字段 -> 实体类属性
        club.setId(rs.getLong("id"));
        club.setName(rs.getString("name"));
        club.setCategory(rs.getString("category"));
        club.setDescription(rs.getString("description"));
        club.setImageUrl(rs.getString("image_url"));
        club.setMemberCount(rs.getInt("member_count"));

        // 尝试读取 joined 和 pending 字段（如果是 list/findById 查出来的，就会包含这两个字段）
        try {
            club.setJoined(rs.getBoolean("joined"));
            club.setPending(rs.getBoolean("pending"));
        } catch (SQLException e) {
            // 忽略，防止在 joinedByUser 这种没有 select joined 的 SQL 里报错
        }

        return club;
    }

}
