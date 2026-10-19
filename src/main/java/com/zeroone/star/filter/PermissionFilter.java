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
 * <p>
 * 描述：权限验证过滤器
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
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
        if ("/".equals(uri) || "/login".equals(uri) || uri.endsWith("index.jsp") || uri.startsWith("/js")) {
            chain.doFilter(request, response);
        } else {
            HttpSession session = request.getSession();
            String username = (String) session.getAttribute("username");
            if (username == null) {
                System.out.println("登录超时");
                response.sendRedirect("index.jsp");
            } else {
                //这里就应该去数据库查询当前登录用户是否具有访问这个URL地址的权限
                if (userService.hasPermission(username, uri)) {
                    chain.doFilter(request, response);
                } else {
                    response.setCharacterEncoding("UTF-8");
                    PrintWriter writer = response.getWriter();
                    writer.print("没有访问权限");
                    writer.flush();
                    writer.close();
                }
            }
        }
    }
}
