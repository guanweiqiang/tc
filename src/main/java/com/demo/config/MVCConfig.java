package com.demo.config;

import com.demo.interceptor.AutoLoginInterceptor;
import com.demo.interceptor.LoginRequiredInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class MVCConfig implements WebMvcConfigurer {


    @Resource
    private AutoLoginInterceptor autoLoginInterceptor;

    @Resource
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Resource
    private FileUploadProperties fileUploadProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(autoLoginInterceptor)
                .addPathPatterns("/**")
                .order(0);


        registry.addInterceptor(loginRequiredInterceptor)
                .addPathPatterns("/user/*")
                .addPathPatterns("/auth/logout")
                .addPathPatterns("/article/add")
                .addPathPatterns("/article/like/**")
                .order(1);




    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(fileUploadProperties.getAvatarUrlPrefix() + "**")
                .addResourceLocations("file:" + fileUploadProperties.getAvatarPath());
    }


    //    @Override
//    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
//        configurer.enable();
//    }
}
