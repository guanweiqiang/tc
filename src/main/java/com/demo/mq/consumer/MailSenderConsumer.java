package com.demo.mq.consumer;

import com.demo.config.MailMQConfig;
import com.demo.common.exception.GlobalException;
import com.demo.pojo.EmailVerifyPurpose;
import com.demo.model.dto.MailMessageDTO;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class MailSenderConsumer {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @RabbitListener(queues = {MailMQConfig.QUEUE})
    public void process(MailMessageDTO mailMessageDTO, Message message, Channel channel) {
        log.info("mq收到发送邮件的信息");
        log.debug("msg={}", mailMessageDTO);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String key = "mail:sent:mq:"
                + mailMessageDTO.getEmail()
                + ":" + mailMessageDTO.getCode()
                + ":" + mailMessageDTO.getPurpose();

        try {

            if (Boolean.FALSE.equals(redisTemplate.opsForValue().setIfAbsent(key,
                    1,
                    10,
                    TimeUnit.MINUTES))) {
                log.warn("邮件消息重复消费，直接ack，dto={}", mailMessageDTO);
            } else {
                sendEmail(mailMessageDTO.getEmail(),
                        mailMessageDTO.getCode(),
                        mailMessageDTO.getPurpose());
            }

            channel.basicAck(deliveryTag, false);
        } catch (MessagingException e) {


            try {
//                Boolean redelivered = message.getMessageProperties().getRedelivered();
//
//                if (redelivered) {
//                    log.warn("消息已经重新发送，丢弃信息，发送邮件dto={}", mailMessageDTO);
//                } else {
//                    log.warn("向{}发送邮件失败，重新发送", mailMessageDTO.getEmail());
//                }
                log.warn("发送邮件失败，dto={}，进入死信队列", mailMessageDTO);

                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("向rabbitmq发送nack失败，发送邮件向{}", mailMessageDTO.getEmail());
            }
        } catch (IOException e) {
            log.error("向rabbitmq发送ack失败，发送邮件向{}", mailMessageDTO.getEmail());
            throw new GlobalException("向rabbitmq发送ack失败");
        }


    }

    @RabbitListener(queues = {MailMQConfig.DLQ})
    public void processDealLetter(MailMessageDTO mailMessageDTO, Message message, Channel channel) {
        log.warn("有邮件信息进入死信队列，处理，dto={}", mailMessageDTO);

        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        //todo: handle the dead letter of mail service
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.warn("邮件服务向死信队列发送ack失败");

        }
    }


    private void sendEmail(String email,
                           String code,
                           EmailVerifyPurpose purpose) throws MessagingException {
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
}
