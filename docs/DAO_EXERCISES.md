# DAO 分层实训练习

当前项目已经保留 Servlet、Entity、数据库和前端页面，
DAO 层处于“可编译但未实现”的状态。所有实现方法都会抛出：

```text
UnsupportedOperationException
```

这表示项目结构是完整的，但对应功能必须由同学完成 DAO 后才能运行。

## 一、八人分工

团队共 8 人：A-G 七人合力完成 DAO，所有接口测试、数据库验收、
代码评审和最终合并由第 8 人负责。

| 人员  | 负责方法 | 所在文件 | 完成后的功能 |
|-----|---|---|---|
| 北游  | `getUserByUsername`、`getUserById`、`getUrlCount` | `UserDaoImpl` | 登录时查询用户 |
| 小灰灰 | `createUser`、`updateProfile`、`updatePassword` | `UserDaoImpl` | 注册、修改资料、修改密码 |
| 小千  | `list`、`findById`、`joinedByUser`、`existsById`、`countAll`、`sumMemberCount` | `ClubDaoImpl` | 首页、分类、社团详情、统计 |
| 小梦  | `findMembers`、`addMember`、`removeMember`、`isMember` | `ClubDaoImpl` | 成员列表、加入、移除、数量维护 |
| 小茜  | `createJoin`、`findByUser`、`findPending`、`countPending` | `ApplicationDaoImpl` | 提交申请、我的申请、待审批列表 |
| 小树  | `approve`、`reject` | `ApplicationDaoImpl` | 审批通过或驳回的事务 |
| 小周  | 公告 DAO 全部方法与活动 DAO 全部方法 | `AnnouncementDaoImpl`、`ActivityDaoImpl` | 公告和活动的增删改查 |
| 李响宾 | 接口测试、数据库验收、代码评审、最终合并 | Postman、MySQL、Git | 每次合并前统一验收 |

A 和 B 共用一个实现文件，C 和 D、E 和 F 也一样。
因此按顺序开发和合并，后开始的人先拉取最新 `arth`，减少 Git 冲突。

## 二、共同规则

1. 不修改 DAO 接口中的方法名、参数和返回值。
2. 只修改自己任务对应的方法，不删除别人的方法骨架和接口注释。
3. 使用 `DruidUtil.getConnection()` 获取数据库连接。
4. 使用 `PreparedStatement` 和 `?` 传参。
5. 使用 try-with-resources 自动关闭连接和查询对象。
6. 查询列表时返回空集合，不要返回 `null`。
7. 查询单条数据不存在时，按接口注释返回 `null` 或 `Optional.empty()`。
8. SQLException 统一包装成 RuntimeException，并写清楚失败原因。
9. 涉及两张表同时修改时，必须使用事务、commit 和 rollback。
10. Servlet 中不能出现 SQL，SQL 只能出现在 DAO 实现类中。

通用 JDBC 结构：

```java
try (Connection connection = DruidUtil.getConnection();
     PreparedStatement statement = connection.prepareStatement("SQL语句")) {

    statement.setLong(1, someId);

    try (ResultSet resultSet = statement.executeQuery()) {
        // 读取结果并转换成实体对象
    }
} catch (SQLException e) {
    throw new RuntimeException("查询失败", e);
}
```

## 三、开发与合并顺序

推荐严格按以下顺序开发、验收和合并：

```text
1. A：用户查询
2. B：用户写入
3. C：社团查询
4. D：社团成员
5. E：申请提交与查询
6. F：申请审批事务
7. G：公告与活动
8. 验收负责人：完整接口联调
```

依赖原因：

```text
登录依赖用户查询
注册和修改资料依赖用户写入
社团页面依赖社团查询
申请审批同时依赖申请 DAO 和社团成员 DAO
公告、活动依赖登录用户和社团编号
```

每完成一项，只提交自己修改的方法。验收负责人确认接口和数据库结果后再合并，
下一位再从最新 `arth` 创建分支，避免几个人同时修改同一个 Java 文件。

## 四、逐项验收点

### A：用户查询

- `admin / 123456` 可以登录。
- 查询不存在的账号时返回 `null`。
- 用户字段能够完整映射到 User 对象。

### B：用户写入

- 注册新用户后，数据库中能看到新增记录。
- 新用户可以获得数据库生成的主键。
- 修改资料后，个人中心显示新内容。
- 输入正确原密码时可以修改密码。

### C：社团查询

- 首页返回 8 个社团。
- `category=tech` 只返回学术科技类社团。
- 已加入的社团 `joined=true`。
- 待审批的社团 `pending=true`。
- 社团详情和统计接口返回正确结果。

### D：社团成员

- 管理员查看成员列表时能看到成员。
- 新增成员时 `club_member` 和 `member_count` 同步变化。
- 移除成员后，两张表的数据同步变化。
- 重复加入不会生成重复成员记录。

### E：申请提交与查询

- 普通用户可以提交入团申请。
- 重复申请会回到 `PENDING`。
- 用户可以查询自己的申请。
- 管理员可以查询全部待审批申请。
- 待审批数量统计正确。

### F：申请审批事务

- 审批通过后，申请变成 `APPROVED`，并在 `club_member` 中新增成员。
- 审批驳回后，申请变成 `REJECTED`，不能增加成员。
- 已处理的申请不能重复审批。
- 任一步骤失败时，申请状态和成员记录都必须回滚。

`approve` 方法必须使用事务。推荐思路：

```text
关闭自动提交
查询并锁定 PENDING 申请
更新申请状态
调用 clubDao.addMember(connection, clubId, userId)
commit
出现任何异常时 rollback
```

### G：公告与活动

- 社团详情页显示对应公告和活动。
- 普通成员个人中心只显示已加入社团的公告。
- 管理员发布后，前端能立即刷新看到。
- 修改和删除后，数据库同步变化。
- 活动按开始时间显示，公告按发布时间倒序显示。

## 五、分支建议

每个人从最新 `arth` 创建独立分支：

```text
feature/dao-a-user-query
feature/dao-b-user-write
feature/dao-c-club-query
feature/dao-d-club-member
feature/dao-e-application-query
feature/dao-f-application-review
feature/dao-g-announcement-activity
```

每个人只修改任务表中的方法。验收负责人负责运行测试、检查数据库、
处理冲突和合并分支。

提交前至少执行：

```text
mvn resources:resources compile
```

只有编译成功并把分支推送到远程仓库后，才通知验收负责人测试。

## 六、验收负责人流程

第 8 人不需要编写 DAO，按以下步骤验收：

1. 检查提交是否只修改了指定方法。
2. 拉取分支并执行 `mvn resources:resources compile`。
3. 启动 `tomcat9:run`。
4. 用 Postman 或页面调用对应接口。
5. 用 Navicat 或 MySQL 检查真实数据。
6. 测试通过后合并到 `arth`。
7. 在合并后的最新 `arth` 上通知下一位开始开发。

## 七、任务完成标准

一个 A-G 任务视为完成，需要同时满足：

1. 自己负责的方法中不再出现 `UnsupportedOperationException`。
2. 自己负责的方法在接口测试中通过。
3. 数据库结果与页面结果一致。
4. 项目编译成功。
5. 验收负责人已完成测试和 Review。
6. 没有提交与本任务无关的 IDE 或本机环境文件。
