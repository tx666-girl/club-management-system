# 社团管理系统接口

所有接口统一使用：

```text
/api
```

前端页面位于 `/prototype/`，因此页面中的请求基础地址是：

```js
const API_BASE = "../api";
```

登录状态保存在 `JSESSIONID` 中，前端请求使用：

```js
credentials: "same-origin"
```

统一响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 登录和账号

```text
POST /api/auth/login
POST /api/auth/register
POST /api/auth/logout
GET  /api/auth/me
PUT  /api/auth/password
PUT  /api/me/profile
```

登录请求：

```json
{
  "username": "admin",
  "password": "123456"
}
```

登录成功返回：

```json
{
  "id": 1,
  "username": "admin",
  "name": "系统管理员",
  "studentNo": "A00001",
  "grade": "教师",
  "role": "ADMIN",
  "status": "ACTIVE"
}
```

## 社团

```text
GET /api/clubs
GET /api/clubs?category=tech
GET /api/clubs/{id}
GET /api/clubs/{id}/announcements
GET /api/clubs/{id}/activities
```

分类值：

```text
tech       学术科技
art        文化艺术
sport      体育运动
volunteer  志愿公益
```

社团返回字段：

```text
id, name, category, description, imageUrl,
memberCount, joined, pending
```

## 普通成员

```text
POST /api/applications
GET  /api/applications/my
GET  /api/me/clubs
GET  /api/me/announcements
```

申请入团：

```json
{
  "clubId": 1
}
```

## 管理员

```text
GET    /api/admin/applications
POST   /api/admin/applications/{id}/approve
POST   /api/admin/applications/{id}/reject

GET    /api/admin/clubs
GET    /api/admin/stats
GET    /api/admin/clubs/{id}/members
DELETE /api/admin/clubs/{id}/members/{userId}

POST   /api/admin/announcements
PUT    /api/admin/announcements/{id}
DELETE /api/admin/announcements/{id}

POST   /api/admin/activities
PUT    /api/admin/activities/{id}
DELETE /api/admin/activities/{id}
```

公告请求：

```json
{
  "clubId": 1,
  "title": "本周活动通知",
  "content": "周六下午三点在活动室集合。"
}
```

活动请求：

```json
{
  "clubId": 1,
  "title": "前端项目实战分享",
  "description": "从页面结构到接口联调。",
  "startTime": "2026-10-24 14:00:00",
  "location": "活动室 A"
}
```

当前不实现成员活动报名，因此没有 `activity_signup` 接口。

## 页面与接口

| 页面 | 接口 |
|---|---|
| `login.html` | `POST /api/auth/login` |
| `register.html` | `POST /api/auth/register` |
| `home.html` | `GET /api/clubs`、`POST /api/applications` |
| `my-applications.html` | `GET /api/auth/me`、`GET /api/applications/my`、`GET /api/me/clubs` |
| `applications.html` | `GET /api/admin/applications`、审批接口、`GET /api/admin/stats` |
| `club-member.html` | 社团详情、公告、活动接口 |
| `club-admin.html` | 社团详情、成员、公告、活动管理接口 |
| `personal-member.html` | 当前用户、我的申请、我的社团、最新公告 |
| `personal-admin.html` | 管理员统计、待审批申请、全部社团、个人资料 |

## 权限

```text
未登录：只能访问登录、注册
USER：社团浏览、申请加入、个人数据
ADMIN：申请审批、成员管理、公告管理、活动管理
```

管理员接口由 `ApiServlet` 在服务端检查 Session 角色，不能只依赖前端隐藏按钮。

## 数据库

```text
database/schema.sql
database/seed.sql
```

当前表：

```text
sys_user
club
club_member
application
announcement
activity
```
