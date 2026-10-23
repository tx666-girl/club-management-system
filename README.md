# 社团管理系统

这是一个前后端放在同一个 Maven 项目中的 Java Web 示例。

## 使用技术

- Java 8 语法
- Servlet 4.0
- JDBC
- Druid 连接池
- MySQL 5.7
- HTML、CSS、原生 JavaScript
- Maven + Tomcat 9

项目没有使用 Spring、Vue、JSP 等额外框架。

## DAO 实训模式

当前 `dao` 目录只保留接口，`dao/impl` 目录保留实现类骨架。
所有 DAO 方法尚未实现，会抛出 `UnsupportedOperationException`。

接口中已经写清参数、返回值、数据库字段和实现提示。
八人团队分工、开发顺序和验收标准见：

```text
docs/DAO_EXERCISES.md
```

## 目录

```text
club-management-system
├─ database
│  ├─ schema.sql          创建数据库和 6 张业务表
│  └─ seed.sql            写入演示账号、社团、公告和活动
├─ src/main/java
│  └─ com/zeroone/star
│     ├─ dao              DAO 接口
│     ├─ dao/impl         DAO 的 JDBC 实现
│     ├─ entity           普通 Java 数据类
│     ├─ filter           会话和页面访问过滤
│     ├─ servlet          统一 JSON API
│     └─ util             数据源和 JSON 工具
├─ src/main/resources
│  └─ jdbc.properties     本机数据库连接配置
└─ src/main/webapp
   ├─ index.html          跳到登录页
   └─ prototype           全部前端页面、样式和 JavaScript
```

## 第一次运行

1. 启动本机 MySQL。
2. 在 `src/main/resources/jdbc.properties` 中填写本机数据库账号和密码。
3. 在项目根目录打开 MySQL 客户端，依次执行：

```sql
SOURCE database/schema.sql;
SOURCE database/seed.sql;
```

4. 在 IDEA 中运行 Maven 命令：

```text
tomcat9:run
```

5. 浏览器打开：

```text
http://localhost:9000/
```

演示账号：

```text
管理员：admin / 123456
普通成员：zhangsan / 123456
```

## 前后端连接方式

前端只用 `fetch` 请求 JSON 接口。例如登录：

```text
POST /api/auth/login
```

后端收到账号密码后，通过 JDBC 查询 MySQL，再把结果转成 JSON 返回。
登录成功后，Tomcat 会保存 `JSESSIONID`，后续请求依靠它识别当前用户。

完整接口列表见：

```text
src/main/webapp/prototype/docs/API.md
```

## 页面

| 页面 | 作用 |
|---|---|
| `login.html` | 登录 |
| `register.html` | 注册普通成员 |
| `home.html` | 浏览和筛选社团、申请加入 |
| `my-applications.html` | 查看自己的申请记录 |
| `club-member.html` | 查看社团介绍、公告和活动 |
| `personal-member.html` | 普通成员个人中心 |
| `applications.html` | 管理员审批入团申请 |
| `club-admin.html` | 管理员管理社团成员、公告和活动 |
| `personal-admin.html` | 管理员个人中心 |
| `admin.html` | 管理中心 |

成员活动报名暂未实现，其他页面操作均已连接后端接口。
