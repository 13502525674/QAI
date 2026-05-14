package com.ape.apeframework.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author shaozhujie
 * @version 1.0
 * @description: 跨域
 * @date 2023/8/28 10:57
 */
@Configuration
public class CorsConfig {

    /**
     * @description: 配置跨域
     * @param:
     * @return:
     * @author shaozhujie
     * @date: 2023/9/14 11:03
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许cookies跨域
        corsConfiguration.setAllowCredentials(true);
        // #允许向该服务器提交请求的URI，*表示全部允许，自定义可以添加多个
        corsConfiguration.addAllowedOriginPattern("*");
        // #允许访问的头信息,*表示全部，可以添加多个
        corsConfiguration.addAllowedHeader("*");
        // 预检请求的缓存时间（秒），即在这个时间段里，对于相同的跨域请求不会再预检了
        corsConfiguration.setMaxAge(1800L);
        // 允许提交请求的方法，*表示全部允许，一般OPTIONS,GET,POST三个够了
        corsConfiguration.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", corsConfiguration);

        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorsFilter(source));
        // 最高优先级，确保在 JwtFilter 之前执行
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
