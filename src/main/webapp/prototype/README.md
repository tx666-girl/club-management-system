# 社团管理系统前端原型

根据 Modao 产品流程图整理的可预览 HTML 原型。

## 页面

| 文件 | 用途 |
|---|---|
| `login.html` | 登录 |
| `register.html` | 注册 |
| `admin.html` | 管理员界面原型总入口 |
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

分类入口会跳转到 `home.html?category=tech` 等地址，并由
`js/home-filter.js` 过滤社团卡片。

后端接口契约见 `docs/API.md`。

## 预览图

`previews/` 目录包含每页桌面截图和手机截图。

总览图：

```text
previews/overview.png
```

## 说明

- 这是纯 HTML/CSS 静态原型，没有连接后端。
- 页面之间的导航链接可以直接点击。
- 不依赖外部字体、图片或 JavaScript 库。
- 后续可以直接把这些页面替换到 `src/main/webapp`，或者交给前端继续改成 JSP。

## 删除

如果不再需要这套原型，可以整体删除：

```text
C:\Users\ASUS\Documents\Codex\2026-10-18\w\outputs\club-prototype-html
C:\Users\ASUS\Documents\Codex\2026-10-18\w\outputs\club-prototype-html.zip
```
