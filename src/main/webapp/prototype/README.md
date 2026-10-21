# 社团管理系统前端

这些页面已经接入 `/api/*` 后端接口，不再是纯静态原型。

## 页面

| 文件 | 用途 |
|---|---|
| `login.html` | 登录 |
| `register.html` | 注册 |
| `admin.html` | 管理员功能入口 |
| `home.html` | 全部社团分类 |
| `my-applications.html` | 普通成员查看入团申请 |
| `applications.html` | 管理员审批入团/创建社团申请 |
| `club-member.html` | 社员查看社团详情、活动和公告 |
| `club-admin.html` | 管理员管理社团、活动公告和成员 |
| `personal-member.html` | 普通成员个人中心 |
| `personal-admin.html` | 管理员个人中心 |

登录页采用左侧阳光校园插画、右侧登录表单的布局。

首页在桌面端按两列四行展示 8 个社团，移动端自动切换为单列。

所有图片都放在 `assets/` 目录，使用本地 SVG，不依赖外部图片链接。

分类入口会跳转到 `home.html?category=tech` 等地址。
`js/home.js` 会调用后端接口，重新加载对应分类的社团。

后端接口契约见 `docs/API.md`。

## 说明

- 页面使用 HTML、CSS 和原生 JavaScript。
- 登录状态由 `JSESSIONID` 保存。
- 页面不能直接双击打开；必须通过 Tomcat 访问，才能调用后端接口。
- 不依赖外部字体、图片或 JavaScript 库。
