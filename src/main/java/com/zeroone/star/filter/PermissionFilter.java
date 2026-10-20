package com.zeroone.star.filter;

import com.zeroone.star.service.UserService;
import com.zeroone.star.service.impl.UserServiceImpl;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 权限验证过滤器。
 *
 * HTML 原型位于 /prototype/ 下，原型页面和静态资源需要允许直接访问，
 * 真正的登录和角色权限仍由 Servlet 与后端接口负责。
 */
@WebFilter("/*")
public class PermissionFilter extends HttpFilter {

    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        String requestUri = request.getRequestURI();
        String uri = requestUri.replace(request.getContextPath(), "");

        if (isPublicPath(uri)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");
        if (username == null) {
            response.sendRedirect(request.getContextPath() + "/prototype/login.html");
            return;
        }

        if (userService.hasPermission(username, uri)) {
            chain.doFilter(request, response);
            return;
        }

        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.print("没有访问权限");
        }
    }

    private boolean isPublicPath(String uri) {
        return "/".equals(uri)
                || "/index.html".equals(uri)
                || "/login".equals(uri)
                || "/prototype".equals(uri)
                || uri.startsWith("/prototype/")
                || uri.startsWith("/js");
    }
}
