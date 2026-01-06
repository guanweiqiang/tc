package com.demo.interceptor;


import com.demo.exception.user.JWTTokenException;
import com.demo.exception.user.LoginException;
import com.demo.pojo.UserContext;
import com.demo.util.JWTUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
            throw new JWTTokenException("未登录");
        }

        String token = authorization.substring(7);

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        if (ops.get("jwt:blacklist:" + token) != null) {
            throw new JWTTokenException("token已失效");
        }

        Long userId = JWTUtil.parseJWT(token);
        if (userId == null) {
            throw new JWTTokenException("登陆状态失效");
        }

        String key = (String) ops.get("login:active:" + userId);
        if (key == null) {
            throw new LoginException("登陆已过期");
        }

        if (!JWTUtil.parseJti(token).equals(key)) {
            throw new LoginException("账号在其他设备登录");
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
