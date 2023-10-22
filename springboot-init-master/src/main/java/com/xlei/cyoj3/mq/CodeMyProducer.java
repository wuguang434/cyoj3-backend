package com.xlei.cyoj3.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 消息队列-生产者
 */
@Component
public class CodeMyProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 生产者生产消息
     * @param exchange
     * @param routingKey
     * @param message
     */
    public void sendMessage(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
