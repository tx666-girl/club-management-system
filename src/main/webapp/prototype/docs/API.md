# 前后端接口约定

这份文档用于把当前静态 HTML 原型接入 Servlet + JDBC 后端。

## 基础约定

如果部署在独立 Tomcat 的 `club-backend` 上下文，接口前缀是：

```text
/club-backend/api
```

如果部署在 Maven 内嵌 Tomcat 的根路径，接口前缀是：

```text
/api
```

前端页面和接口在同一个 Web 应用里时，使用相对地址，不需要 CORS：

```js
fetch("/api/clubs", {
  credentials: "same-origin"
});
```

登录状态使用 `JSESSIONID`，前端不要自己保存用户角色作为权限依据。

统一响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 登录和注册

### POST `/api/auth/login`

请求：

```json
{
  "username": "admin",
  "password": "123456"
}
```

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "系统管理员",
    "role": "ADMIN"
  }
}
```

失败响应：

```json
{
  "code": 401,
  "message": "账号或密码错误",
  "data": null
}
```

### POST `/api/auth/register`

请求：

```json
{
  "username": "zhangsan",
  "password": "123456",
  "name": "张三"
}
```

### POST `/api/auth/logout`

销毁 Session，返回成功。

### GET `/api/auth/me`

返回当前 Session 用户。未登录返回 401。

## 社团

### GET `/api/clubs?category=tech`

`category` 可选，对应页面的分类：

```text
tech       学术科技
art        文化艺术
sport      体育运动
volunteer  志愿公益
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "计算机协会",
      "category": "tech",
      "description": "技术分享、项目实践和编程交流。",
      "imageUrl": "assets/clubs/tech.svg",
      "memberCount": 128,
      "joined": false,
      "pending": false
    }
  ]
}
```

### GET `/api/clubs/{id}`

返回社团详情、简介、活动数和用户当前入团状态。

### GET `/api/clubs/{id}/announcements`

返回社团公告：

```json
[
  {
    "id": 1,
    "title": "本周活动报名开始",
    "content": "请提前十分钟到场。",
    "publisherName": "管理员",
    "createdAt": "2026-10-19 10:00:00"
  }
]
```

## 普通成员

### POST `/api/applications`

申请加入社团：

```json
{
  "clubId": 1
}
```

### GET `/api/applications/my`

返回当前用户的申请：

```json
[
  {
    "id": 10,
    "clubId": 1,
    "clubName": "计算机协会",
    "status": "PENDING",
    "createdAt": "2026-10-20 09:30:00",
    "reviewedAt": null
  }
]
```

状态值：

```text
PENDING   待审批
APPROVED  已通过
REJECTED  已拒绝
```

### GET `/api/me/clubs`

返回当前用户已经加入的社团。

### GET `/api/me/announcements?limit=10`

返回当前用户已加入社团的最新公告。

## 管理员

以下接口必须由管理员 Session 访问。

### GET `/api/admin/applications`

返回待审批申请：

```json
[
  {
    "id": 10,
    "applicantName": "李四",
    "clubId": 1,
    "clubName": "计算机协会",
    "type": "JOIN",
    "status": "PENDING",
    "createdAt": "2026-10-20 09:30:00"
  }
]
```

### POST `/api/admin/applications/{id}/approve`

通过申请。后端应在同一个事务中：

```text
更新申请状态为 APPROVED
写入社团成员表
提交事务
```

### POST `/api/admin/applications/{id}/reject`

拒绝申请。

### GET `/api/admin/clubs/{id}/members`

返回社团成员：

```json
[
  {
    "userId": 2,
    "username": "zhangsan",
    "name": "张三",
    "joinedAt": "2026-09-10 12:00:00"
  }
]
```

### DELETE `/api/admin/clubs/{id}/members/{userId}`

把成员移出社团。

### POST `/api/admin/announcements`

发布公告：

```json
{
  "clubId": 1,
  "title": "本周活动报名开始",
  "content": "请提前十分钟到场。"
}
```

## 页面与接口对应

| 页面 | 接口 |
|---|---|
| `login.html` | `POST /api/auth/login` |
| `register.html` | `POST /api/auth/register` |
| `home.html` | `GET /api/clubs` |
| `my-applications.html` | `GET /api/applications/my` |
| `applications.html` | `GET /api/admin/applications`、审批接口 |
| `club-member.html` | 社团详情和公告接口 |
| `club-admin.html` | 社团、成员、公告管理接口 |
| `personal-member.html` | `/api/me/clubs`、`/api/me/announcements` |
| `personal-admin.html` | 待审批申请和管理社团接口 |

## 接入顺序

1. 把 `jdbc.properties` 配置放在服务器本地。
2. 执行 `database/schema.sql` 和 `database/seed.sql`。
3. 将 `LoginServlet` 从纯文本返回值改成 JSON。
4. 增加社团、申请、公告、成员 Servlet。
5. 把原型页面里的静态数据替换成 `fetch` 返回的数据。
6. 管理员接口统一做 Session 角色校验，不能只靠前端隐藏按钮。

## 重要原则

- 前端不能直接连接 MySQL。
- 真实数据库密码不能出现在 HTML 或 JavaScript 中。
- 管理员权限必须在后端 Servlet/Filter 中再次检查。
- 审批入团必须使用数据库事务。
- 所有 SQL 使用 `PreparedStatement`。
