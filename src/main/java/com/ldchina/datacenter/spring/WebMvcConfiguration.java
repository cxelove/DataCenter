package com.ldchina.datacenter.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    /**
     * 注册 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SpringMVCInterceptor())
                //添加过滤url ** 代表所有
                .addPathPatterns("/**")
                //添加排除过滤url
                .excludePathPatterns(
                        "/favicon.ico"
                        , "/**/*.jpg"
                        , "/**/*.png"
                        , "/**/*.css"
                        , "/**/*.js"
                );
        super.addInterceptors(registry);
    }
    /**
     * 解决resources下面静态资源无法访问
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/favicon.ico")//favicon.ico
                .addResourceLocations("classpath:/static/");
        super.addResourceHandlers(registry);
    }
}
