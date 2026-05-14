package com.ape.apeframework.config;

import com.ape.apeframework.filter.JwtFilter;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author shaozhujie
 * @version 1.0
 * @description: shiro配置类
 * @date 2023/8/11 9:14
 */
@Configuration
public class ShiroConfig {

    /**
     * @description: 注册 JWT 过滤器
     * @return: FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtFilter());
        registration.addUrlPatterns("/*");
        registration.setName("jwtFilter");
        registration.setOrder(1); // CorsFilter 优先级更高
        return registration;
    }

    /**
     * @description: 注入realm进行安全管理
     * @param: shiroRealm
     * @return:
     * @author shaozhujie
     * @date: 2023/9/14 11:06
     */
    @Bean("securityManager")
    public SecurityManager securityManager(ShiroRealm shiroRealm) {
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        securityManager.setRealm(shiroRealm);
        // 关闭shiro自带的session存放token功能
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);
        // 设置为全局 SecurityManager，使 SecurityUtils.getSubject() 可用
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    @Bean("lifecycleBeanPostProcessor")
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * @description: 高版本shrio增加配置，否则类里方法上有@RequiresPermissions注解的，会导致整个类下的接口无法访问404
     * @param:
     * @return:
     * @author shaozhujie
     * @date: 2023/9/14 11:07
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}
