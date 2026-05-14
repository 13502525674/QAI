package com.ape.apeframework.filter;

import com.alibaba.fastjson2.JSONObject;
import com.ape.apecommon.utils.JwtUtil;
import com.ape.apeframework.custom.JwtToken;
import com.ape.apeframework.utils.RequestUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author shaozhujie
 * @version 1.0
 * @description: jwt过滤器
 * @date 2023/8/11 9:59
 */
public class JwtFilter implements Filter {

    // 不需要认证的路径（与原版 ShiroConfig 中的 anon 路径一致）
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/login",
            "/login/register",
            "/classification/getApeClassificationList",
            "/school/getApeSchoolList",
            "/major/getApeMajorList",
            "/user/setUserAvatar",
            "/common/",
            "/img/",
            "/video/",
            "/file/",
            "/api/test/",
            "/api/ai/recommend/my",
            "/chat/",
            "/chatApi/images/"  // 聊天图片路径
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // 检查是否是排除的路径（anon）
        if (isExcludePath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 执行 JWT 认证（与原版 JwtFilter 逻辑一致）
        try {
            String token = JwtUtil.getTokenByRequest(httpRequest);
            JwtToken jwtToken = new JwtToken(token);
            // 提交给 realm 进行登入，如果错误他会抛出异常并被捕获
            Subject subject = SecurityUtils.getSubject();
            subject.login(jwtToken);
            // 如果没有抛出异常则代表登入成功
            chain.doFilter(request, response);
        } catch (IncorrectCredentialsException e) {
            returnError(httpResponse, 1011, e.getMessage());
        } catch (LockedAccountException e) {
            returnError(httpResponse, 1009, e.getMessage());
        } catch (UnknownAccountException e) {
            returnError(httpResponse, 1008, e.getMessage());
        } catch (AuthenticationException e) {
            returnError(httpResponse, 1006, e.getMessage());
        } catch (Exception e) {
            returnError(httpResponse, 1006, e.getMessage());
        }
    }

    /**
     * 返回错误信息
     */
    private void returnError(HttpServletResponse response, int code, String message) throws IOException {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("message", message);
        json.put("timeStamp", System.currentTimeMillis());
        try {
            RequestUtils.returnJson(response, json.toJSONString());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * 检查是否是排除认证的路径
     */
    private boolean isExcludePath(String path) {
        for (String excludePath : EXCLUDE_PATHS) {
            if (path.equals(excludePath) || path.startsWith(excludePath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
    }
}
