package com.demo.interceptor;


import com.demo.exception.user.JWTTokenException;
import com.demo.exception.user.LoginException;
import com.demo.pojo.UserContext;
import com.demo.util.JWTUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class AutoLoginInterceptor implements HandlerInterceptor {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return true;
        }

        String token = authorization.substring(7);

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        if (ops.get("jwt:blacklist:" + token) != null) {
            return true;
        }

        Long userId = JWTUtil.parseJWT(token);
        if (userId == null) {
            return true;
        }

        String key = (String) ops.get("login:active:" + userId);
        if (key == null) {
            return true;
        }

        if (!JWTUtil.parseJti(token).equals(key)) {
            return true;
        }

        UserContext.set(userId);


        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}
