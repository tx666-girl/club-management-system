package com.zeroone.star.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zeroone.star.dao.ActivityDao;
import com.zeroone.star.dao.AnnouncementDao;
import com.zeroone.star.dao.ApplicationDao;
import com.zeroone.star.dao.ClubDao;
import com.zeroone.star.dao.UserDao;
import com.zeroone.star.dao.impl.ActivityDaoImpl;
import com.zeroone.star.dao.impl.AnnouncementDaoImpl;
import com.zeroone.star.dao.impl.ApplicationDaoImpl;
import com.zeroone.star.dao.impl.ClubDaoImpl;
import com.zeroone.star.dao.impl.UserDaoImpl;
import com.zeroone.star.entity.User;
import com.zeroone.star.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 社团管理系统统一 JSON API。
 *
 * 前端页面全部通过 /api/* 调用这里，页面本身不直接访问 MySQL。
 */
@WebServlet("/api/*")
public class ApiServlet extends HttpServlet {

    private static final UserDao USER_DAO = new UserDaoImpl();
    private static final ClubDao CLUB_DAO = new ClubDaoImpl();
    private static final ApplicationDao APPLICATION_DAO = new ApplicationDaoImpl();
    private static final AnnouncementDao ANNOUNCEMENT_DAO = new AnnouncementDaoImpl();
    private static final ActivityDao ACTIVITY_DAO = new ActivityDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        List<String> path = pathParts(request);
        try {
            if (path.size() == 2 && "auth".equals(path.get(0)) && "me".equals(path.get(1))) {
                User user = requireUser(request, response);
                if (user != null) {
                    ok(response, user);
                }
                return;
            }

            if (path.size() == 1 && "clubs".equals(path.get(0))) {
                User user = requireUser(request, response);
                if (user != null) {
                    ok(response, CLUB_DAO.list(request.getParameter("category"), user.getId()));
                }
                return;
            }

            if (path.size() >= 2 && "clubs".equals(path.get(0))) {
                User user = requireUser(request, response);
                if (user == null) {
                    return;
                }
                long clubId = parseLong(path.get(1));
                if (path.size() == 2) {
                    java.util.Optional<com.zeroone.star.entity.Club> club =
                            CLUB_DAO.findById(clubId, user.getId());
                    if (club.isPresent()) {
                        ok(response, club.get());
                    } else {
                        error(response, 404, "社团不存在");
                    }
                    return;
                }
                if (path.size() == 3 && "announcements".equals(path.get(2))) {
                    ok(response, ANNOUNCEMENT_DAO.listByClub(clubId));
                    return;
                }
                if (path.size() == 3 && "activities".equals(path.get(2))) {
                    ok(response, ACTIVITY_DAO.listByClub(clubId));
                    return;
                }
            }

            if (path.size() == 2 && "applications".equals(path.get(0))
                    && "my".equals(path.get(1))) {
                User user = requireUser(request, response);
                if (user != null) {
                    ok(response, APPLICATION_DAO.findByUser(user.getId()));
                }
                return;
            }

            if (path.size() == 2 && "me".equals(path.get(0))) {
                User user = requireUser(request, response);
                if (user == null) {
                    return;
                }
                if ("clubs".equals(path.get(1))) {
                    ok(response, CLUB_DAO.joinedByUser(user.getId()));
                    return;
                }
                if ("announcements".equals(path.get(1))) {
                    ok(response, ANNOUNCEMENT_DAO.latestForUser(user.getId(), 10));
                    return;
                }
            }

            if (path.size() >= 1 && "admin".equals(path.get(0))) {
                User admin = requireAdmin(request, response);
                if (admin == null) {
                    return;
                }
                if (path.size() == 2 && "applications".equals(path.get(1))) {
                    ok(response, APPLICATION_DAO.findPending());
                    return;
                }
                if (path.size() == 2 && "clubs".equals(path.get(1))) {
                    ok(response, CLUB_DAO.list(null, admin.getId()));
                    return;
                }
                if (path.size() == 2 && "stats".equals(path.get(1))) {
                    Map<String, Object> stats = new LinkedHashMap<>();
                    stats.put("clubs", CLUB_DAO.countAll());
                    stats.put("activities", ACTIVITY_DAO.countAll());
                    stats.put("pendingApplications", APPLICATION_DAO.countPending());
                    stats.put("members", CLUB_DAO.sumMemberCount());
                    ok(response, stats);
                    return;
                }
                if (path.size() == 4 && "clubs".equals(path.get(1))
                        && "members".equals(path.get(3))) {
                    ok(response, CLUB_DAO.findMembers(parseLong(path.get(2))));
                    return;
                }
            }

            error(response, 404, "接口不存在");
        } catch (Exception e) {
            handleError(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        List<String> path = pathParts(request);
        try {
            if (path.size() == 2 && "auth".equals(path.get(0))) {
                if ("login".equals(path.get(1))) {
                    login(request, response);
                    return;
                }
                if ("register".equals(path.get(1))) {
                    register(request, response);
                    return;
                }
                if ("logout".equals(path.get(1))) {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        session.invalidate();
                    }
                    ok(response, null);
                    return;
                }
            }

            if (path.size() == 1 && "applications".equals(path.get(0))) {
                User user = requireUser(request, response);
                if (user == null) {
                    return;
                }
                long clubId = jsonBody(request).getLongValue("clubId");
                if (!CLUB_DAO.existsById(clubId)) {
                    throw new IllegalArgumentException("社团不存在");
                }
                if (CLUB_DAO.isMember(clubId, user.getId())) {
                    throw new IllegalStateException("已经加入该社团");
                }
                APPLICATION_DAO.createJoin(user.getId(), clubId);
                ok(response, null);
                return;
            }

            if (path.size() >= 1 && "admin".equals(path.get(0))) {
                User admin = requireAdmin(request, response);
                if (admin == null) {
                    return;
                }
                if (path.size() == 4 && "applications".equals(path.get(1))
                        && "approve".equals(path.get(3))) {
                    APPLICATION_DAO.approve(parseLong(path.get(2)), admin.getId(), CLUB_DAO);
                    ok(response, null);
                    return;
                }
                if (path.size() == 4 && "applications".equals(path.get(1))
                        && "reject".equals(path.get(3))) {
                    APPLICATION_DAO.reject(parseLong(path.get(2)), admin.getId());
                    ok(response, null);
                    return;
                }
                if (path.size() == 2 && "announcements".equals(path.get(1))) {
                    JSONObject body = jsonBody(request);
                    ANNOUNCEMENT_DAO.create(
                            body.getLongValue("clubId"),
                            admin.getId(),
                            required(body, "title"),
                            required(body, "content")
                    );
                    ok(response, null);
                    return;
                }
                if (path.size() == 2 && "activities".equals(path.get(1))) {
                    JSONObject body = jsonBody(request);
                    ACTIVITY_DAO.create(
                            body.getLongValue("clubId"),
                            admin.getId(),
                            required(body, "title"),
                            required(body, "description"),
                            required(body, "startTime"),
                            required(body, "location")
                    );
                    ok(response, null);
                    return;
                }
            }

            error(response, 404, "接口不存在");
        } catch (Exception e) {
            handleError(response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        List<String> path = pathParts(request);
        try {
            User user = requireUser(request, response);
            if (user == null) {
                return;
            }
            if (path.size() == 2 && "me".equals(path.get(0)) && "profile".equals(path.get(1))) {
                JSONObject body = jsonBody(request);
                String name = required(body, "name");
                String studentNo = body.getString("studentNo");
                String grade = body.getString("grade");
                USER_DAO.updateProfile(user.getId(), name, studentNo, grade);
                user.setName(name);
                user.setStudentNo(studentNo);
                user.setGrade(grade);
                ok(response, user);
                return;
            }
            if (path.size() == 2 && "auth".equals(path.get(0)) && "password".equals(path.get(1))) {
                JSONObject body = jsonBody(request);
                User databaseUser = USER_DAO.getUserByUsername(user.getUsername());
                if (databaseUser == null
                        || !databaseUser.getPassword().equals(body.getString("oldPassword"))) {
                    throw new IllegalArgumentException("原密码错误");
                }
                String newPassword = required(body, "newPassword");
                if (newPassword.length() < 6) {
                    throw new IllegalArgumentException("新密码至少 6 位");
                }
                USER_DAO.updatePassword(user.getId(), newPassword);
                ok(response, null);
                return;
            }
            if (path.size() >= 1 && "admin".equals(path.get(0))) {
                User admin = requireAdmin(request, response);
                if (admin == null) {
                    return;
                }
                if (path.size() == 3 && "announcements".equals(path.get(1))) {
                    JSONObject body = jsonBody(request);
                    ANNOUNCEMENT_DAO.update(
                            parseLong(path.get(2)),
                            required(body, "title"),
                            required(body, "content")
                    );
                    ok(response, null);
                    return;
                }
                if (path.size() == 3 && "activities".equals(path.get(1))) {
                    JSONObject body = jsonBody(request);
                    ACTIVITY_DAO.update(
                            parseLong(path.get(2)),
                            required(body, "title"),
                            required(body, "description"),
                            required(body, "startTime"),
                            required(body, "location")
                    );
                    ok(response, null);
                    return;
                }
            }
            error(response, 404, "接口不存在");
        } catch (Exception e) {
            handleError(response, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        List<String> path = pathParts(request);
        try {
            User admin = requireAdmin(request, response);
            if (admin == null) {
                return;
            }
            if (path.size() == 3 && "announcements".equals(path.get(1))) {
                ANNOUNCEMENT_DAO.delete(parseLong(path.get(2)));
                ok(response, null);
                return;
            }
            if (path.size() == 3 && "activities".equals(path.get(1))) {
                ACTIVITY_DAO.delete(parseLong(path.get(2)));
                ok(response, null);
                return;
            }
            if (path.size() == 5 && "clubs".equals(path.get(1))
                    && "members".equals(path.get(3))) {
                CLUB_DAO.removeMember(parseLong(path.get(2)), parseLong(path.get(4)));
                ok(response, null);
                return;
            }
            error(response, 404, "接口不存在");
        } catch (Exception e) {
            handleError(response, e);
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        JSONObject body = jsonBody(request);
        String username = required(body, "username");
        String password = required(body, "password");
        User user = USER_DAO.getUserByUsername(username);

        if (user == null) {
            error(response, 401, "账号不存在");
            return;
        }
        if (!user.getPassword().equals(password)) {
            error(response, 401, "账号或密码错误");
            return;
        }

        user.setPassword(null);
        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        session.setAttribute("username", user.getUsername());
        ok(response, user);
    }

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        JSONObject body = jsonBody(request);
        String username = required(body, "username");
        String password = required(body, "password");
        String name = required(body, "name");

        if (username.length() < 3) {
            throw new IllegalArgumentException("账号至少 3 位");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("密码至少 6 位");
        }
        if (USER_DAO.getUserByUsername(username) != null) {
            throw new IllegalStateException("账号已经存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setName(name);
        user.setStudentNo(body.getString("studentNo"));
        user.setGrade(body.getString("grade"));
        user.setRole("USER");
        user.setStatus("ACTIVE");
        long id = USER_DAO.createUser(user);
        user.setId(id);
        user.setPassword(null);
        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        session.setAttribute("username", user.getUsername());
        ok(response, user);
    }

    private User requireUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("user");
        if (value instanceof User) {
            return (User) value;
        }
        error(response, 401, "请先登录");
        return null;
    }

    private User requireAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return null;
        }
        if (!"ADMIN".equals(user.getRole())) {
            error(response, 403, "没有管理员权限");
            return null;
        }
        return user;
    }

    private JSONObject jsonBody(HttpServletRequest request) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        if (builder.length() == 0) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        JSONObject object = JSON.parseObject(builder.toString());
        if (object == null) {
            throw new IllegalArgumentException("请求体不是 JSON");
        }
        return object;
    }

    private String required(JSONObject body, String key) {
        String value = body.getString(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(key + "不能为空");
        }
        return value.trim();
    }

    private List<String> pathParts(HttpServletRequest request) {
        String path = request.getPathInfo();
        List<String> parts = new ArrayList<>();
        if (path == null) {
            return parts;
        }
        for (String value : path.split("/")) {
            if (!value.isEmpty()) {
                parts.add(value);
            }
        }
        return parts;
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("编号格式不正确");
        }
    }

    private void ok(HttpServletResponse response, Object data) throws IOException {
        write(response, 200, 200, "success", data);
    }

    private void error(HttpServletResponse response, int status, String message)
            throws IOException {
        write(response, status, status, message, null);
    }

    private void write(HttpServletResponse response, int status, int code,
                       String message, Object data) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("data", data);
        try (PrintWriter writer = response.getWriter()) {
            writer.print(JsonUtil.toJson(body));
        }
    }

    private void handleError(HttpServletResponse response, Exception exception)
            throws IOException {
        if (exception instanceof IllegalArgumentException) {
            error(response, 400, exception.getMessage());
        } else if (exception instanceof IllegalStateException) {
            error(response, 409, exception.getMessage());
        } else if (exception instanceof UnsupportedOperationException) {
            error(response, 501, exception.getMessage());
        } else if (exception instanceof SQLIntegrityConstraintViolationException) {
            error(response, 409, "数据已存在或不符合约束");
        } else {
            exception.printStackTrace();
            error(response, 500, "服务器内部错误");
        }
    }
}
