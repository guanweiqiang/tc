package com.demo.service.Impl;

import com.demo.exception.GlobalException;
import com.demo.exception.auth.VerificationCodeException;
import com.demo.pojo.EmailVerifyPurpose;
import com.demo.service.EmailVerificationService;
import com.demo.util.VerificationUtil;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    public void sendMail(String email, EmailVerifyPurpose purpose) {
        String key = "email:verify:" + purpose.name() + ":" + email;
        String countKey = "email:verify:count:" + purpose.name() + ":" + email;

        //check if there is already verification code
        //if there is, don't send
        if (redisTemplate.opsForValue().get(key) != null) {
            log.info("{}重复获取验证码", email);
            throw new VerificationCodeException("请勿重复获取验证码");
        }

        //check the frequency of sending email
        Integer count = (Integer)redisTemplate.opsForValue().get(countKey);
        if (count != null && count >= VerificationUtil.MAX_COUNT) {
            log.info("{}发送验证码达到上限", email);
            throw new VerificationCodeException("获取验证码次数达到上限，请在" + VerificationUtil.DURATION + "小时后重试");
        }

        String verificationCode = VerificationUtil.generateVerificationCode();
        //store code to redis
        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                key,
                verificationCode,
                VerificationUtil.TIME_OUT,
                TimeUnit.MILLISECONDS);

        if (Boolean.FALSE.equals(success)) {
            throw new VerificationCodeException("请勿重复获取验证码");
        }

        try {
            log.info("开始向{}发送验证码", email);
            sendEmail(email, verificationCode, purpose);
        } catch (MessagingException e) {
            redisTemplate.delete(key);
            log.warn("向{}邮件发送失败", email);
            throw new GlobalException("邮件发送失败，请重试");
        }

        log.info("向{}发送验证码成功", email);
        //increase the count of sending email
        if (count == null) {
            redisTemplate.opsForValue().set(
                    countKey,
                    1,
                    VerificationUtil.DURATION,
                    TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(countKey);
        }
    }

    //todo: send different template html by purpose
    private void sendEmail(String email, String code, EmailVerifyPurpose purpose) throws MessagingException {
        //send code by email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("2125205485@qq.com");
        helper.setTo(email);
        helper.setSubject("邮箱验证码");

        Context context = new Context();
        context.setVariable("code", code);
        String html = templateEngine.process("email/verification.html", context);
        helper.setText(html, true);

        mailSender.send(message);
    }

    public void verify(String email, String code, EmailVerifyPurpose purpose) {
        String key = "email:verify:" + purpose.name() + ":" + email;

        //check whether the verification code expired
        //for safe, the code are allowed to get one time
        //if fail at the first time, it should apply again
        String verify = (String)redisTemplate.opsForValue().get(key);
        if (verify == null) {
            throw new VerificationCodeException("验证码为空或已过期，请重新获取");
        }

        //compare the verification code with the code sent
        if (!verify.equals(code)) {
            throw new VerificationCodeException("验证码不正确");
        }
    }
}
