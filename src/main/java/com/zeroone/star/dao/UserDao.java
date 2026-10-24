package com.zeroone.star.dao;

import com.zeroone.star.entity.User;

/**
 * 用户数据访问接口。
 *
 * <p>对应数据库表：{@code sys_user}。</p>
 *
 * <p>实现要求：</p>
 * <ol>
 *   <li>所有 SQL 必须写在 {@code UserDaoImpl} 中，接口只负责描述行为。</li>
 *   <li>使用 {@code DruidUtil.getConnection()} 获取连接。</li>
 *   <li>使用 try-with-resources 自动关闭 Connection、PreparedStatement 和 ResultSet。</li>
 *   <li>SQL 参数一律使用 {@code ?} 和 {@code PreparedStatement}，不要拼接字符串。</li>
 *   <li>SQLException 应包装成 RuntimeException，并附带能看懂的中文提示。</li>
 * </ol>
 */
public interface UserDao {

    /**
     * 任务 A：根据登录账号查询用户。
     *
     * <p>使用场景：登录接口接收到 username 和 password 后，先通过本方法查询用户。</p>
     *
     * <p>需要查询的字段：</p>
     * <pre>
     * id, username, password, name, student_no, grade, role, status, created_at
     * </pre>
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>SQL 条件使用 {@code WHERE username = ?}。</li>
     *   <li>调用 {@code ResultSet.next()} 判断是否查到用户。</li>
     *   <li>查到用户时，把每一列赋值给 User 对象并返回。</li>
     *   <li>没有查到用户时返回 {@code null}，不要抛出“用户不存在”异常。</li>
     *   <li>created_at 是 Timestamp，可以转成字符串后放入 User。</li>
     * </ul>
     *
     * @param username 登录账号
     * @return 查询到的用户；账号不存在时返回 null
     */
    User getUserByUsername(String username);

    /**
     * 任务 A：根据用户主键查询用户。
     *
     * <p>使用场景：已知用户 id 时查询完整用户资料。</p>
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>条件使用 {@code WHERE id = ?}。</li>
     *   <li>查询字段与 getUserByUsername 保持一致。</li>
     *   <li>可以把 ResultSet 到 User 的转换抽成一个私有 mapUser 方法，避免重复代码。</li>
     * </ul>
     *
     * @param id 用户主键
     * @return 用户对象；不存在时返回 null
     */
    User getUserById(long id);

    /**
     * 任务 B：新增一个用户并返回数据库自动生成的主键。
     *
     * <p>需要写入的字段：</p>
     * <pre>
     * username, password, name, student_no, grade, role, status
     * </pre>
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>使用 INSERT INTO 语句，字段值全部写成 {@code ?}。</li>
     *   <li>调用 {@code prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)}。</li>
     *   <li>执行 executeUpdate 后，通过 {@code getGeneratedKeys()} 取得新 id。</li>
     *   <li>没有取得主键时，应抛出 SQLException 或 RuntimeException。</li>
     * </ul>
     *
     * @param user 待新增用户，需要包含所有必填字段
     * @return 数据库生成的新用户 id
     */
    long createUser(User user);

    /**
     * 任务 B：修改用户的基础资料。
     *
     * <p>允许修改的字段：</p>
     * <pre>
     * name, student_no, grade
     * </pre>
     *
     * <p>实现提示：</p>
     * <ul>
     *   <li>使用 UPDATE sys_user SET ... WHERE id = ?。</li>
     *   <li>不要把 username、role 等字段放进本方法。</li>
     *   <li>参数顺序要与 SQL 中的问号顺序完全一致。</li>
     * </ul>
     *
     * @param id        要修改的用户 id
     * @param name      新姓名
     * @param studentNo 新学号
     * @param grade     新年级
     */
    void updateProfile(long id, String name, String studentNo, String grade);

    /**
     * 任务 B：修改用户密码。
     *
     * <p>使用场景：用户在个人中心输入原密码和新密码，业务层校验原密码后调用本方法。</p>
     *
     * <p>实现提示：SQL 使用 UPDATE sys_user SET password = ? WHERE id = ?。</p>
     *
     * @param id       用户 id
     * @param password 新密码
     */
    void updatePassword(long id, String password);

    /**
     * 任务 A：查询用户对指定 URL 的访问计数。
     *
     * <p>这是旧版页面权限逻辑保留的兼容方法。当前系统主要通过 role 判断管理员权限。</p>
     *
     * <p>实现提示：如果当前阶段不练习页面权限，可以在实现类中暂时返回 1；
     * 如果继续扩展权限功能，应新增权限表，再根据 username 和 url 查询。</p>
     *
     * @param username 用户账号
     * @param url      页面地址
     * @return 当前兼容实现返回 1
     */
    Integer getUrlCount(String username, String url);
}
