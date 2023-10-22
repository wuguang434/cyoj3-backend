package com.xlei.cyoj3.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import static com.xlei.cyoj3.constant.MqConstant.*;

/**
 * 创建测试程序用到的交换机和队列(只用在程序启动前执行一次)
 */
@Slf4j
public class CodeMqinitMain {
    public static void doInitCodeMq() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setPassword("guest");
            factory.setUsername("guest");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String codeExchangeName = CODE_EXCHANGE_NAME;
            channel.exchangeDeclare(codeExchangeName, CODE_DIRECT_EXCHANGE);

            //创建code队列
            HashMap<String, Object> codeMap = new HashMap<>();
            String codeQueue = CODE_QUEUE;

            //code队列绑定死信交换机
            codeMap.put("x-dead-letter-exchange", CODE_DLX_EXCHANGE);
            codeMap.put("x-dead-letter-routing-key", CODE_DLX_ROUTING_KEY);
            channel.queueDeclare(codeQueue, true, false, false, codeMap);
            channel.queueBind(codeQueue, codeExchangeName, CODE_ROUTING_KEY);

            //创建死信队列
            channel.queueDeclare(CODE_DLX_QUEUE, true, false, false, null);
            //创建死信交换机
            channel.exchangeDeclare(CODE_DLX_EXCHANGE,CODE_DIRECT_EXCHANGE);
            channel.queueBind(CODE_DLX_QUEUE, CODE_DLX_EXCHANGE, CODE_DLX_ROUTING_KEY);

            log.info("消息队列启动成功");
        } catch (Exception e) {
            log.info("消息队列启动失败");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        doInitCodeMq();
    }
}
