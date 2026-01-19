package com.demo.mq.producer;

import com.demo.config.MailMQConfig;
import com.demo.mq.consumer.MailSenderConsumer;
import com.demo.pojo.dto.MailMessageDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MailSenderProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;


    public void send(MailMessageDTO mailMessageDTO) {
        rabbitTemplate.convertAndSend(
                MailMQConfig.EXCHANGE,
                MailMQConfig.ROUTE,
                mailMessageDTO
        );
    }
}
